package sc.denishik.ru.recordingApi;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.widget.ImageButton;
import android.widget.Toast;
import sc.denishik.ru.HomeActivity;
import sc.denishik.ru.R;

public final class ScreenRecordingController {
    public interface Appearance { void setRecording(boolean recording); }
    private static final int REQUEST = 918;
    private final HomeActivity activity;
    private final ImageButton button;
    private final Appearance appearance;
    private boolean starting, active, closed;
    public ScreenRecordingController(HomeActivity activity, Appearance appearance) {
        this.activity = activity; this.appearance = appearance;
        button = activity.findViewById(R.id.record_speedometer);
        button.setOnClickListener(v -> {
            if (active) { stop(); return; }
            if (starting) return;
            starting = true; button.setEnabled(false);
            try {
                MediaProjectionManager manager = (MediaProjectionManager) activity.getSystemService(Activity.MEDIA_PROJECTION_SERVICE);
                activity.startActivityForResult(manager.createScreenCaptureIntent(), REQUEST);
            } catch (Exception error) { reset(); toast("Запись экрана недоступна"); }
        });
    }
    public boolean onResult(int request, int result, Intent data) {
        if (request != REQUEST) return false;
        if (result != Activity.RESULT_OK || data == null || closed) { reset(); return true; }
        appearance.setRecording(true);
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        float scale = Math.min(1f, 1920f / Math.max(metrics.widthPixels, metrics.heightPixels));
        Intent service = new Intent(activity, ScreenRecordingService.class)
                .putExtra("consent", data)
                .putExtra("width", Math.max(2, ((int)(metrics.widthPixels * scale) / 2) * 2))
                .putExtra("height", Math.max(2, ((int)(metrics.heightPixels * scale) / 2) * 2))
                .putExtra("density", metrics.densityDpi)
                .putExtra("receiver", new ResultReceiver(new Handler(Looper.getMainLooper())) {
                    @Override protected void onReceiveResult(int code, Bundle data) {
                        if (closed) return;
                        if (code == 1) {
                            starting = false; active = true; button.setEnabled(true);
                            button.setImageResource(R.drawable.ic_record_stop);
                            button.setContentDescription("Остановить запись");
                        } else { reset(); toast(data.getString("message", "Запись завершена")); }
                    }
                });
        try {
            if (Build.VERSION.SDK_INT >= 26) activity.startForegroundService(service);
            else activity.startService(service);
            active = true;
        } catch (Exception error) { reset(); toast("Не удалось начать запись"); }
        return true;
    }
    public void stop() {
        if (active) activity.stopService(new Intent(activity, ScreenRecordingService.class));
        reset();
    }
    private void reset() {
        active = false; starting = false;
        if (closed) return;
        appearance.setRecording(false);
        button.setEnabled(true); button.setImageResource(R.drawable.ic_record);
        button.setContentDescription("Записать экран для видео");
    }
    private void toast(String message) { Toast.makeText(activity, message, Toast.LENGTH_LONG).show(); }
    public void destroy() { stop(); closed = true; }
}
