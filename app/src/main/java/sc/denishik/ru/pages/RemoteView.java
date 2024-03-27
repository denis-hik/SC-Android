package sc.denishik.ru.pages;

import static sc.denishik.ru.midwayApi.base.KeysBaseParam.LIGHT_KEY;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.LOCK_KEY;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.MAX_KEY;
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
    private ImageView parking_image;
    private ImageView light_image;
    private Button back;
    private Button max20;
    private Button max30;
    private Button gear0;
    private Button gear1;
    private Button gear2;
    private Button showAdvParams;
    private Button disableAdvParams;
    private TextView max_text;
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
                Log.d("onRec", String.valueOf(intent.getAction()));
                HashMap<String, Object> temp = (HashMap<String, Object>) intent.getSerializableExtra("data");
                if (temp != null) {
                    boolean isReDraw = false;
                    if (params != null) {
                        isReDraw = params.equals(new BaseParams(temp));
                    } else {
                        isReDraw = true;
                    }
                    params = new BaseParams(temp);
                    if (isReDraw) {
                        parking_image.setImageDrawable(getActivity().getDrawable(!params.getLockSw() ? R.drawable.parking : R.drawable.parking_off));
                        light_image.setImageDrawable(getActivity().getDrawable(params.getHeadLightSw() ? R.drawable.headlight : R.drawable.headlight_off));
                        max_text.setText(String.valueOf(params.getLimitMode3()));
                    }
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View _view = inflater.inflate(R.layout.fragment_remote_view, container, false);

        parking = _view.findViewById(R.id.parking);
        parking_image = _view.findViewById(R.id.parking_image);
        light_image = _view.findViewById(R.id.light_image);
        light = _view.findViewById(R.id.light);
        max_text = _view.findViewById(R.id.max_text);
        back = _view.findViewById(R.id.back);
        max20 = _view.findViewById(R.id.max20);
        max30 = _view.findViewById(R.id.max30);
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
            parking_image.setImageDrawable(getActivity().getDrawable(params.getLockSw() ? R.drawable.parking : R.drawable.parking_off));
            handleParking();
        });
        light.setOnClickListener(v -> {
            light_image.setImageDrawable(getActivity().getDrawable(!params.getHeadLightSw() ? R.drawable.headlight : R.drawable.headlight_off));
            handleLight();
        });
        back.setOnClickListener(v -> {
            ViewPager viewPager = getActivity().findViewById(R.id.viewpage);;
            viewPager.setCurrentItem(0);
        });
        max20.setOnClickListener(v -> {
            handleMaxSpeed((int) 30);
        });
        max30.setOnClickListener(v -> {
            handleMaxSpeed((int) 40);
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

    private void handleLight() {
        Intent i = new Intent(getContext(), ServiceScooter.class);
        i.putExtra("command", SCOOTER_SEND_DATA_PARAMS_COMMAND);
        // You can also include some extra data.
        i.putExtra("key", LIGHT_KEY);
        i.putExtra("value",  true);
        getActivity().startService(i);
    }
    private void handleMaxSpeed(int speed) {
        Intent i = new Intent(getContext(), ServiceScooter.class);
        i.putExtra("command", SCOOTER_SEND_DATA_PARAMS_COMMAND);
        // You can also include some extra data.
        i.putExtra("key", MAX_KEY);
        i.putExtra("value",  speed);
        getActivity().startService(i);
    }
    private void handleModeGear(int mode) {
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

            getActivity().registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_GET_DATA_PARAMS_COMMAND));
        } else {
            try {

                getActivity().unregisterReceiver(mMessageReceiver);
            } catch (Exception e) {

            }
        }
    }
}