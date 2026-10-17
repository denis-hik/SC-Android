package sc.denishik.ru.ledApiBLU;

import android.content.*;
import android.os.Build;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import sc.denishik.ru.R;

public final class DevicesSheet {
    public static void show(AppCompatActivity activity) {
        LedConnections connections = LedConnections.get(activity);
        if (connections.entries().isEmpty()) {
            DevicePicker.show(activity, client -> show(activity));
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.connect_view, null);
        dialog.setContentView(view);
        ((TextView) view.findViewById(R.id.title)).setText(R.string.led_ble_title);
        view.findViewById(R.id.tuya_login).setVisibility(View.GONE);
        view.findViewById(R.id.tuya_settings).setVisibility(View.GONE);
        ImageView add = view.findViewById(R.id.connect);
        add.setImageResource(R.drawable.ic_connect_add);
        add.setContentDescription(activity.getString(R.string.led_ble_add));
        add.setOnClickListener(v -> { dialog.dismiss(); DevicePicker.show(activity, client -> show(activity)); });
        ((ImageView) view.findViewById(R.id.refresh)).setImageResource(R.drawable.abc_ic_ab_back_material);
        view.findViewById(R.id.refresh).setOnClickListener(v -> dialog.dismiss());
        LinearLayout base = view.findViewById(R.id.base_block);
        base.removeAllViews();
        ScrollView scroll = new ScrollView(activity);
        LinearLayout content = new LinearLayout(activity); content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content); base.addView(scroll, new LinearLayout.LayoutParams(-1, (int)(420 * activity.getResources().getDisplayMetrics().density)));
        final String[] rendered = {null};
        Runnable render = () -> {
            StringBuilder stateKey = new StringBuilder();
            for (LedConnections.Entry entry : connections.entries()) stateKey.append(entry.address)
                    .append(entry.connected()).append(entry.client == null ? "" : entry.client.getStatus());
            stateKey.append(connections.connecting());
            if (stateKey.toString().equals(rendered[0])) return;
            rendered[0] = stateKey.toString();
            content.removeAllViews();
            text(activity, content, activity.getString(R.string.led_ble_count, connections.connectedCount(), connections.entries().size()));
            text(activity, content, activity.getString(R.string.led_ble_capacity));
            add.setEnabled(!connections.connecting());
            add.setAlpha(connections.connecting() ? 0.4f : 1f);
            if (connections.connecting()) text(activity, content, activity.getString(R.string.led_ble_busy));
            for (LedConnections.Entry entry : connections.entries()) {
                String state = entry.client == null ? activity.getString(R.string.led_ble_waiting) : entry.client.getStatus();
                View item = activity.getLayoutInflater().inflate(R.layout.led_ble_device_item, content, false);
                ((TextView) item.findViewById(R.id.ble_device_details)).setText(entry.name + "\n" + entry.address + "\n" + state);
                View forget = item.findViewById(R.id.ble_device_forget);
                forget.setContentDescription(activity.getString(R.string.led_ble_forget) + ": " + entry.name);
                forget.setOnClickListener(v -> connections.forget(entry.address));
                content.addView(item);
            }
            text(activity, content, activity.getString(R.string.led_ble_saved_hint));
        };
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) { render.run(); }
        };
        IntentFilter filter = new IntentFilter(Client.STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) activity.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else activity.registerReceiver(receiver, filter);
        androidx.lifecycle.LifecycleEventObserver observer = (owner, event) -> {
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) dialog.dismiss();
        };
        activity.getLifecycle().addObserver(observer);
        dialog.setOnDismissListener(d -> {
            activity.unregisterReceiver(receiver); activity.getLifecycle().removeObserver(observer);
        });
        dialog.show();
        dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        render.run();
    }
    private static void text(AppCompatActivity activity, LinearLayout content, String value) {
        TextView text = new TextView(activity); text.setText(value); text.setTextSize(15);
        text.setTextColor(activity.getColor(R.color.app_text)); text.setPadding(16, 16, 16, 16); content.addView(text);
    }
}
