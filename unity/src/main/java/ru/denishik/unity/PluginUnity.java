package ru.denishik.unity;

import android.app.Activity;

import java.util.ArrayList;

import ru.denishik.unity.midwayApi.Scooter;
import ru.denishik.unity.midwayApi.ScootersApi;

public class PluginUnity {
    private static Activity unityActivity;
    static ArrayList<Scooter> scooters;

    public static void receiveUnityActivity(Activity tActivity) {
        unityActivity = tActivity;
    }

    public static void fetchScooters() {
        ru.denishik.unity.midwayApi.ScootersApi.getScooters(unityActivity, new ScootersApi.getScooterCallback() {
            @Override
            public void onGetScooter(ArrayList<Scooter> result) {
                scooters = result;
            }

            @Override
            public void onGetError() {

            }
        });
    }

    public static ArrayList<Scooter> getScooters() {
        return scooters;
    }
}
