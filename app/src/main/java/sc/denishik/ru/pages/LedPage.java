package sc.denishik.ru.pages;

import static sc.denishik.ru.midwayApi.base.KeysBaseParam.MODE_KEY;
import static sc.denishik.ru.other.Config.SCOOTER_LED;
import static sc.denishik.ru.other.Config.SCOOTER_LED_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_LED_DISCONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_LED_RECONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_SEND_DATA_PARAMS_COMMAND;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import sc.denishik.ru.ledApi.Client;
import sc.denishik.ru.ledApi.LedInfo;
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
        ledsList = new CustomArrayList<>();
        adapter = new Adapters.ListviewLedAdapter(ledsList, activity);

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    switch (intent.getAction()) {
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
                    }
                }
            }
        };
        adapter = new Adapters.ListviewLedAdapter(ledsList, activity);
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
            if (getActivity() != null) {
                Intent i = new Intent(getContext(), ServiceScooter.class);
                i.putExtra("command", SCOOTER_LED_RECONNECT);
                getActivity().startService(i);
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
            activity.unregisterReceiver(mMessageReceiver);
        }
    }
}