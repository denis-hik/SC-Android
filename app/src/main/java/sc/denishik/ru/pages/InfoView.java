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
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

import sc.denishik.ru.R;
import sc.denishik.ru.midwayApi.base.BaseParams;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoView extends Fragment {

    private BroadcastReceiver mMessageReceiver;
    private BaseParams params;
    private TextView data;
    private Button back;
    private AppCompatActivity activity;

    public InfoView(AppCompatActivity activity) {
        this.activity = activity;
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
                        data.setText(params.toString());
//                        parking_image.setImageDrawable(getActivity().getDrawable(!params.getLockSw() ? R.drawable.parking : R.drawable.parking_off));
//                        light_image.setImageDrawable(getActivity().getDrawable(params.getHeadLightSw() ? R.drawable.headlight : R.drawable.headlight_off));
                    }
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_view, container, false);

        data = view.findViewById(R.id.data);
        back = view.findViewById(R.id.back);

        back.setOnClickListener(v -> {
            ViewPager viewPager = activity.findViewById(R.id.viewpage);;
            viewPager.setCurrentItem(0);
        });

        return view;
    }

    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        if (isvisible) {
            try {

                activity.registerReceiver(mMessageReceiver, new IntentFilter(SCOOTER_GET_DATA_PARAMS_COMMAND));
            } catch (Exception e) {

            }
        } else {
            try {

                activity.unregisterReceiver(mMessageReceiver);
            } catch (Exception e) {

            }
        }
    }
}