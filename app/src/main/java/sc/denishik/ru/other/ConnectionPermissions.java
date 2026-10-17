package sc.denishik.ru.other;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import sc.denishik.ru.R;

public final class ConnectionPermissions {
    private ConnectionPermissions() { }
    private static boolean granted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
    public static String[] missing(Context context) {
        ArrayList<String> result = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31) {
            if (!granted(context, Manifest.permission.BLUETOOTH_SCAN)) result.add(Manifest.permission.BLUETOOTH_SCAN);
            if (!granted(context, Manifest.permission.BLUETOOTH_CONNECT)) result.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        // Current scanners use location; SCAN is not declared neverForLocation.
        // Android 12+ needs both location permissions in a precise-location request.
        if (!granted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            result.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            result.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return result.toArray(new String[0]);
    }
    public static BottomSheetDialog show(AppCompatActivity activity, Modals.showPermissionCallback callback) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.permisson_view);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.go).setOnClickListener(v -> callback.onRequest(dialog));
        dialog.findViewById(R.id.back).setOnClickListener(v -> { dialog.dismiss(); activity.finish(); });
        dialog.setOnShowListener(ignored -> {
            View sheet = dialog.getWindow().findViewById(R.id.design_bottom_sheet);
            sheet.setBackgroundResource(android.R.color.transparent);
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            dialog.getBehavior().setDraggable(false);
            refresh(activity, dialog);
        });
        dialog.show();
        return dialog;
    }
    public static void refresh(AppCompatActivity activity, BottomSheetDialog dialog) {
        boolean bluetooth = Build.VERSION.SDK_INT < 31 || (granted(activity, Manifest.permission.BLUETOOTH_SCAN)
                && granted(activity, Manifest.permission.BLUETOOTH_CONNECT));
        boolean location = granted(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        ((TextView) dialog.findViewById(R.id.permission_bluetooth_status)).setText(bluetooth ? R.string.permission_granted : R.string.permission_needed);
        ((TextView) dialog.findViewById(R.id.permission_location_status)).setText(location ? R.string.permission_granted : R.string.permission_needed);
        boolean blocked = false;
        for (String permission : missing(activity)) {
            if (!granted(activity, permission) && activity.getSharedPreferences("permission_requests", Context.MODE_PRIVATE)
                    .getBoolean(permission, false) && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) blocked = true;
        }
        ((TextView) dialog.findViewById(R.id.permission_hint)).setText(blocked ? R.string.permission_settings_hint : R.string.permission_system_hint);
        ((MaterialButton) dialog.findViewById(R.id.go)).setText(bluetooth && location ? R.string.permission_continue
                : blocked ? R.string.permission_settings : R.string.permission_allow);
    }
}
