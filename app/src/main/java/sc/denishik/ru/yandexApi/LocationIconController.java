package sc.denishik.ru.yandexApi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AtomicFile;
import androidx.core.content.ContextCompat;
import com.yandex.mapkit.user_location.*;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.runtime.image.ImageProvider;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sc.denishik.ru.R;

/** Owns a bounded local copy; no broad photo-library permission or permanent URI access needed. */
public final class LocationIconController implements UserLocationObjectListener {
    public interface ImportResult { void accept(boolean success); }
    private final Context context;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private UserLocationView locationView;
    private ImageProvider image;
    private Bitmap recordingIcon;
    public Bitmap recordingIcon() { return recordingIcon; }
    private boolean closed;
    private int generation;
    public LocationIconController(Context context, UserLocationLayer layer) {
        this.context = context.getApplicationContext();
        load(); layer.setObjectListener(this);
    }
    private AtomicFile file() { return new AtomicFile(new File(context.getNoBackupFilesDir(), "map-location-icon.png")); }
    private String selected() { return context.getSharedPreferences("map_places", 0).getString("location_icon", "arrow"); }
    private void load() {
        Bitmap bitmap = null;
        if ("custom".equals(selected())) {
            try (InputStream stream = file().openRead()) { bitmap = BitmapFactory.decodeStream(stream); }
            catch (Exception ignored) { }
        }
        if (bitmap == null) {
            Drawable drawable = ContextCompat.getDrawable(context, "scooter".equals(selected()) ? R.drawable.ic_route_scooter : R.drawable.ic_gps_marker);
            bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
            drawable.setTint(0xff315ed4); drawable.setBounds(12, 12, 84, 84); drawable.draw(new Canvas(bitmap));
        }
        recordingIcon = bitmap;
        image = ImageProvider.fromBitmap(bitmap); apply();
    }
    public void select(String name) {
        generation++;
        context.getSharedPreferences("map_places", 0).edit().putString("location_icon", name).apply(); load();
    }
    public void importImage(Uri uri, ImportResult complete) {
        final int request = ++generation;
        worker.execute(() -> {
            Bitmap normalized = null;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = true;
                try (InputStream input = context.getContentResolver().openInputStream(uri)) { BitmapFactory.decodeStream(input, null, options); }
                if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Unsupported image");
                options.inJustDecodeBounds = false; options.inSampleSize = 1;
                while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > 256) options.inSampleSize *= 2;
                Bitmap source;
                try (InputStream input = context.getContentResolver().openInputStream(uri)) { source = BitmapFactory.decodeStream(input, null, options); }
                if (source == null) throw new IOException("Unsupported image");
                normalized = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
                float scale = 88f / Math.max(source.getWidth(), source.getHeight());
                float w = source.getWidth() * scale, h = source.getHeight() * scale;
                new Canvas(normalized).drawBitmap(source, null, new android.graphics.RectF((96-w)/2, (96-h)/2, (96+w)/2, (96+h)/2),
                        new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.FILTER_BITMAP_FLAG));
                source.recycle();
            } catch (Exception ignored) { }
            final Bitmap icon = normalized;
            main.post(() -> {
                if (closed || request != generation) return;
                boolean success = false;
                if (icon != null) {
                    AtomicFile target = file(); FileOutputStream output = null;
                    try {
                        output = target.startWrite();
                        if (!icon.compress(Bitmap.CompressFormat.PNG, 100, output)) throw new IOException("PNG encoding failed");
                        target.finishWrite(output);
                        context.getSharedPreferences("map_places", 0).edit().putString("location_icon", "custom").apply();
                        recordingIcon = icon;
                        image = ImageProvider.fromBitmap(icon); apply(); success = true;
                    } catch (Exception error) { if (output != null) target.failWrite(output); }
                }
                complete.accept(success);
            });
        });
    }
    private void apply() {
        if (locationView == null || !locationView.isValid() || image == null) return;
        IconStyle style = new IconStyle().setAnchor(new PointF(0.5f, 0.5f)).setScale(0.5f);
        locationView.getArrow().setIcon(image, style);
        locationView.getPin().setIcon(image, style);
    }
    public void onObjectAdded(UserLocationView view) { locationView = view; apply(); }
    public void onObjectRemoved(UserLocationView view) { locationView = null; }
    public void onObjectUpdated(UserLocationView view, ObjectEvent event) { locationView = view; }
    public void destroy() { closed = true; generation++; worker.shutdownNow(); main.removeCallbacksAndMessages(null); locationView = null; }
}
