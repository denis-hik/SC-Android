package sc.denishik.ru;

import android.Manifest;
import androidx.core.content.ContextCompat;

import static sc.denishik.ru.other.Config.SCOOTER_ERROR_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_NAME_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_LED;
import static sc.denishik.ru.other.Config.SCOOTER_LED_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_LED_DISCONNECT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import sc.denishik.ru.midwayApi.Scooter;
import sc.denishik.ru.midwayApi.base.BaseParams;
import sc.denishik.ru.other.Adapters;
import sc.denishik.ru.other.CustomViewPager;

public class HomeActivity extends AppCompatActivity {



    private sc.denishik.ru.mapsApi.MapsController maps;
    private final sc.denishik.ru.mapsApi.RideMode rideMode = new sc.denishik.ru.mapsApi.RideMode();
    private final android.os.Handler rideHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean riding, speedometerMode;
    private boolean mapEnabled = true;
    private boolean telemetryOnline;
    private String gaugeGear = "—";
    private double gaugeVoltage = Double.NaN, gaugeBattery = Double.NaN;
    private sc.denishik.ru.pages.SpeedometerView gauge;
    private sc.denishik.ru.recordingApi.ScreenRecordingController screenRecording;
    public void showSpeedometer(boolean enabled) {
        if (speedometerMode == enabled) return;
        if (!enabled && screenRecording != null) screenRecording.stop();
        speedometerMode = enabled;
        View overlay = findViewById(R.id.speedometer_overlay);
        overlay.setVisibility(enabled ? View.VISIBLE : View.GONE);
        // ViewDragHelper checks sibling bounds, including a previously laid-out
        // GONE overlay. The sheet must be the top child again to receive drags.
        if (enabled) overlay.bringToFront();
        else linear2.bringToFront();
        // Keep the sheet in CoordinatorLayout's layout pass so its measured height
        // and BottomSheetBehavior offsets remain valid while the gauge is open.
        linear2.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        behavior.setDraggable(!enabled);
        findViewById(R.id.map_status).setVisibility(enabled ? View.GONE : View.VISIBLE);
        findViewById(R.id.locate_me).setVisibility(enabled || !mapEnabled ? View.GONE : View.VISIBLE);
        error_block.setVisibility(enabled || telemetryOnline ? View.GONE : View.VISIBLE);
        save_scale_type.setVisibility(!enabled && riding ? View.VISIBLE : View.GONE);
        maps.speedometer(enabled);
        if (enabled || riding) getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!enabled) {
            linear2.requestLayout();
            linear2.post(() -> {
                if (!speedometerMode && !isFinishing() && !isDestroyed()) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }
        updateGauge();
    }
    private void updateGauge() {
        if (gauge != null) gauge.update(lastSpeed, gaugeGear, gaugeVoltage, gaugeBattery, telemetryOnline && lastTelemetryAt > 0
                && android.os.SystemClock.elapsedRealtime() - lastTelemetryAt <= 15000);
    }
    @Override public void onBackPressed() {
        if (speedometerMode) showSpeedometer(false); else super.onBackPressed();
    }
    private double lastSpeed;
    private long lastTelemetryAt;
    private final Runnable rideTick = new Runnable() {
        public void run() {
            if (android.os.SystemClock.elapsedRealtime() - lastTelemetryAt > 15000) {
                rideMode.reset(); setRideMode(false); speed.setText("—");
            } else setRideMode(rideMode.update(lastSpeed, android.os.SystemClock.elapsedRealtime()));
            updateGauge();
            rideHandler.postDelayed(this, 1000);
        }
    };
    public void setMapEnabled(boolean enabled) {
        mapEnabled = enabled;
        route_block.setVisibility(enabled && !riding && behavior.getState() != BottomSheetBehavior.STATE_EXPANDED
                ? View.VISIBLE : View.GONE);
        behavior.setPeekHeight((int) getDip(enabled && !riding ? 126 : 76));
        findViewById(R.id.locate_me).setVisibility(enabled && !speedometerMode ? View.VISIBLE : View.GONE);
    }
    public String mapProvider() { return maps == null ? "yandex" : maps.provider(); }
    public void selectMap(String provider) { if (maps != null) maps.selectProvider(provider); }
    public sc.denishik.ru.mapsApi.TravelMode travelMode() { return maps == null ? sc.denishik.ru.mapsApi.TravelMode.SCOOTER : maps.travelMode(); }
    public void selectTravelMode(sc.denishik.ru.mapsApi.TravelMode mode) { if (maps != null) maps.travelMode(mode); }
    public void selectLocationIcon(String name) { if (maps != null) maps.locationIcon(name); }
    public void importLocationIcon(android.net.Uri uri) { if (maps != null) maps.importLocationIcon(uri); }
    public void disconnectScooter() {
        sc.denishik.ru.tuya.KugooProfileStore.rememberDevice(this, "");
        getSharedPreferences("login", MODE_PRIVATE).edit().remove("address").apply();
        stopService(new Intent(this, ServiceScooter.class));
        startActivity(new Intent(this, MainActivity.class).putExtra("manual_disconnect", true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
    public void collapseForMap() {
        if (!speedometerMode) behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    private void setRideMode(boolean value) {
        if (riding == value) return;
        riding = value;
        maps.ride(value);
        save_scale_type.setVisibility(value && !speedometerMode ? View.VISIBLE : View.GONE);
        route_block.setVisibility(value || !mapEnabled ? View.GONE : View.VISIBLE);
        behavior.setPeekHeight((int) getDip(value || !mapEnabled ? 76 : 126), true);
        if (value) {
            viewPager.setCurrentItem(0);
            collapseForMap();
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else if (!speedometerMode) getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

	private CoordinatorLayout linear1;
	private Scooter scooter;
	private MapView mapview1;
	private View linear2;
	private LinearLayout linear13;
	private LinearLayout route_block;
	private TextView textview1;
    private ImageView image;
    private Boolean imageForKugoo;
    private void updateScooterImage(boolean kugoo) {
        if (imageForKugoo != null && imageForKugoo == kugoo) return;
        imageForKugoo = kugoo;
        image.setImageResource(kugoo ? R.drawable.kugoo_scooter : R.drawable.scooter1);
        image.setScaleX(kugoo ? 1f : -1.5f);
        image.setScaleY(kugoo ? 1f : 1.5f);
        image.setTranslationX(kugoo ? 0 : getDip(100));
    }
	private CustomViewPager viewPager;
	private CardView save_scale_type;
	private LinearLayout linear4;
	private LinearLayout linear16;
	private BroadcastReceiver mMessageReceiver;
	private CardView error_block;
	private TextView speed;
	private CardView home;
	private CardView garden;
	private BottomSheetBehavior behavior;
	private Adapters.ViewsAdapter adapterView = new Adapters.ViewsAdapter(getSupportFragmentManager(), this, this);

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		MapKitFactory.initialize(this);
		setContentView(R.layout.home);
		initialize(_savedInstanceState);
		initializeLogic();
	}

	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		mapview1 = findViewById(R.id.mapview1);
		image = findViewById(R.id.image);
		linear2 = findViewById(R.id.linear2);
		linear13 = findViewById(R.id.linear13);
		textview1 = findViewById(R.id.textview1);
		linear4 = findViewById(R.id.linear4);
		linear16 = findViewById(R.id.linear16);
		viewPager = findViewById(R.id.viewpage);
		save_scale_type = findViewById(R.id.save_scale_type);
		error_block = findViewById(R.id.error_block);
		speed = findViewById(R.id.speed);
		route_block = findViewById(R.id.route_block);
		garden = findViewById(R.id.garden);
		home = findViewById(R.id.home);
		route_block = findViewById(R.id.route_block);
        updateScooterImage(!sc.denishik.ru.tuya.KugooProfileStore.lastDeviceId(this).isEmpty());
        gauge = findViewById(R.id.speedometer_gauge);
        screenRecording = new sc.denishik.ru.recordingApi.ScreenRecordingController(this,
                recording -> gauge.setRecording(recording, maps));
        findViewById(R.id.close_speedometer).setOnClickListener(v -> showSpeedometer(false));
        behavior = BottomSheetBehavior.from(linear2);
	}

	private void initializeLogic() {
		scooter = new Scooter("", "", "", "");
		viewPager.setPagingEnabled(false);
		mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (intent.getAction()) {
					case SCOOTER_GET_DATA_NAME_COMMAND:
						String name = intent.getStringExtra("name");
						scooter.setData(name, scooter.getUUID(), scooter.getBattery(), scooter.getAddress());
						setScooterDataName();
						break;

					case SCOOTER_ERROR_CONNECT:
						telemetryOnline = intent.getBooleanExtra("Status", false);
                        error_block.setVisibility(telemetryOnline || speedometerMode ? View.GONE : View.VISIBLE);
                        updateGauge();
						break;
					case SCOOTER_GET_DATA_PARAMS_COMMAND:
						HashMap<String, Object> temp = (HashMap<String, Object>) intent.getSerializableExtra("data");
						if (temp != null) {
							BaseParams params = new BaseParams(temp);
                            lastSpeed = params.getSpeed();
                            lastTelemetryAt = intent.getLongExtra("telemetry_at", android.os.SystemClock.elapsedRealtime());
                            telemetryOnline = intent.getBooleanExtra("telemetry_online", true);
                            gaugeBattery = intent.getDoubleExtra("battery_percentage", Double.NaN);
                            updateScooterImage("kugoo".equals(intent.getStringExtra("source")));
                            if ("kugoo".equals(intent.getStringExtra("source"))) {
                                gaugeGear = sc.denishik.ru.mapsApi.SpeedometerData.gear(intent.getStringExtra("kugoo_level") == null ? "" : intent.getStringExtra("kugoo_level"));
                                gaugeVoltage = intent.getDoubleExtra("voltage", Double.NaN);
                            } else {
                                Object power = temp.get("power");
                                gaugeVoltage = power instanceof Number && ((Number)power).doubleValue() > 0 ? ((Number)power).doubleValue() : Double.NaN;
                                gaugeGear = params.getLockSw() ? "P" : String.valueOf(temp.get("gearPosition"));
                            }
                            updateGauge();
                            speed.setText(String.format(java.util.Locale.getDefault(), "%.0f", lastSpeed));
                            setRideMode(rideMode.update(lastSpeed, lastTelemetryAt));
						}
						break;
					case SCOOTER_LED_CONNECT:
						showMessage("Connect LEDs");
						break;
					case SCOOTER_LED_DISCONNECT:
						showMessage("disconnect LEDs");
						break;

					default:
						break;
				}
			}
		};
		viewPager.setAdapter(adapterView);
		IntentFilter filter = new IntentFilter();
		filter.addAction(SCOOTER_GET_DATA_NAME_COMMAND);
		filter.addAction(SCOOTER_ERROR_CONNECT);
		filter.addAction(SCOOTER_GET_DATA_PARAMS_COMMAND);
		filter.addAction(SCOOTER_LED);
		registerReceiver(mMessageReceiver, filter);

        maps = new sc.denishik.ru.mapsApi.MapsController(this);
        behavior.setPeekHeight((int) getDip(mapEnabled ? 126 : 76));
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            public void onStateChanged(@NonNull View sheet, int state) { }
            public void onSlide(@NonNull View sheet, float offset) {
                route_block.setVisibility(!mapEnabled || riding || offset > 0.6f ? View.GONE : View.VISIBLE);
                route_block.setAlpha(Math.max(0, 1 - Math.max(0, offset) / 0.6f));
            }
        });
    }
    @Override public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == sc.denishik.ru.mapsApi.MapsController.LOCATION_REQUEST
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) maps.requestLocation();
    }

	@Override
	public void onDestroy() {
		if (screenRecording != null) screenRecording.destroy();
		super.onDestroy();
		try { unregisterReceiver(mMessageReceiver); } catch (IllegalArgumentException ignored) { }
		if (maps != null) maps.destroy();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		MapKitFactory.getInstance().onStart();
		maps.start();
        rideHandler.post(rideTick);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		try {
			try { unregisterReceiver(mMessageReceiver); } catch (IllegalArgumentException ignored) { }
		} catch (Exception e) {

		}
		try {

		} catch (Exception e) {

		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(SCOOTER_GET_DATA_NAME_COMMAND);
		filter.addAction(SCOOTER_ERROR_CONNECT);
		filter.addAction(SCOOTER_GET_DATA_PARAMS_COMMAND);
		filter.addAction(SCOOTER_LED);
		registerReceiver(mMessageReceiver, filter);
        Intent snapshot = new Intent(this, ServiceScooter.class);
        snapshot.putExtra("command", SCOOTER_GET_DATA_NAME_COMMAND);
        startService(snapshot);
	}
	
	@Override
	public void onStop() {
        if (screenRecording != null) screenRecording.stop();
		rideHandler.removeCallbacks(rideTick);
        maps.stop();
		try { unregisterReceiver(mMessageReceiver); } catch (IllegalArgumentException ignored) { }


		MapKitFactory.getInstance().onStop();
		super.onStop();
	}

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (screenRecording != null) screenRecording.onResult(request, result, data);
    }
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx)) {
                _result.add((double)_arr.keyAt(_iIdx));
            }
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}

	private void setScooterDataName() {
		textview1.setText(scooter.getName());
	}
}
