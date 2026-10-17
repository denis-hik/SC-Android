package sc.denishik.ru.pages;

import static sc.denishik.ru.midwayApi.base.KeysBaseParam.LIGHT_KEY;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.LOCK_KEY;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.MODE_KEY;
import static sc.denishik.ru.other.Config.SCOOTER_ADV_PARAMS_DISABLE;
import static sc.denishik.ru.other.Config.SCOOTER_ADV_PARAMS_SHOW;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_SEND_DATA_PARAMS_COMMAND;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import java.util.HashMap;
import java.util.Objects;

import sc.denishik.ru.HomeActivity;
import sc.denishik.ru.R;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.midwayApi.base.BaseParams;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RemoteView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoteView extends Fragment {
    private CardView parking;
    private CardView light;
    private BaseParams params;
    private boolean kugooDevice;
    private boolean midwayConnected;
    private boolean noParking;
    private boolean canToggleParking;
    private boolean canSetNoParking;
    private View gearControls;
    private ImageView parking_image;
    private ImageView light_image;
    private Button back;
    private Button gear0;
    private Button gear1;
    private Button gear2;
    private Button showAdvParams;
    private Button disableAdvParams;
    private BroadcastReceiver mMessageReceiver;

    public RemoteView() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static RemoteView newInstance() {
        RemoteView fragment = new RemoteView();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (parking == null || light == null) return;
                if (intent.hasExtra("command_error")) {
                    android.widget.Toast.makeText(context, intent.getStringExtra("command_error"), android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                if (sc.denishik.ru.other.Config.SCOOTER_CONNECT_COMMAND.equals(intent.getAction())
                        || sc.denishik.ru.other.Config.SCOOTER_ERROR_CONNECT.equals(intent.getAction())) {
                    midwayConnected = false;
                    gearControls.setVisibility(View.GONE);
                    return;
                }
                midwayConnected = "midway".equals(intent.getStringExtra("source"))
                        && intent.getBooleanExtra("telemetry_online", false);
                kugooDevice = "kugoo".equals(intent.getStringExtra("source"));
                noParking = kugooDevice && intent.getBooleanExtra("no_parking", false);
                canToggleParking = intent.getBooleanExtra("can_toggle_parking", false);
                canSetNoParking = intent.getBooleanExtra("can_set_no_parking", false);
                boolean controlsEnabled = !kugooDevice;
                gearControls.setVisibility(midwayConnected ? View.VISIBLE : View.GONE);
                parking.setEnabled(controlsEnabled || canToggleParking || canSetNoParking || noParking);
                light.setEnabled(controlsEnabled || intent.getBooleanExtra("can_toggle_headlight", false));
                gear0.setEnabled(midwayConnected);
                gear1.setEnabled(midwayConnected);
                gear2.setEnabled(midwayConnected);
                Log.d("onRec", String.valueOf(intent.getAction()));
                HashMap<String, Object> temp = (HashMap<String, Object>) intent.getSerializableExtra("data");
                if (temp != null) {
                    boolean isReDraw = false;
                    if (params != null) {
                        isReDraw = !params.equals(new BaseParams(temp));
                    } else {
                        isReDraw = true;
                    }
                    params = new BaseParams(temp);
                    if (isReDraw) {
                        light_image.setImageDrawable(getActivity().getDrawable(params.getHeadLightSw() ? R.drawable.headlight : R.drawable.headlight_off));
                    }
                    renderParking();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View _view = inflater.inflate(R.layout.fragment_remote_view, container, false);

        midwayConnected = false;
        gearControls = _view.findViewById(R.id.gear_controls);
        gearControls.setVisibility(midwayConnected ? View.VISIBLE : View.GONE);

        parking = _view.findViewById(R.id.parking);
        parking.setEnabled(false);
        parking_image = _view.findViewById(R.id.parking_image);
        light_image = _view.findViewById(R.id.light_image);
        light = _view.findViewById(R.id.light);
        light.setEnabled(false);
        back = _view.findViewById(R.id.back);
        gear0 = _view.findViewById(R.id.gear0);
        gear1 = _view.findViewById(R.id.gear1);
        gear2 = _view.findViewById(R.id.gear2);
//        showAdvParams = _view.findViewById(R.id.showAdvParams);
//        disableAdvParams = _view.findViewById(R.id.disableAdvParams);
//
//        showAdvParams.setOnClickListener(v -> {
//            handleAdvParamsMode(true);
//        });
//        disableAdvParams.setOnClickListener(v -> {
//            handleAdvParamsMode(false);
//        });
        parking.setOnClickListener(v -> {
            if (params == null) return;
            if (kugooDevice && (noParking || !canToggleParking)) return;
            if (!kugooDevice) parking_image.setImageDrawable(getActivity().getDrawable(params.getLockSw() ? R.drawable.parking : R.drawable.parking_off));
            handleParking();
        });
        parking.setOnLongClickListener(v -> {
            if (!kugooDevice || (!canSetNoParking && !noParking)) return false;
            Intent command = new Intent(requireContext(), ServiceScooter.class);
            command.putExtra("command", SCOOTER_SEND_DATA_PARAMS_COMMAND);
            command.putExtra("key", LOCK_KEY);
            command.putExtra("toggle_no_parking", true);
            requireContext().startService(command);
            return true;
        });
        light.setOnClickListener(v -> {
            if (params == null) return;
            handleLight();
        });
        back.setOnClickListener(v -> {
            ViewPager viewPager = getActivity().findViewById(R.id.viewpage);;
            viewPager.setCurrentItem(0);
        });
        gear0.setOnClickListener(v -> {
            handleModeGear(0);
        });
        gear1.setOnClickListener(v -> {
            handleModeGear(1);
        });
        gear2.setOnClickListener(v -> {
            handleModeGear(2);
        });

        return _view;
    }

    private void handleParking() {
        Intent i = new Intent(getContext(), ServiceScooter.class);
        i.putExtra("command", SCOOTER_SEND_DATA_PARAMS_COMMAND);
        // You can also include some extra data.
        i.putExtra("key", LOCK_KEY);
        i.putExtra("value", true);
        getActivity().startService(i);
    }

    private void renderParking() {
        if (noParking) {
            parking_image.setImageResource(R.drawable.parking_blocked);
            parking.setContentDescription("Без паркинга. Удерживайте, чтобы выключить режим");
        } else {
            boolean on = kugooDevice ? params.getLockSw() : !params.getLockSw();
            parking_image.setImageResource(on ? R.drawable.parking : R.drawable.parking_off);
            parking.setContentDescription(on ? "Паркинг включён" : "Паркинг выключен");
        }
    }

    private void handleLight() {
        Intent i = new Intent(getContext(), ServiceScooter.class);
        i.putExtra("command", SCOOTER_SEND_DATA_PARAMS_COMMAND);
        // You can also include some extra data.
        i.putExtra("key", LIGHT_KEY);
        i.putExtra("value",  true);
        getActivity().startService(i);
    }
    private void handleModeGear(int mode) {
        if (!midwayConnected) return;
        Intent i = new Intent(getContext(), ServiceScooter.class);
        i.putExtra("command", SCOOTER_SEND_DATA_PARAMS_COMMAND);
        // You can also include some extra data.
        i.putExtra("key", MODE_KEY);
        i.putExtra("value",  mode);
        getActivity().startService(i);
    }
    private  void handleAdvParamsMode(boolean isMode) {
        Intent i = new Intent(getContext(), ServiceScooter.class);
        Log.d("", "handleAdvParamsMode: ".concat(String.valueOf(isMode)));
        i.putExtra("command", isMode ? SCOOTER_ADV_PARAMS_SHOW : SCOOTER_ADV_PARAMS_DISABLE);
        getActivity().startService(i);
    }

    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        if (isvisible) {

            midwayConnected = false;
            if (gearControls != null) gearControls.setVisibility(View.GONE);
            IntentFilter filter = new IntentFilter(SCOOTER_GET_DATA_PARAMS_COMMAND);
            filter.addAction(sc.denishik.ru.other.Config.SCOOTER_CONNECT_COMMAND);
            filter.addAction(sc.denishik.ru.other.Config.SCOOTER_ERROR_CONNECT);
            getActivity().registerReceiver(mMessageReceiver, filter);
            Intent snapshot = new Intent(getActivity(), ServiceScooter.class);
            snapshot.putExtra("command", sc.denishik.ru.other.Config.SCOOTER_GET_DATA_NAME_COMMAND);
            getActivity().startService(snapshot);
        } else {
            try {

                getActivity().unregisterReceiver(mMessageReceiver);
            } catch (Exception e) {

            }
        }
    }
}
