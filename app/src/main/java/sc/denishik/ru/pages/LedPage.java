package sc.denishik.ru.pages;

import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;

import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sc.denishik.ru.R;
import sc.denishik.ru.ledApi.Client;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LedPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LedPage extends Fragment {

    private TextView ws_url;
    private ImageView back;
    private Client client;

    public LedPage() {
        // Required empty public constructor
    }


    public static LedPage newInstance() {
        LedPage fragment = new LedPage();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new Client((AppCompatActivity) getActivity(), new Client.CallBack() {
            @Override
            public void onGetText(String s) {

            }

            @Override
            public void onError(String err) {

            }
            @Override
            public void onStop() {

            }
            @Override
            public void onOpen() {
                ws_url.setText(client.getUrl());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_led_page, container, false);
        ViewPager viewPager = getActivity().findViewById(R.id.viewpage);;

        ws_url = view.findViewById(R.id.ws_url);
        back = view.findViewById(R.id.back);

        back.setOnClickListener(v -> {
            viewPager.setCurrentItem(0);
        });

        return view;
    }

    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        if (isvisible) {

        } else {
            if (client == null) {
                return;
            }
           client.stop();
        }
    }
}