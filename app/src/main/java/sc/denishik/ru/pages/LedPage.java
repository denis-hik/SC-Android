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
import android.widget.TextView;

import java.util.HashMap;

import sc.denishik.ru.R;
import sc.denishik.ru.ledApi.Client;
import sc.denishik.ru.midwayApi.base.BaseParams;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LedPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LedPage extends Fragment {

    private TextView ws_url;
    private float speed_old = 0;
    private ImageView back;
    private ImageView refresh;
    private Client client;
    private boolean isConnect = false;
    private boolean isSend = false;
    private AppCompatActivity activity;
    private BroadcastReceiver mMessageReceiver;
    private Client.CallBack callbackWS;

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
                    float speed = new BaseParams(temp).getSpeed();
                    if (speed_old != speed) {
                        speed_old = speed;
                        client.sendWS("_n9", String.valueOf(((int) speed) * 30));
                    }
                }
            }
        };
        callbackWS = new Client.CallBack() {
            @Override
            public void onGetText(String s) {
                activity.runOnUiThread(() -> {

                });
            }

            @Override
            public void onError(String err) {
                isConnect = false;
                try {

                    activity.unregisterReceiver(mMessageReceiver);
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onDiscovered() {
                activity.runOnUiThread(() -> {
                    ws_url.setText(client.getUrl());
                    client.connectServer();
                });
            }
            @Override
            public void onStop() {
                isConnect = false;
                try {

                    activity.unregisterReceiver(mMessageReceiver);
                } catch (Exception ignored) {

                }
            }
            @Override
            public void onOpen() {
                activity.runOnUiThread(() -> {
                    if (!isSend) {
                        activity.registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_GET_DATA_PARAMS_COMMAND));
                        isSend = true;
                    }
                    ws_url.setText(client.getUrl());
                    isConnect = true;
                });
            }
        };
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