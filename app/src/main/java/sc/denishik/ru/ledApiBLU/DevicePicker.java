package sc.denishik.ru.ledApiBLU;

import android.Manifest;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import sc.denishik.ru.R;

/** Discovery and connection UI; a selection is accepted only after GATT confirmation. */
public final class DevicePicker {
    public interface Callback { void onConnected(Client client); }
    public static void show(AppCompatActivity activity, Callback callback) {
        LedConnections connections = LedConnections.get(activity);
        if (connections.connecting()) { Toast.makeText(activity, R.string.led_ble_busy, Toast.LENGTH_SHORT).show(); return; }
        connections.pickerOpen(true);
        final LedBluDevice[] selected = {null};
        Client client = new Client(activity);
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.connect_view, null);
        dialog.setContentView(view);
        ((TextView) view.findViewById(R.id.title)).setText(R.string.led_ble_title);
        view.findViewById(R.id.tuya_login).setVisibility(View.GONE);
        view.findViewById(R.id.tuya_settings).setVisibility(View.GONE);
        view.findViewById(R.id.empty).setVisibility(View.GONE);
        TextView status = new TextView(activity);
        status.setTextColor(activity.getColor(R.color.app_text_secondary));
        status.setPadding(16, 16, 16, 16);
        ((LinearLayout) view.findViewById(R.id.base_block)).addView(status, 0);
        ListView list = view.findViewById(R.id.listview1);
        list.getLayoutParams().height = (int) (280 * activity.getResources().getDisplayMetrics().density);
        ProgressBar loading = view.findViewById(R.id.loading);
        ImageView search = view.findViewById(R.id.connect);
        ImageView back = view.findViewById(R.id.refresh);
        search.setImageResource(R.drawable.refresh);
        back.setImageResource(R.drawable.abc_ic_ab_back_material);
        back.setOnClickListener(v -> dialog.dismiss());
        ArrayList<LedBluDevice> devices = new ArrayList<>();
        ArrayAdapter<LedBluDevice> adapter = new ArrayAdapter<LedBluDevice>(activity, android.R.layout.simple_list_item_2, android.R.id.text1, devices) {
            @Override public View getView(int position, View convert, ViewGroup parent) {
                View row = super.getView(position, convert, parent);
                TextView title = row.findViewById(android.R.id.text1), detail = row.findViewById(android.R.id.text2);
                title.setText(getItem(position).getName()); detail.setText(getItem(position).getAddress());
                title.setTextColor(activity.getColor(R.color.app_text));
                detail.setTextColor(activity.getColor(R.color.app_text_secondary));
                row.setBackgroundResource(R.drawable.bg_connect_card);
                return row;
            }
        };
        list.setAdapter(adapter);
        Runnable scan = () -> {
            if (!Client.hasPermissions(activity)) {
                activity.requestPermissions(Build.VERSION.SDK_INT >= 31
                        ? new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}
                        : new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 821);
                status.setText(R.string.led_ble_permissions);
                loading.setVisibility(View.GONE); return;
            }
            status.setText(R.string.led_ble_search); loading.setVisibility(View.VISIBLE);
            devices.clear(); adapter.notifyDataSetChanged(); list.setVisibility(View.GONE);
            client.onSearch(new Client.Callback() {
                @Override public void onSuccess(ArrayList<LedBluDevice> result) {
                    if (!dialog.isShowing()) return;
                    devices.clear();
                    for (LedBluDevice device : result) if (!connections.contains(device.getAddress())) devices.add(device);
                    adapter.notifyDataSetChanged();
                    loading.setVisibility(View.GONE);
                    list.setVisibility(devices.isEmpty() ? View.GONE : View.VISIBLE);
                    status.setText(devices.isEmpty() ? R.string.led_ble_empty : R.string.led_ble_title);
                }
                @Override public void onError(String error) {
                    loading.setVisibility(View.GONE);
                    status.setText(error);
                }
            });
        };
        search.setOnClickListener(v -> scan.run());
        client.setListener(() -> {
            status.setText(client.getStatus());
            if (client.isConnected()) {
                dialog.dismiss();
                connections.remember(selected[0], client); callback.onConnected(client);
            } else if (!client.hasSelection()) {
                status.append("\n\n" + activity.getString(R.string.led_ble_extra_failed));
                dialog.setCancelable(true); dialog.getBehavior().setDraggable(true);
                loading.setVisibility(View.GONE); list.setEnabled(true); search.setEnabled(true); back.setEnabled(true);
            }
        });
        list.setOnItemClickListener((parent, row, position, id) -> {
            dialog.setCancelable(false);
            dialog.getBehavior().setDraggable(false);
            list.setEnabled(false);
            search.setEnabled(false); back.setEnabled(false);
            selected[0] = devices.get(position);
            loading.setVisibility(View.VISIBLE);
            client.selectDevice(selected[0]);
        });
        androidx.lifecycle.LifecycleEventObserver observer = (owner, event) -> {
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) dialog.dismiss();
        };
        activity.getLifecycle().addObserver(observer);
        dialog.setOnDismissListener(d -> {
            activity.getLifecycle().removeObserver(observer);
            connections.pickerOpen(false);
            client.setListener(null); client.stopSearch();
            if (!client.isConnected())
                client.unSelectDevice();
        });
        dialog.show();
        dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        scan.run();
    }
}
