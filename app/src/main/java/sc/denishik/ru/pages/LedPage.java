package sc.denishik.ru.pages;

import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import sc.denishik.ru.R;
import sc.denishik.ru.ledApi.Client;
import sc.denishik.ru.ledApi.LedInfo;
import sc.denishik.ru.midwayApi.base.BaseParams;
import sc.denishik.ru.other.Adapters;
import sc.denishik.ru.other.CustomArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LedPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LedPage extends Fragment {

    private TextView ws_url;
    private float speed_old = 0;
    private boolean isPArking_old = false;
    private ImageView back;
    private ImageView refresh;
    private Client client;
    private boolean isConnect = false;
    private boolean isSend = false;
    private AppCompatActivity activity;
    private CustomArrayList<LedInfo> ledsList;
    private ListView list;
    private BroadcastReceiver mMessageReceiver;
    private Client.CallBack callbackWS;
    private Adapters.ListviewLedAdapter adapter;

    public LedPage(AppCompatActivity activity) {
        this.activity = activity;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onRec", String.valueOf(intent.getAction()));
                HashMap<String, Object> temp = (HashMap<String, Object>) intent.getSerializableExtra("data");
                if (temp != null && isConnect) {
                    BaseParams params = new BaseParams(temp);
                    float speed = params.getSpeed();
                    if (speed > 30) {
                        speed = 29;
                    }
                    boolean isParking = params.getLockSw();
                    if (speed_old != speed) {
                        speed_old = speed;
                        client.sendWS("_n9", String.valueOf(((int) speed) * 8.5));
                    }
                    if (isPArking_old != isParking) {
                        isPArking_old = isParking;
                        client.sendWS("_n2", !isParking ? "3" : "0");
                    }
                }
            }
        };
        ledsList = new CustomArrayList<>();
        adapter = new Adapters.ListviewLedAdapter(ledsList, activity);
        callbackWS = new Client.CallBack() {
            @Override
            public void onGetText(String s) {
                activity.runOnUiThread(() -> {
                    ledsList.add(new LedInfo("get string ws>".concat(String.valueOf(s)), true));
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String err) {
                isConnect = false;
                try {
                    ledsList.add(new LedInfo(String.valueOf(err), false));
                    adapter.notifyDataSetChanged();
                    activity.unregisterReceiver(mMessageReceiver);
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onDiscovered() {
                activity.runOnUiThread(() -> {
                    ledsList.add(new LedInfo("Discovering...", false));
                    adapter.notifyDataSetChanged();
                    ws_url.setText(client.getUrl());
                    client.connectServer();
                });
            }
            @Override
            public void onStop() {
                isConnect = false;
                ledsList.add(new LedInfo("Stop ws", false));
                adapter.notifyDataSetChanged();
                try {

                    activity.unregisterReceiver(mMessageReceiver);
                } catch (Exception ignored) {

                }
            }
            @Override
            public void onOpen() {
                activity.runOnUiThread(() -> {
                    ledsList.add(new LedInfo("Open ws", true));
                    adapter.notifyDataSetChanged();
                    if (!isSend) {
                        activity.registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_GET_DATA_PARAMS_COMMAND));
                        isSend = true;
                    }
                    ws_url.setText(client.getUrl());
                    isConnect = true;
                });
            }
        };
        ledsList.add(new LedInfo("connecting ws...", false));
        adapter.notifyDataSetChanged();
        client = new Client(activity, callbackWS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_led_page, container, false);
        ViewPager viewPager = activity.findViewById(R.id.viewpage);;

        ws_url = view.findViewById(R.id.ws_url);
        back = view.findViewById(R.id.back);
        refresh = view.findViewById(R.id.refresh);
        list = view.findViewById(R.id.list);
        list.setAdapter(adapter);

        back.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
        });

        refresh.setOnClickListener(v -> {
            try {
                client.stop();
                activity.unregisterReceiver(mMessageReceiver);
            } catch (Exception e) {

            }
            isConnect = false;
            isSend = false;
            ledsList = new CustomArrayList<>();
            adapter = new Adapters.ListviewLedAdapter(ledsList, activity);
            list.setAdapter(adapter);
            ledsList.add(new LedInfo("Refreshing", false));
            adapter.notifyDataSetChanged();
            client = new Client(activity, callbackWS);
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
        client.stop();
    }

    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        if (isvisible) {

        } else {
//            if (client == null) {
//                return;
//            }
//           client.stop();
        }
    }
}