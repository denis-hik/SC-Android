package sc.denishik.ru.pages;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sc.denishik.ru.HomeActivity;
import sc.denishik.ru.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomePage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomePage extends Fragment {
    private CardView remote;
    private CardView info;
    private CardView leds;
    public HomePage() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static HomePage newInstance() {
        HomePage fragment = new HomePage();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        ViewPager viewPager = getActivity().findViewById(R.id.viewpage);;

        remote = view.findViewById(R.id.remote);
        info = view.findViewById(R.id.info);
        leds = view.findViewById(R.id.leds);


        remote.setOnClickListener(v -> {
            viewPager.setCurrentItem(1);
        });
        info.setOnClickListener(v -> {
            viewPager.setCurrentItem(2);
        });
        leds.setOnClickListener(v -> {
            viewPager.setCurrentItem(3);
        });

        return view;
    }
}