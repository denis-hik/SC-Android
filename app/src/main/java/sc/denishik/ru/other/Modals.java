package sc.denishik.ru.other;

import static sc.denishik.ru.midwayApi.ScootersApi.getScooters;
import static sc.denishik.ru.other.Config.SCOOTER_CONNECT_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_STATUS_CONNECTED;
import static sc.denishik.ru.other.Config.SCOOTER_STATUS_ERROR;
import static sc.denishik.ru.other.Config.SCOOTER_STATUS_BINDED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.net.URL;

import cn.wandersnail.ble.Device;
import sc.denishik.ru.HomeActivity;
import sc.denishik.ru.R;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.ledApiBLU.Client;
import sc.denishik.ru.ledApiBLU.LedBluDevice;
import sc.denishik.ru.midwayApi.Scooter;
import sc.denishik.ru.midwayApi.ScootersApi;
import sc.denishik.ru.kuggoApi.kugooApi;
import sc.denishik.ru.tuya.OfflineKeysActivity;

public class Modals {
    public static void SettingsWSModal(AppCompatActivity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        sc.denishik.ru.ledApiWS.Config.load(activity);
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        View content = activity.getLayoutInflater().inflate(R.layout.led_ws_settings_sheet, null);
        content.findViewById(R.id.led_info).setOnClickListener(v -> InfoWSModal(activity));
        EditText host = content.findViewById(R.id.ws_host_input);
        EditText port = content.findViewById(R.id.ws_port_input);
        EditText board = content.findViewById(R.id.ws_board_input);
        host.setText(sc.denishik.ru.ledApiWS.Config.IP_HOST_DEFAULT);
        port.setText(sc.denishik.ru.ledApiWS.Config.PORT_CLIENT_DEFAULT);
        board.setText(sc.denishik.ru.ledApiWS.Config.ID_BOARD);
        com.google.android.material.slider.Slider targetSpeed = content.findViewById(R.id.ws_target_speed);
        TextView speedLabel = content.findViewById(R.id.ws_target_speed_label);
        targetSpeed.setValue(sc.denishik.ru.ledApiWS.Config.TARGET_SPEED);
        speedLabel.setText(activity.getString(R.string.led_settings_speed_value, Math.round(targetSpeed.getValue())));
        targetSpeed.addOnChangeListener((slider, value, fromUser) ->
                speedLabel.setText(activity.getString(R.string.led_settings_speed_value, Math.round(value))));
        com.google.android.material.textfield.TextInputLayout hostLayout = content.findViewById(R.id.ws_host_layout);
        com.google.android.material.textfield.TextInputLayout portLayout = content.findViewById(R.id.ws_port_layout);
        com.google.android.material.textfield.TextInputLayout boardLayout = content.findViewById(R.id.ws_board_layout);
        content.findViewById(R.id.ws_cancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.ws_save).setOnClickListener(v -> {
            hostLayout.setError(null); portLayout.setError(null); boardLayout.setError(null);
            String hostValue = host.getText().toString().trim();
            String boardValue = board.getText().toString().trim();
            boolean valid = true;
            try {
                java.net.URI address = new java.net.URI("ws://" + hostValue);
                if (hostValue.isEmpty() || address.getHost() == null || address.getRawUserInfo() != null
                        || address.getPort() != -1 || !address.getRawPath().isEmpty()
                        || address.getRawQuery() != null || address.getRawFragment() != null)
                    throw new IllegalArgumentException();
            } catch (Exception invalid) { hostLayout.setError(activity.getString(R.string.led_settings_host_error)); valid = false; }
            int portValue = 0;
            try {
                portValue = Integer.parseInt(port.getText().toString().trim());
                if (portValue < 1 || portValue > 65535) throw new IllegalArgumentException();
            } catch (Exception invalid) { portLayout.setError(activity.getString(R.string.led_settings_port_error)); valid = false; }
            if (!boardValue.matches("[A-Za-z0-9_-]{1,64}")) {
                boardLayout.setError(activity.getString(R.string.led_settings_board_error)); valid = false;
            }
            if (!valid) return;
            sc.denishik.ru.ledApiWS.Config.save(activity, hostValue, String.valueOf(portValue), boardValue,
                    Math.round(targetSpeed.getValue()));
            int message = R.string.led_settings_saved;
            try {
                activity.startService(new Intent(activity, ServiceScooter.class)
                        .putExtra("command", sc.denishik.ru.other.Config.SCOOTER_LED_RECONNECT));
            } catch (IllegalStateException | SecurityException unavailable) {
                message = R.string.led_settings_reconnect_error;
            }
            android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.setContentView(content);
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        androidx.lifecycle.LifecycleEventObserver observer = (owner, event) -> {
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) dialog.dismiss();
        };
        activity.getLifecycle().addObserver(observer);
        dialog.setOnDismissListener(ignored -> activity.getLifecycle().removeObserver(observer));
        dialog.show();
        dialog.getWindow().findViewById(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent);
        dialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
    }

    public static void InfoWSModal(AppCompatActivity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        sc.denishik.ru.ledApiWS.Config.load(activity);
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        View content = activity.getLayoutInflater().inflate(R.layout.led_info_sheet, null);
        String board = sc.denishik.ru.ledApiWS.Config.ID_BOARD;
        String address = "ws://" + sc.denishik.ru.ledApiWS.Config.IP_HOST_DEFAULT + ":"
                + sc.denishik.ru.ledApiWS.Config.PORT_CLIENT_DEFAULT + "/hub/" + board;
        ((TextView) content.findViewById(R.id.led_info_destination)).setText(
                activity.getString(R.string.led_info_destination, address));
        ((TextView) content.findViewById(R.id.led_info_payload)).setText(
                activity.getString(R.string.led_info_payload, board,
                        String.valueOf(sc.denishik.ru.ledApiWS.Config.ledValue(10, false)),
                        sc.denishik.ru.ledApiWS.Config.TARGET_SPEED));
        content.findViewById(R.id.led_info_close).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.led_info_source).setOnClickListener(v -> {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://github.com/GyverLibs/GyverHub/")));
            } catch (android.content.ActivityNotFoundException ignored) {
                android.widget.Toast.makeText(activity, R.string.led_info_no_browser,
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setContentView(content);
        androidx.lifecycle.LifecycleEventObserver observer = (owner, event) -> {
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) dialog.dismiss();
        };
        activity.getLifecycle().addObserver(observer);
        dialog.setOnDismissListener(ignored -> activity.getLifecycle().removeObserver(observer));
        dialog.show();
        dialog.getWindow().findViewById(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent);
    }

    public static void showKugooLogin(AppCompatActivity activity) {
        if (activity.getSupportFragmentManager().findFragmentByTag("kugoo_login") == null)
            new KugooLoginSheet().show(activity.getSupportFragmentManager(), "kugoo_login");
    }

    public interface KugooProfileSelection {
        void onSelected(org.json.JSONObject profile);
    }

    public static void showPrimaryKugoo(AppCompatActivity activity,
            java.util.List<org.json.JSONObject> profiles, String selectedId, KugooProfileSelection callback) {
        if (activity.isFinishing() || activity.isDestroyed() || profiles.isEmpty()) return;
        final com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        View content = activity.getLayoutInflater().inflate(R.layout.kugoo_primary_sheet, null);
        LinearLayout list = content.findViewById(R.id.primary_list);
        final boolean[] chosen = {false};
        for (org.json.JSONObject profile : profiles) {
            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) activity.getLayoutInflater()
                            .inflate(R.layout.item_saved_kugoo, list, false);
            String id = profile.optString("devId");
            String name = profile.optString("name", "Kugoo");
            boolean selected = id.equals(selectedId);
            ((TextView) card.findViewById(R.id.kugoo_device_name)).setText(name);
            ((TextView) card.findViewById(R.id.kugoo_device_detail)).setText(activity.getString(
                    selected ? R.string.kugoo_primary_current : R.string.kugoo_profile_detail,
                    id.substring(Math.max(0, id.length() - 6))));
            if (selected) card.setStrokeColor(androidx.core.content.ContextCompat.getColor(activity, R.color.colorAccent));
            android.widget.Button select = card.findViewById(R.id.kugoo_device_connect);
            select.setText(selected ? R.string.kugoo_primary_keep : R.string.kugoo_primary_select);
            select.setContentDescription(select.getText() + ": " + name);
            select.setOnClickListener(v -> {
                if (chosen[0]) return;
                chosen[0] = true;
                dialog.dismiss();
                callback.onSelected(profile);
            });
            list.addView(card);
        }
        content.findViewById(R.id.primary_back).setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(content);
        androidx.lifecycle.LifecycleEventObserver lifecycle = (owner, event) -> {
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) dialog.dismiss();
        };
        activity.getLifecycle().addObserver(lifecycle);
        dialog.setOnDismissListener(d -> activity.getLifecycle().removeObserver(lifecycle));
        dialog.setOnShowListener(d -> {
            View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) sheet.setBackgroundResource(android.R.color.transparent);
            // Keep the heading visible while long profile lists scroll, including in landscape.
            View scroll = content.findViewById(R.id.primary_scroll);
            content.post(() -> {
                if (!dialog.isShowing()) return;
                android.graphics.Rect visible = new android.graphics.Rect();
                activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);
                int headerHeight = content.getHeight() - scroll.getHeight();
                int available = Math.max(1, (int) (visible.height() * 0.85f) - headerHeight);
                ViewGroup.LayoutParams params = scroll.getLayoutParams();
                params.height = Math.min(list.getHeight(), available);
                scroll.setLayoutParams(params);
                dialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            });
        });
        dialog.show();
    }

    public interface showPermissionCallback {
        void onRequest(com.google.android.material.bottomsheet.BottomSheetDialog dialog);
    }
    public static com.google.android.material.bottomsheet.BottomSheetDialog showPermissionModal(AppCompatActivity activity, showPermissionCallback callback) {
        return ConnectionPermissions.show(activity, callback);

    }

    public interface ICallback {
        void onConnect(Client client);
    }
    public static void showModalLedsBluetooth(AppCompatActivity activity, ICallback callbackModal) {
        sc.denishik.ru.ledApiBLU.DevicePicker.show(activity, callbackModal::onConnect);
    }

    public static void showStartModal(AppCompatActivity activity, LinearLayout blu_err) {
        final LinearLayout[] loading_item = new LinearLayout[1];
        final LinearLayout[] loaded_item = new LinearLayout[1];
        final boolean[] isShow = {false};
        final com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        View lay = activity.getLayoutInflater().inflate(R.layout.connect_view, null); dialog.setContentView(lay);
        final LinearLayout linear1 = (LinearLayout)lay.findViewById(R.id.linear1);
        final ListView listView1 = lay.findViewById(R.id.listview1);
        final ImageView empty = lay.findViewById(R.id.empty);
        final ImageView refresh = lay.findViewById(R.id.refresh);
        final ImageView tuyaLogin = lay.findViewById(R.id.tuya_login);
        final View tuyaSettings = lay.findViewById(R.id.tuya_settings);
        final ProgressBar loading = lay.findViewById(R.id.loading);
        final LinearLayout connect_block = lay.findViewById(R.id.connect_block);
        final LinearLayout base_block = lay.findViewById(R.id.base_block);
        final EditText connect_text = lay.findViewById(R.id.connect_text);
        final ImageView connect_go = lay.findViewById(R.id.connect_go);
        final ImageView connect = lay.findViewById(R.id.connect);
        final ImageView connect_back = lay.findViewById(R.id.connect_back);

        dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog.show();
        dialog.setCancelable(false);
        final boolean[] connectionPending = {false};
        final java.util.function.Consumer<Boolean> setConnectionPending = pending -> {
            connectionPending[0] = pending;
            dialog.setCancelable(!pending);
            dialog.setCanceledOnTouchOutside(!pending);
            dialog.getBehavior().setDraggable(!pending);
            dialog.getBehavior().setHideable(!pending);
            connect.setEnabled(!pending);
            connect_back.setEnabled(!pending);
            connect_go.setEnabled(!pending);
            refresh.setEnabled(!pending);
            tuyaLogin.setEnabled(!pending);
            tuyaSettings.setEnabled(!pending);
            listView1.setEnabled(!pending);
        };


        final LinearLayout savedKugooPanel = lay.findViewById(R.id.saved_kugoo_list);
        final TextView kugooStatus = lay.findViewById(R.id.kugoo_connect_status);
        final View kugooContent = lay.findViewById(R.id.saved_kugoo_content);
        final boolean[] kugooMode = {false};
        final java.util.List<android.widget.Button> kugooButtons = new java.util.ArrayList<>();
        final int[] profileLoadGeneration = {0};
        final boolean[] kugooSelectionPending = {false};
        final Runnable renderKugooMode = () -> {
            kugooContent.setVisibility(kugooMode[0] ? View.VISIBLE : View.GONE);
            tuyaSettings.setVisibility(kugooMode[0] ? View.VISIBLE : View.GONE);
            tuyaLogin.setSelected(kugooMode[0]);
            ((TextView) lay.findViewById(R.id.title)).setText(kugooMode[0] ? "Kugoo" : "Midway");
            if (kugooMode[0]) {
                base_block.setVisibility(View.VISIBLE);
                listView1.setVisibility(View.GONE);
                empty.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                connect_block.setVisibility(View.GONE);
                connect_back.setVisibility(View.VISIBLE);
                connect_back.setEnabled(!kugooSelectionPending[0]);
                connect.setVisibility(View.GONE);
                refresh.setVisibility(View.GONE);
            } else {
                connect_back.setVisibility(View.GONE);
                connect_back.setEnabled(true);
                refresh.setVisibility(View.VISIBLE);
            }
        };
        final Runnable reloadSavedKugoo = () -> {
            if (!kugooMode[0] || kugooSelectionPending[0]) return;
            kugooStatus.setText(R.string.kugoo_profile_loading);
            kugooStatus.setVisibility(View.VISIBLE);
            lay.findViewById(R.id.kugoo_empty).setVisibility(View.GONE);
            final int request = ++profileLoadGeneration[0];
            new Thread(() -> {
                try {
                    java.util.List<org.json.JSONObject> profiles = sc.denishik.ru.tuya.KugooProfileStore.loadAll(activity.getApplicationContext());
                    activity.runOnUiThread(() -> {
                        if (!dialog.isShowing() || activity.isDestroyed() || request != profileLoadGeneration[0] || !kugooMode[0]) return;
                        savedKugooPanel.removeAllViews();
                        kugooButtons.clear();
                        kugooStatus.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
                        kugooStatus.setText(profiles.isEmpty() ? activity.getString(R.string.kugoo_no_saved) : "");
                        lay.findViewById(R.id.kugoo_empty).setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
                        savedKugooPanel.setVisibility(profiles.isEmpty() ? View.GONE : View.VISIBLE);
                        lay.findViewById(R.id.saved_kugoo_scroll).setVisibility(profiles.isEmpty() ? View.GONE : View.VISIBLE);
                        for (org.json.JSONObject profile : profiles) {
                            View card = activity.getLayoutInflater().inflate(R.layout.item_saved_kugoo, savedKugooPanel, false);
                            ((TextView) card.findViewById(R.id.kugoo_device_name)).setText(profile.optString("name", "Kugoo"));
                            String id = profile.optString("devId");
                            String suffix = id.substring(Math.max(0, id.length() - 6));
                            ((TextView) card.findViewById(R.id.kugoo_device_detail)).setText(activity.getString(R.string.kugoo_profile_detail, suffix));
                            android.widget.Button item = card.findViewById(R.id.kugoo_device_connect);
                            item.setContentDescription(activity.getString(R.string.kugoo_connect) + " " + profile.optString("name", "Kugoo"));
                            kugooButtons.add(item);
                            savedKugooPanel.addView(card);
                            item.setOnClickListener(v -> {
                                if (kugooSelectionPending[0]) return;
                                if (android.os.Build.VERSION.SDK_INT >= 31 && (activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                        || activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                                    activity.requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN}, 41);
                                    return;
                                }
                                kugooSelectionPending[0] = true;
                                for (android.widget.Button button : kugooButtons) button.setEnabled(false);
                                item.setText(R.string.kugoo_connecting);
                                connect_back.setEnabled(false);
                                tuyaSettings.setEnabled(false);
                                tuyaLogin.setEnabled(false);
                                refresh.setEnabled(false);
                                kugooStatus.setVisibility(View.VISIBLE);
                                kugooStatus.setText("Подключение «" + profile.optString("name", "Kugoo") + "»…");
                                Intent start = new Intent(activity, ServiceScooter.class);
                                start.putExtra("command", SCOOTER_CONNECT_COMMAND);
                                start.putExtra("kugoo_dev_id", profile.optString("devId"));
                                setConnectionPending.accept(true);
                                activity.startService(start);
                            });
                        }
                    });
                } catch (Exception error) {
                    activity.runOnUiThread(() -> {
                        if (!dialog.isShowing() || request != profileLoadGeneration[0] || !kugooMode[0]) return;
                        kugooStatus.setVisibility(View.VISIBLE);
                        kugooStatus.setText("Не удалось прочитать привязки Kugoo. Откройте настройки и повторите импорт.");
                    });
                }
            }, "kugoo-profiles").start();
        };
        final Runnable returnToScan = () -> {
            if (kugooSelectionPending[0]) return;
            kugooMode[0] = false;
            profileLoadGeneration[0]++;
            renderKugooMode.run();
            connect.setVisibility(View.VISIBLE);
            refresh.performClick();
        };
        tuyaSettings.setOnClickListener(v -> openOfflineKeys(activity));
        tuyaLogin.setOnClickListener(v -> {
            if (kugooSelectionPending[0]) return;
            if (kugooMode[0]) {
                returnToScan.run();
                return;
            }
            kugooMode[0] = true;
            renderKugooMode.run();
            connect_back.setOnClickListener(back -> returnToScan.run());
            reloadSavedKugoo.run();
        });

        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onRec", String.valueOf(intent.getAction()));
                String s = intent.getStringExtra("Status");
                if (kugooMode[0] && !kugooSelectionPending[0]) return;
                if (Objects.equals(s, SCOOTER_STATUS_ERROR) || Objects.equals(s, SCOOTER_STATUS_BINDED))
                    setConnectionPending.accept(false);
                if (intent.hasExtra("message")) { kugooStatus.setVisibility(View.VISIBLE); kugooStatus.setText(intent.getStringExtra("message")); }
                if (Objects.equals(s, SCOOTER_STATUS_CONNECTED)) {
                    if (!isShow[0]) {
                        listView1.setVisibility(View.GONE);
                        isShow[0] = true;
                        dialog.dismiss();
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_VIEW);
                        i.setClass(activity.getApplicationContext(), HomeActivity.class);
                        activity.startActivity(i);
                        activity.finish();
                    }
                } else if (kugooMode[0]) {
                    renderKugooMode.run();
                    if (Objects.equals(s, SCOOTER_STATUS_ERROR)) {
                        kugooSelectionPending[0] = false;
                        for (android.widget.Button button : kugooButtons) {
                            button.setEnabled(true);
                            button.setText(R.string.kugoo_connect);
                        }
                        connect_back.setEnabled(true);
                        tuyaSettings.setEnabled(true);
                        tuyaLogin.setEnabled(true);
                        refresh.setEnabled(true);
                    }
                } else if (Objects.equals(s, SCOOTER_STATUS_BINDED)) {
                    String address = intent.getStringExtra("address");
                    if (address != null && !address.isEmpty()) {
                        activity.getSharedPreferences("bind_state", Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(address, true)
                                .apply();
                    }
                    if (loading_item[0] != null) loading_item[0].setVisibility(View.GONE);
                    if (loaded_item[0] != null) loaded_item[0].setVisibility(View.VISIBLE);
                    connect.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                    empty.setVisibility(View.GONE);
                    listView1.setVisibility(View.GONE);
                    refresh.setVisibility(View.VISIBLE);
                    base_block.setVisibility(View.VISIBLE);
                    connect_block.setVisibility(View.GONE);
                    connect_back.setVisibility(View.GONE);

                    getScooters(activity, new ScootersApi.getScooterCallback() {
                        @Override
                        public void onGetScooter(ArrayList<Scooter> scooters) {
                            activity.runOnUiThread(() -> {
                                if (connectionPending[0] || kugooMode[0] || !dialog.isShowing() || connect_block.getVisibility() == View.VISIBLE) return;
                                if (scooters.size() > 0) {
                                    empty.setVisibility(View.GONE);
                                    listView1.setVisibility(View.VISIBLE);
                                    listView1.setAdapter(new Adapters.ListviewScootersAdapter(scooters, activity, (scooter, loading1, loaded) -> {
                                        loading_item[0] = loading1;
                                        loaded_item[0] = loaded;
                                        Intent i = new Intent(activity, ServiceScooter.class);
                                        i.putExtra("command", SCOOTER_CONNECT_COMMAND);
                                        i.putExtra("uuid", scooter.getUUID());
                                        i.putExtra("address", scooter.getAddress());
                                        i.putExtra("name", scooter.getName());
                                        setConnectionPending.accept(true);
                                        activity.startService(i);
                                    }));
                                } else {
                                    empty.setVisibility(View.VISIBLE);
                                    listView1.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onGetError() {
                            activity.runOnUiThread(() -> {
                                if (connectionPending[0] || kugooMode[0] || !dialog.isShowing()) return;
                                blu_err.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            });
                        }
                    });
                } else if (Objects.equals(s, SCOOTER_STATUS_ERROR)) {
                    kugooSelectionPending[0] = false;
                    for (android.widget.Button button : kugooButtons) button.setEnabled(true);
                    if (loading_item[0] != null) loading_item[0].setVisibility(View.GONE);
                    if (loaded_item[0] != null) loaded_item[0].setVisibility(View.VISIBLE);
                    connect.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                    listView1.setVisibility(View.GONE);
                    refresh.setVisibility(View.VISIBLE);
                    base_block.setVisibility(View.VISIBLE);
                    connect_block.setVisibility(View.GONE);
                    connect_back.setVisibility(View.GONE);
                } else {
                    connect.setVisibility(View.GONE);
                    refresh.setVisibility(View.GONE);
                    empty.setVisibility(View.GONE);
                    base_block.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.VISIBLE);
                    listView1.setVisibility(View.GONE);
                    connect_block.setVisibility(View.GONE);
                    connect_back.setVisibility(View.GONE);
                }
            }
        };
        activity.registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_CONNECT_COMMAND));
        androidx.lifecycle.LifecycleEventObserver resumeProfiles = (owner, event) -> {
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) reloadSavedKugoo.run();
        };
        activity.getLifecycle().addObserver(resumeProfiles);
        dialog.setOnDismissListener(ignored -> {
            profileLoadGeneration[0]++;
            activity.getLifecycle().removeObserver(resumeProfiles);
            try { activity.unregisterReceiver(mMessageReceiver); } catch (IllegalArgumentException alreadyRemoved) { }
        });

        connect.setOnLongClickListener(v -> {
            if (connectionPending[0]) return true;
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setClass(activity.getApplicationContext(), HomeActivity.class);
            activity.startActivity(i);
            activity.finish();

            return false;
        });

        refresh.setOnClickListener(v -> {
            if (kugooMode[0]) { reloadSavedKugoo.run(); return; }
            connect.setVisibility(View.VISIBLE);
            base_block.setVisibility(View.VISIBLE);
            connect_block.setVisibility(View.GONE);
            listView1.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setInterpolator(new LinearInterpolator());
            refresh.startAnimation(rotate);

            getScooters(activity, new ScootersApi.getScooterCallback() {
                @Override
                public void onGetScooter(ArrayList<Scooter> scooters) {
                    Log.d("", "onGetScooter: ".concat(String.valueOf(scooters)));
                    activity.runOnUiThread(() -> {
                        if (connectionPending[0] || kugooMode[0] || !dialog.isShowing() || connect_block.getVisibility() == View.VISIBLE) return;
                        if (scooters.size() > 0) {
//                            for (Scooter scooter : scooters) {
//                                if (scooter.getAddress().contains(activity.getSharedPreferences("login", Activity.MODE_PRIVATE).getString("address", "null"))) {
//                                    listView1.setVisibility(View.GONE);
//                                    Intent i = new Intent(activity, ServiceScooter.class);
//                                    i.putExtra("command", SCOOTER_CONNECT_COMMAND);
//                                    i.putExtra("uuid", scooter.getUUID());
//                                    i.putExtra("address", scooter.getAddress());
//                                    i.putExtra("name", scooter.getName());
//                                    activity.startService(i);
//                                }
//                            }
                            empty.setVisibility(View.GONE);
                            loading.setVisibility(View.GONE);
                            listView1.setVisibility(View.VISIBLE);
                            listView1.setAdapter(new Adapters.ListviewScootersAdapter(scooters, activity, (scooter, loading1, loaded) -> {

                                loading_item[0] = loading1;
                                loaded_item[0] = loaded;
                                 Intent i = new Intent(activity, ServiceScooter.class);
                                 i.putExtra("command", SCOOTER_CONNECT_COMMAND);
                                 i.putExtra("uuid", scooter.getUUID());
                                 i.putExtra("address", scooter.getAddress());
                                 i.putExtra("name", scooter.getName());
                                 Device device = scooter.getDevice();
                                 i.putExtra("name_device", device != null ? device.getName() : scooter.getName());
                                 setConnectionPending.accept(true);
                                 activity.startService(i);
                             }));
                         } else {
                             loading.setVisibility(View.GONE);
                             empty.setVisibility(View.VISIBLE);
                            listView1.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onGetError() {
                    activity.runOnUiThread(() -> {
                        if (connectionPending[0] || kugooMode[0] || !dialog.isShowing()) return;
                        blu_err.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    });
                }
            });
        });

        connect.setOnClickListener(v -> {
            kugooMode[0] = false;
            renderKugooMode.run();
            base_block.setVisibility(View.GONE);
            connect_block.setVisibility(View.VISIBLE);
            connect_back.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.GONE);

            connect_go.setOnClickListener(v1 -> {
                if (String.valueOf(connect_text.getText()).length() < 6) {
                    connect_text.setError("ID not valid!");
                    return;
                }
                connect_text.setError(null);
                Scooter scooter = new Scooter("Unknown", "", "0%", String.valueOf(connect_text.getText()));
                Intent i = new Intent(activity, ServiceScooter.class);
                i.putExtra("command", SCOOTER_CONNECT_COMMAND);
                i.putExtra("uuid", scooter.getUUID());
                i.putExtra("address", scooter.getAddress());
                i.putExtra("name", scooter.getName());
                setConnectionPending.accept(true);
                activity.startService(i);
                connect_block.setVisibility(View.GONE);
                connect.setVisibility(View.GONE);
                listView1.setVisibility(View.GONE);
                empty.setVisibility(View.GONE);
                base_block.setVisibility(View.VISIBLE);
                loading.setVisibility(View.VISIBLE);
            });

            connect_back.setOnClickListener(v2 -> {
                connect_back.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                connect.setVisibility(View.VISIBLE);
                loading.setVisibility(View.VISIBLE);
                connect_block.setVisibility(View.GONE);
                connect_back.setVisibility(View.GONE);
                base_block.setVisibility(View.VISIBLE);

                getScooters(activity, new ScootersApi.getScooterCallback() {
                    @Override
                    public void onGetScooter(ArrayList<Scooter> scooters) {
                        Log.d("", "onGetScooter: ".concat(String.valueOf(scooters)));
                        activity.runOnUiThread(() -> {
                            if (connectionPending[0] || kugooMode[0] || !dialog.isShowing() || connect_block.getVisibility() == View.VISIBLE) return;
                            if (scooters.size() > 0) {
                                empty.setVisibility(View.GONE);
                                loading.setVisibility(View.GONE);
                                listView1.setVisibility(View.VISIBLE);
                                listView1.setAdapter(new Adapters.ListviewScootersAdapter(scooters, activity, (scooter, loading12, loaded12) -> {
                                    loading_item[0] = loading12;
                                    loaded_item[0] = loaded12;
                                    Intent i = new Intent(activity, ServiceScooter.class);
                                    i.putExtra("command", SCOOTER_CONNECT_COMMAND);
                                    i.putExtra("uuid", scooter.getUUID());
                                    i.putExtra("address", scooter.getAddress());
                                    i.putExtra("name", scooter.getName());
                                    setConnectionPending.accept(true);
                                    activity.startService(i);
                                }));
                            } else {
                                loading.setVisibility(View.GONE);
                                empty.setVisibility(View.VISIBLE);
                                listView1.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onGetError() {
                        activity.runOnUiThread(() -> {
                            if (connectionPending[0] || kugooMode[0] || !dialog.isShowing()) return;
                            blu_err.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                    }
                });
            });
        });

        getScooters(activity, new ScootersApi.getScooterCallback() {
            @Override
            public void onGetScooter(ArrayList<Scooter> scooters) {
                Log.d("", "onGetScooter: ".concat(String.valueOf(scooters)));
                activity.runOnUiThread(() -> {
                    if (connectionPending[0] || kugooMode[0] || !dialog.isShowing() || connect_block.getVisibility() == View.VISIBLE) return;
                    if (scooters.size() > 0) {
//                        for (Scooter scooter : scooters) {
//                            if (scooter.getAddress().contains(activity.getSharedPreferences("login", Activity.MODE_PRIVATE).getString("address", "null"))) {
//                                listView1.setVisibility(View.GONE);
//                                Intent i = new Intent(activity, ServiceScooter.class);
//                                i.putExtra("command", SCOOTER_CONNECT_COMMAND);
//                                i.putExtra("uuid", scooter.getUUID());
//                                i.putExtra("address", scooter.getAddress());
//                                i.putExtra("name", scooter.getName());
//                                activity.startService(i);
//                            }
//                        }
                        empty.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        listView1.setVisibility(View.VISIBLE);
                        listView1.setAdapter(new Adapters.ListviewScootersAdapter(scooters, activity, (scooter, loading13, loaded14) -> {
                            loading_item[0] = loading13;
                            loaded_item[0] = loaded14;
                            Intent i = new Intent(activity, ServiceScooter.class);
                            i.putExtra("command", SCOOTER_CONNECT_COMMAND);
                            i.putExtra("uuid", scooter.getUUID());
                            i.putExtra("address", scooter.getAddress());
                            i.putExtra("name", scooter.getName());
                            setConnectionPending.accept(true);
                            activity.startService(i);
                        }));
                    } else {
                        loading.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        listView1.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onGetError() {
                activity.runOnUiThread(() -> {
                    if (connectionPending[0] || kugooMode[0] || !dialog.isShowing()) return;
                    blu_err.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                });
            }
        });

    }

    private static void openOfflineKeys(AppCompatActivity activity) {
        Intent i = new Intent(activity, OfflineKeysActivity.class);
        activity.startActivity(i);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static class ThingScooterAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final ArrayList<sc.denishik.ru.kuggoApi.Scooter> scooters;
        private final HashMap<String, Bitmap> iconCache = new HashMap<>();
        private final Handler main = new Handler(Looper.getMainLooper());

        ThingScooterAdapter(Context context, ArrayList<sc.denishik.ru.kuggoApi.Scooter> scooters) {
            this.inflater = LayoutInflater.from(context);
            this.scooters = scooters == null ? new ArrayList<>() : scooters;
        }

        @Override
        public int getCount() {
            return scooters.size();
        }

        @Override
        public Object getItem(int position) {
            return scooters.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_thing_scooter, parent, false);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.title = convertView.findViewById(R.id.title);
                holder.subtitle = convertView.findViewById(R.id.subtitle);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            sc.denishik.ru.kuggoApi.Scooter s = scooters.get(position);
            String title = s.getDisplayName();
            if (s.isBinded()) title = title + " (binded)";
            holder.title.setText(title);

            String sub = s.getAddress();
            if (sub == null || sub.isEmpty()) sub = s.getDevId();
            holder.subtitle.setText(sub == null ? "" : sub);

            String iconUrl = s.getIconUrl();
            if (iconUrl == null || iconUrl.trim().isEmpty()) {
                holder.icon.setImageResource(android.R.drawable.ic_menu_search);
                return convertView;
            }

            Bitmap cached = iconCache.get(iconUrl);
            if (cached != null) {
                holder.icon.setImageBitmap(cached);
                return convertView;
            }

            holder.icon.setImageResource(android.R.drawable.ic_menu_gallery);
            new Thread(() -> {
                try {
                    Bitmap bmp = BitmapFactory.decodeStream(new URL(iconUrl).openStream());
                    if (bmp != null) {
                        iconCache.put(iconUrl, bmp);
                        main.post(this::notifyDataSetChanged);
                    }
                } catch (Throwable ignored) {
                }
            }).start();

            return convertView;
        }

        private static class ViewHolder {
            ImageView icon;
            TextView title;
            TextView subtitle;
        }
    }
}
