package sc.denishik.ru.recordingApi;

import android.app.*;
import android.content.*;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import androidx.core.app.NotificationCompat;
import java.io.File;
import sc.denishik.ru.R;

/** Owns the projection and finalizes the MP4 even when Android revokes capture. */
public final class ScreenRecordingService extends Service {
    private MediaProjection projection;
    private VirtualDisplay display;
    private MediaRecorder recorder;
    private ParcelFileDescriptor output;
    private Uri uri;
    private File legacyFile;
    private ResultReceiver receiver;
    private boolean recording, finishing;
    private final MediaProjection.Callback callback = new MediaProjection.Callback() {
        @Override public void onStop() { finishRecording(null); }
    };
    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || "stop".equals(intent.getAction())) {
            finishRecording(null); return START_NOT_STICKY;
        }
        if (recorder != null) return START_NOT_STICKY;
        receiver = intent.getParcelableExtra("receiver");
        try {
            String channel = "speedometer_recording";
            if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager.class)
                    .createNotificationChannel(new NotificationChannel(channel, "Запись спидометра", NotificationManager.IMPORTANCE_LOW));
            PendingIntent stop = PendingIntent.getService(this, 91,
                    new Intent(this, ScreenRecordingService.class).setAction("stop"),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new NotificationCompat.Builder(this, channel)
                    .setSmallIcon(R.drawable.ic_record).setContentTitle("Запись спидометра")
                    .setContentText("Нажмите «Остановить», чтобы сохранить видео")
                    .setOngoing(true).addAction(R.drawable.ic_record_stop, "Остановить", stop).build();
            if (Build.VERSION.SDK_INT >= 29) startForeground(91, notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            else startForeground(91, notification);

            String name = "SC-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss-SSS", java.util.Locale.US)
                    .format(new java.util.Date()) + ".mp4";
            recorder = new MediaRecorder();
            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DISPLAY_NAME, name);
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SC-Android");
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
                uri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new java.io.IOException("Cannot create video");
                output = getContentResolver().openFileDescriptor(uri, "w");
                if (output == null) throw new java.io.IOException("Cannot open video");
                recorder.setOutputFile(output.getFileDescriptor());
            } else {
                File directory = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                if (directory == null) throw new java.io.IOException("Storage unavailable");
                legacyFile = new File(directory, name);
                recorder.setOutputFile(legacyFile.getAbsolutePath());
            }
            int width = intent.getIntExtra("width", 720), height = intent.getIntExtra("height", 1280);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setVideoSize(width, height);
            recorder.setVideoFrameRate(30);
            recorder.setVideoEncodingBitRate(8_000_000);
            recorder.setOnErrorListener((mr, what, extra) -> finishRecording("Ошибка записи видео"));
            recorder.prepare();
            MediaProjectionManager manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            projection = manager.getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra("consent"));
            if (projection == null) throw new IllegalStateException("No projection");
            projection.registerCallback(callback, new Handler(Looper.getMainLooper()));
            display = projection.createVirtualDisplay("SC-Speedometer", width, height,
                    intent.getIntExtra("density", 320), DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    recorder.getSurface(), null, null);
            recorder.start(); recording = true;
            if (receiver != null) receiver.send(1, Bundle.EMPTY);
        } catch (Exception error) {
            finishRecording("Не удалось начать запись экрана");
        }
        return START_NOT_STICKY;
    }

    private void finishRecording(String error) {
        if (finishing) return;
        finishing = true;
        boolean saved = recording;
        if (recording) {
            try { recorder.stop(); } catch (RuntimeException e) { saved = false; }
        }
        recording = false;
        if (display != null) { display.release(); display = null; }
        if (recorder != null) { recorder.release(); recorder = null; }
        if (projection != null) {
            projection.unregisterCallback(callback); projection.stop(); projection = null;
        }
        try { if (output != null) output.close(); } catch (java.io.IOException ignored) { }
        output = null;
        try {
            if (uri != null) {
                if (saved) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Video.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                } else getContentResolver().delete(uri, null, null);
            }
            if (!saved && legacyFile != null) legacyFile.delete();
        } catch (Exception e) { saved = false; }
        Bundle result = new Bundle();
        result.putString("message", saved ? (uri != null ? "Видео сохранено в Movies/SC-Android"
                : "Видео сохранено: " + legacyFile.getAbsolutePath())
                : (error != null ? error : "Запись слишком короткая или файл не удалось сохранить"));
        if (receiver != null) { receiver.send(0, result); receiver = null; }
        stopForeground(true);
        stopSelf();
    }
    @Override public void onDestroy() { finishRecording(null); super.onDestroy(); }
}
