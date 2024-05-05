package ru.denishik.unity;

import android.app.Activity;


import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;

import ru.denishik.unity.midwayApi.Scooter;
import ru.denishik.unity.midwayApi.ScootersApi;

public class PluginUnity {
    private static Activity unityActivity;
    static ArrayList<Scooter> scooters = null;

    public static boolean fetchScooters() {
        if (unityActivity == null) {
            unityActivity = new UnityPlayerActivity();
        }
        ru.denishik.unity.midwayApi.ScootersApi.getScooters(unityActivity, new ScootersApi.getScooterCallback() {
            @Override
            public void onGetScooter(ArrayList<Scooter> result) {
                scooters = result;
            }

            @Override
            public void onGetError() {

            }
        });

        return true;
    }

    public static ArrayList<Scooter> getScooters() {
        return scooters;
    }
}
