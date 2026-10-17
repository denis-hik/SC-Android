package sc.denishik.ru.pages;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import sc.denishik.ru.R;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.ledApiBLU.Client;
import sc.denishik.ru.ledApiBLU.Config;
import sc.denishik.ru.ledApiWS.LedInfo;
import sc.denishik.ru.other.*;
import static sc.denishik.ru.other.Config.*;

public class LedPage extends Fragment {
    private TextView url, bleStatus;
    private ListView list;
    private ImageView bluetooth, refresh;
    private String wsAddress = "ws://";
    private final CustomArrayList<LedInfo> log = new CustomArrayList<>();
    private Adapters.ListviewLedAdapter adapter;
    public LedPage() { }
    public LedPage(AppCompatActivity activity) { }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (SCOOTER_LED.equals(intent.getAction())) {
                String address = intent.getStringExtra("url");
                if (address != null) wsAddress = address;
                String status = intent.getStringExtra("Status");
                String text = intent.getStringExtra("text");
                if (status != null) {
                    log.add(new LedInfo(text == null ? status : text,
                            SCOOTER_LED_CONNECT.equals(status) || SCOOTER_LED_TEXT.equals(status)));
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            }
            render();
        }
    };
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_led_page, container, false);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        url = view.findViewById(R.id.ws_url); bleStatus = view.findViewById(R.id.ble_status);
        list = view.findViewById(R.id.list); bluetooth = view.findViewById(R.id.bluetooth);
        refresh = view.findViewById(R.id.refresh);
        adapter = new Adapters.ListviewLedAdapter(log, activity); list.setAdapter(adapter);
        sc.denishik.ru.ledApiWS.Config.load(activity);
        wsAddress = "ws://" + sc.denishik.ru.ledApiWS.Config.IP_HOST_DEFAULT + ":"
                + sc.denishik.ru.ledApiWS.Config.PORT_CLIENT_DEFAULT + "/hub/"
                + sc.denishik.ru.ledApiWS.Config.ID_BOARD;
        view.findViewById(R.id.back).setOnClickListener(v -> ((ViewPager) activity.findViewById(R.id.viewpage)).setCurrentItem(0));
        view.findViewById(R.id.led_settings).setOnClickListener(v -> Modals.SettingsWSModal(activity));
        refresh.setOnClickListener(v -> {
            log.clear(); adapter.notifyDataSetChanged();
            activity.startService(new Intent(activity, ServiceScooter.class)
                    .putExtra("command", SCOOTER_LED_RECONNECT)
                    .putExtra("ws_retry_enabled", true));
        });
        bluetooth.setOnClickListener(v -> sc.denishik.ru.ledApiBLU.DevicesSheet.show(activity));
        render(); return view;
    }
    private void render() {
        if (url == null) return;
        sc.denishik.ru.ledApiBLU.LedConnections connections = sc.denishik.ru.ledApiBLU.LedConnections.get(requireContext());
        boolean ble = !connections.entries().isEmpty();
        url.setText(ble ? getString(R.string.led_ble_pairing) : wsAddress);
        list.setVisibility(ble ? View.GONE : View.VISIBLE);
        bleStatus.setVisibility(ble ? View.VISIBLE : View.GONE);
        ((View) refresh.getParent()).setVisibility(ble ? View.GONE : View.VISIBLE);
        bluetooth.setAlpha(ble ? 1f : 0.6f);
        if (ble) bleStatus.setText(connections.summary());
    }
    @Override public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(SCOOTER_LED); filter.addAction(Client.STATE_CHANGED);
        if (android.os.Build.VERSION.SDK_INT >= 33) requireContext().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else requireContext().registerReceiver(receiver, filter);
        render();
    }
    @Override public void onStop() { requireContext().unregisterReceiver(receiver); super.onStop(); }
    @Override public void onDestroyView() { url = null; bleStatus = null; list = null; adapter = null; super.onDestroyView(); }
}
