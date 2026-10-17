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
    private static final int PICK_LOCATION_ICON = 851;
    @Override public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_LOCATION_ICON && resultCode == android.app.Activity.RESULT_OK
                && data != null && data.getData() != null && getActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).importLocationIcon(data.getData());
        }
    }
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
        View authorLink = view.findViewById(R.id.kurwa_author_link);
        if ("sc.kurwa.ru".equals(sc.denishik.ru.BuildConfig.APPLICATION_ID)) {
            // This asset is supplied only by build-kurwa.ps1, not the regular APK.
            int authorImage = getResources().getIdentifier("kurwa_author", "drawable", requireContext().getPackageName());
            if (authorImage != 0) {
                ((android.widget.ImageView) view.findViewById(R.id.kurwa_author_image)).setImageResource(authorImage);
                authorLink.setVisibility(View.VISIBLE);
                authorLink.setOnClickListener(v -> {
                    try {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.tiktok.com/@f3proma_ai")));
                    } catch (android.content.ActivityNotFoundException | SecurityException error) {
                        android.widget.Toast.makeText(requireContext(), R.string.author_link_unavailable,
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
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

        HomeActivity home = (HomeActivity) requireActivity();
        view.findViewById(R.id.disconnect_card).setOnClickListener(v -> home.disconnectScooter());
        View mapCard = view.findViewById(R.id.map_card);
        View travelCard = view.findViewById(R.id.travel_card);
        View gpsCard = view.findViewById(R.id.gps_icon_card);
        android.widget.TextView mapLabel = view.findViewById(R.id.map_card_label);
        android.widget.TextView travelLabel = view.findViewById(R.id.travel_card_label);
        android.widget.ImageView travelIcon = view.findViewById(R.id.travel_card_icon);
        android.widget.TextView hint = view.findViewById(R.id.map_provider_hint);
        com.google.android.material.tabs.TabLayout tabs = view.findViewById(R.id.travel_tabs);
        sc.denishik.ru.mapsApi.TravelMode[] modes = sc.denishik.ru.mapsApi.TravelMode.values();
        int[] icons = {R.drawable.ic_route_scooter, R.drawable.ic_route_bike, R.drawable.ic_route_walk, R.drawable.ic_route_car};
        String[] labels = {"Самокат", "Вело", "Пешком", "Авто"};
        for (int i = 0; i < modes.length; i++) {
            tabs.addTab(tabs.newTab().setText(labels[i]).setIcon(icons[i]).setContentDescription(modes[i].label), false);
        }
        tabs.selectTab(tabs.getTabAt(home.travelMode().ordinal()));
        View speedometerCard = view.findViewById(R.id.speedometer_card);
        android.widget.GridLayout grid = view.findViewById(R.id.map_cards_grid);
        Runnable arrange = () -> {
            int width = grid.getWidth();
            if (width == 0) return;
            int gap = (int)(5 * getResources().getDisplayMetrics().density);
            int index = 0;
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                if (child.getVisibility() == View.GONE) continue;
                android.widget.GridLayout.LayoutParams lp = (android.widget.GridLayout.LayoutParams) child.getLayoutParams();
                int col = index % 3;
                lp.columnSpec = android.widget.GridLayout.spec(col); lp.rowSpec = android.widget.GridLayout.spec(index / 3);
                lp.width = (width - 2 * gap) / 3; lp.height = (int)(70 * getResources().getDisplayMetrics().density);
                lp.setMargins(col == 0 ? 0 : gap, index < 3 ? 0 : gap * 2, 0, 0);
                child.setLayoutParams(lp); index++;
            }
        };
        grid.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> { if(r-l != or-ol) grid.post(arrange); });
        speedometerCard.setOnClickListener(v -> home.showSpeedometer(true));
        Runnable refresh = () -> {
            boolean yandex = "yandex".equals(home.mapProvider());
            boolean hidden = "none".equals(home.mapProvider());
            mapLabel.setText(hidden ? "Без карты" : yandex ? "Яндекс" : "2ГИС");
            mapCard.setContentDescription("Карта: " + (hidden ? "Скрыта. Включить Яндекс" : yandex ? "Яндекс. Переключить на 2ГИС" : "2ГИС. Скрыть карту"));

            travelCard.setVisibility(yandex ? View.VISIBLE : View.GONE);
            gpsCard.setVisibility(yandex ? View.VISIBLE : View.GONE);
            if (!yandex) tabs.setVisibility(View.GONE);
            grid.post(arrange);
            travelLabel.setText(home.travelMode().label);
            travelIcon.setImageResource(icons[home.travelMode().ordinal()]);
            travelCard.setContentDescription("Тип маршрута: " + home.travelMode().label + ". Изменить");
            gpsCard.setContentDescription("Изменить иконку GPS на карте");
            hint.setText(hidden ? "Карта скрыта • нажмите «Без карты», чтобы включить" : yandex ? "Карта • тип маршрута • ваша иконка GPS" : "2ГИС • веб-карта, нужен интернет");
        };
        mapCard.setOnClickListener(v -> { home.selectMap("yandex".equals(home.mapProvider()) ? "dgis" : "dgis".equals(home.mapProvider()) ? "none" : "yandex"); refresh.run(); });
        travelCard.setOnClickListener(v -> {
            tabs.setVisibility(tabs.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            view.requestLayout();
        });
        tabs.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                home.selectTravelMode(modes[tab.getPosition()]); refresh.run();
            }
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) { }
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) { }
        });
        gpsCard.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Иконка GPS")
                .setItems(new String[]{"Стрелка", "Самокат", "Выбрать свою картинку…"}, (dialog, which) -> {
                    if (which == 2) {
                        android.content.Intent pick = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT)
                                .addCategory(android.content.Intent.CATEGORY_OPENABLE).setType("image/*");
                        startActivityForResult(pick, PICK_LOCATION_ICON);
                    }
                    else home.selectLocationIcon(which == 0 ? "arrow" : "scooter");
                }).setNegativeButton("Отмена", null).show());
        refresh.run();
        return view;
    }
}
