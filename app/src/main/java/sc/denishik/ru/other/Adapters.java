package sc.denishik.ru.other;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import sc.denishik.ru.R;
import sc.denishik.ru.midwayApi.Scooter;
import sc.denishik.ru.pages.HomePage;
import sc.denishik.ru.pages.InfoView;
import sc.denishik.ru.pages.LedPage;
import sc.denishik.ru.pages.RemoteView;

public class Adapters {
    static public class ListviewScootersAdapter extends BaseAdapter {
        public interface ListCallback<T> {
            void onClick(Scooter scooter, LinearLayout loading, LinearLayout loaded);
        }

        ArrayList<Scooter> _data;
        AppCompatActivity _activity;
        ListCallback _callback;

        public ListviewScootersAdapter(ArrayList<Scooter> _arr, AppCompatActivity activity, ListCallback callback) {
            _data = _arr;
            _activity = activity;
            _callback = callback;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public Scooter getItem(int _index) {
            return _data.get(_index);
        }

        @Override
        public long getItemId(int _index) {
            return _index;
        }

        @Override
        public View getView(final int _position, View _v, ViewGroup _container) {
            LayoutInflater _inflater = _activity.getLayoutInflater();
            View _view = _v;
            if (_view == null) {
                _view = _inflater.inflate(R.layout.connect_item, null);
            }

            final LinearLayout linear1 = _view.findViewById(R.id.linear1);
            final LinearLayout loading = _view.findViewById(R.id.loading);
            final TextView name = _view.findViewById(R.id.name);
            final TextView des = _view.findViewById(R.id.des);

            linear1.setOnClickListener(v -> {
                loading.setVisibility(View.VISIBLE);
                linear1.setVisibility(View.GONE);
                _callback.onClick(_data.get(_position), loading, linear1);
            });

            name.setText(String.valueOf(_data.get(_position).getName()));
            des.setText(_data.get(_position).getUUID());

            return _view;
        }
    }

    public static class ViewsAdapter extends FragmentPagerAdapter {

        private HomePage homePage ;
        private RemoteView remotePage ;
        private InfoView infoPage ;
        private LedPage ledPage ;


        public ViewsAdapter(FragmentManager fm, Context context, AppCompatActivity activity) {
            super(fm);

            homePage = new HomePage();
            remotePage = new RemoteView();
            infoPage = new InfoView(activity);
            ledPage = new LedPage(activity);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return homePage;
                case 1:
                    return remotePage;
                case 2:
                    return infoPage;
                case 3:
                    return ledPage;
                default:
                    return null;

            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
