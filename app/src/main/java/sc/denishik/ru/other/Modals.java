package sc.denishik.ru.other;

import static sc.denishik.ru.midwayApi.ScootersApi.getScooters;
import static sc.denishik.ru.other.Config.SCOOTER_CONNECT_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_STATUS_CONNECTED;
import static sc.denishik.ru.other.Config.SCOOTER_STATUS_ERROR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Objects;

import sc.denishik.ru.HomeActivity;
import sc.denishik.ru.R;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.midwayApi.Scooter;
import sc.denishik.ru.midwayApi.ScootersApi;

public class Modals {
    public interface showPermissionCallback {
        void onRequest(com.google.android.material.bottomsheet.BottomSheetDialog dialog);
    }
    public static void showPermissionModal(AppCompatActivity activity, showPermissionCallback callback) {
        if (activity.getSharedPreferences("file", Context.MODE_PRIVATE).contains("isOpened")) {
            callback.onRequest(null);
            return;
        }
        final com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        View lay = activity.getLayoutInflater().inflate(R.layout.permisson_view, null); dialog.setContentView(lay);

        final LinearLayout linear1 = (LinearLayout)lay.findViewById(R.id.linear1);
        final LinearLayout go = lay.findViewById(R.id.go);
        final LinearLayout back = lay.findViewById(R.id.back);
        final CheckBox location = lay.findViewById(R.id.location);
        final CardView info = lay.findViewById(R.id.info);

        dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        dialog.show();
        dialog.setCancelable(false);
        android.graphics.drawable.GradientDrawable wd = new android.graphics.drawable.GradientDrawable();
        wd.setColor(Color.WHITE);
        wd.setCornerRadius((int) 10f);
        linear1.setBackground(wd);

        location.setChecked(true);
        info.setVisibility(View.VISIBLE);

        activity.getSharedPreferences("file", Context.MODE_PRIVATE).edit().putString("isLoc",  "1").apply();

        go.setOnClickListener(v -> {
            activity.getSharedPreferences("file", Context.MODE_PRIVATE).edit().putString("isOpened", "1").apply();
            callback.onRequest(dialog);
        });
        back.setOnClickListener(v -> {
            activity.finish();
        });;
        location.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.getSharedPreferences("file", Context.MODE_PRIVATE).edit().putString("isLoc", isChecked ? "1" : "0").apply();
        });

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

        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onRec", String.valueOf(intent.getAction()));
                String s = intent.getStringExtra("Status");
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
                } else if (Objects.equals(s, SCOOTER_STATUS_ERROR)) {
                    loading_item[0].setVisibility(View.GONE);
                    loaded_item[0].setVisibility(View.VISIBLE);
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

        connect.setOnLongClickListener(v -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setClass(activity.getApplicationContext(), HomeActivity.class);
            activity.startActivity(i);
            activity.finish();

            return false;
        });

        refresh.setOnClickListener(v -> {
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
                    activity.runOnUiThread( () -> {
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
                                i.putExtra("name_device", scooter.getDevice().getName());
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
                    activity.runOnUiThread( () -> {
                        blu_err.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    });
                }
            });
        });

        connect.setOnClickListener(v -> {
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
                        activity.runOnUiThread( () -> {
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
                        activity.runOnUiThread( () -> {
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
                activity.runOnUiThread( () -> {
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
                activity.runOnUiThread( () -> {
                    blu_err.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                });
            }
        });

        dialog.setOnDismissListener(dialog1 -> {
            activity.unregisterReceiver(mMessageReceiver);
        });


    }
}
