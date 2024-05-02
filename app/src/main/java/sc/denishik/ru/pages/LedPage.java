package sc.denishik.ru.pages;

import static sc.denishik.ru.ledApiBLU.Config.setDefaultClient;
import static sc.denishik.ru.other.Config.SCOOTER_LED;
import static sc.denishik.ru.other.Config.SCOOTER_LED_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_LED_DISCONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_LED_ERROR;
import static sc.denishik.ru.other.Config.SCOOTER_LED_RECONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_LED_TEXT;
import static sc.denishik.ru.other.Modals.showModalLedsBluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import sc.denishik.ru.R;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.ledApiBLU.Client;
import sc.denishik.ru.ledApiWS.LedInfo;
import sc.denishik.ru.other.Adapters;
import sc.denishik.ru.other.CustomArrayList;
import sc.denishik.ru.other.Modals;

public class LedPage extends Fragment {

    private TextView ws_url;
    private float speed_old = 0;
    private boolean isPArking_old = false;
    private ImageView back;
    private ImageView refresh;
    private ImageView bluetooth;
    private boolean isConnect = false;
    private boolean isSend = false;
    private AppCompatActivity activity;
    private CustomArrayList<LedInfo> ledsList;
    private ListView list;
    private BroadcastReceiver mMessageReceiver;
    private Adapters.ListviewLedAdapter adapter;
    private String TAG = "LedPage";

    private Client clientBL;

    public LedPage(AppCompatActivity activity) {
        this.activity = activity;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledsList = new CustomArrayList<>();
        adapter = new Adapters.ListviewLedAdapter(ledsList, activity);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    String s = intent.getStringExtra("Status");
                    String url = intent.getStringExtra("url");
                    boolean bl = intent.getBooleanExtra("bl", false);

                    bluetooth.setBackgroundColor(clientBL != null ? Color.parseColor("#FFA7E6A9") : activity.getResources().getColor(R.color.colorControlHighlight  ));

                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            ws_url.setText(url);
                        });
                    }
                    switch (s) {
                        case SCOOTER_LED_CONNECT:
                            if (activity != null) {
                                activity.runOnUiThread(() -> {
                                    ledsList.add(new LedInfo("Connected", true));
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                            break;
                        case SCOOTER_LED_DISCONNECT:
                            if (activity != null) {
                                activity.runOnUiThread(() -> {
                                    ledsList.add(new LedInfo("Disconected", false));
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                            break;
                        case SCOOTER_LED_TEXT:
                            if (activity != null) {
                                String text = intent.getStringExtra("text");
                                activity.runOnUiThread(() -> {
                                    ledsList.add(new LedInfo(String.valueOf(text), true));
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        case SCOOTER_LED_ERROR:
                            if (activity != null) {
                                String text = intent.getStringExtra("text");
                                activity.runOnUiThread(() -> {
                                    ledsList.add(new LedInfo(String.valueOf(text), false));
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                    }
                }
            }
        };
        adapter = new Adapters.ListviewLedAdapter(ledsList, activity);
        activity.registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_LED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_led_page, container, false);
        ViewPager viewPager = activity.findViewById(R.id.viewpage);;

        ws_url = view.findViewById(R.id.ws_url);
        back = view.findViewById(R.id.back);
        refresh = view.findViewById(R.id.refresh);
        bluetooth = view.findViewById(R.id.bluetooth);
        list = view.findViewById(R.id.list);
        list.setAdapter(adapter);

        back.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
        });

        refresh.setOnClickListener(v -> {
            if (getActivity() != null) {
                ledsList = new CustomArrayList<>();
                adapter.notifyDataSetChanged();
                Intent i = new Intent(getContext(), ServiceScooter.class);
                i.putExtra("command", SCOOTER_LED_RECONNECT);
                getActivity().startService(i);
            }
        });

        bluetooth.setOnClickListener(v -> {
            if (clientBL != null) {
                clientBL.unSelectDevice();
                clientBL = null;
                setDefaultClient(null);
            } else {
                showModalLedsBluetooth(activity, client -> {
                    clientBL = client;
                });
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            activity.unregisterReceiver(mMessageReceiver);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        if (isvisible) {
            activity.registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_LED));
        } else {
            if (mMessageReceiver != null) {
                try {
                    activity.unregisterReceiver(mMessageReceiver);
                } catch (Exception ignored) {

                }
            }
        }
    }
}