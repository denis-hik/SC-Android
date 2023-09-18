package sc.denishik.ru;

import static sc.denishik.ru.other.Config.SCOOTER_ERROR_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_NAME_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;

import android.Manifest;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.FilteringMode;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import sc.denishik.ru.midwayApi.Scooter;
import sc.denishik.ru.midwayApi.base.BaseParams;
import sc.denishik.ru.other.Adapters;
import sc.denishik.ru.other.CustomViewPager;

public class HomeActivity extends AppCompatActivity {

	private static final int ACCESS_FINE_LOCATION = 1;
	private static final String TAG = "HomeActivity";
	private CoordinatorLayout linear1;
	private Scooter scooter;
	private MapView mapview1;
	private View linear2;
	private LinearLayout linear13;
	private LinearLayout route_block;
	private TextView textview1;
	private ImageView image;
	private CustomViewPager viewPager;
	private CardView save_scale_type;
	private LinearLayout linear4;
	private LinearLayout linear16;
	private BroadcastReceiver mMessageReceiver;
	private LocationManager gps;
	private LocationListener gps_listener;
	private Point gps_pos;
	private CardView error_block;
	private TextView speed;
	private CardView home;
	private float azimut;
	private SensorEventListener mySensorEventListener;
	private SensorManager mySensorManager;
	private List<Sensor> mySensors;
	private CardView garden;
	private boolean isMoved = false;
	private BottomSheetBehavior behavior;
	private Animator.AnimatorListener animationHide;
	private boolean isHide = false;
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
		behavior = BottomSheetBehavior.from(linear2);
	}

	private void initializeLogic() {
		animationHide = new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				route_block.setAlpha(0.0f);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				route_block.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		};
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
						error_block.setVisibility(View.VISIBLE);
						break;
					case SCOOTER_GET_DATA_PARAMS_COMMAND:
						HashMap<String, Object> temp = (HashMap<String, Object>) intent.getSerializableExtra("data");
						if (temp != null) {
							BaseParams params = new BaseParams(temp);
							if (params.getSpeed() > 5.0) {
								speed.setText(String.valueOf(params.getSpeed()));
								save_scale_type.setVisibility(View.VISIBLE);
								viewPager.setCurrentItem(1);
								route_block.animate().alpha(0.0f).setDuration(1).setListener(animationHide).start();
								behavior.setPeekHeight((int) getDip(230), true);
								isHide = false;
							} else {
								if (!isHide) {
									isHide = true;
									route_block.setVisibility(View.VISIBLE);
									route_block.setAlpha(1.0f);
									save_scale_type.setVisibility(View.GONE);
									behavior.setPeekHeight((int) getDip(110), true);
								}

							}
						}
						break;

					default:
						break;
				}
			}
		};
		mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		mySensorEventListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if ((int) azimut != (int) event.values[0]) {
					mapview1.getMap().move(
							new CameraPosition(gps_pos, 17.0f, event.values[0], 0.0f),
							new Animation(Animation.Type.SMOOTH, 2),
							null);
				}
				azimut = event.values[0];
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		};


		viewPager.setAdapter(adapterView);
		IntentFilter filter = new IntentFilter();
		filter.addAction(SCOOTER_GET_DATA_NAME_COMMAND);
		filter.addAction(SCOOTER_ERROR_CONNECT);
		filter.addAction(SCOOTER_GET_DATA_PARAMS_COMMAND);
		registerReceiver(mMessageReceiver, filter);

		gps_pos = new Point(59.945933, 30.320045);
		Point home_pos = new Point(55.730887, 37.459038);
		Point garden_pos = new Point(55.738636, 37.464624);
		if (getSharedPreferences("file", MODE_PRIVATE).getString("isLoc", "0").equals("1")) {
			mapview1.getMap().move(
					new CameraPosition(gps_pos, 17.0f, 0.0f, 0.0f),
					new Animation(Animation.Type.SMOOTH, 3),
					b -> {
						mapview1.setVisibility(View.VISIBLE);
						image.setVisibility(View.GONE);
					});
			mapview1.getMap().setFastTapEnabled(false);
			mapview1.setClickable(false);
			mapview1.setNoninteractive(true);
		}

		MapObjectCollection mapObjects = mapview1.getMap().getMapObjects().addCollection();
		PlacemarkMapObject mark = mapObjects.addPlacemark(gps_pos);

		gps = MapKitFactory.getInstance().createLocationManager();
		gps_listener = new LocationListener() {
			@Override
			public void onLocationUpdated(@NonNull Location location) {
				onSpeed(location, mark);
			}

			@Override
			public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {

			}
		};
		garden.setOnClickListener(v -> {

		});
		home.setOnClickListener(v -> {

		});
		behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {

			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
				Log.d(TAG, String.valueOf(slideOffset));
				if (slideOffset > 0.6) {
					route_block.setVisibility(View.GONE);
				} else {
					route_block.setAlpha(1);
					route_block.setVisibility(View.VISIBLE);
				}
				if (slideOffset > 0.2 && slideOffset < 0.6) {
					double i = slideOffset - 0.2;
					route_block.setAlpha((float) i);
				}
			}
		});
		if (getSharedPreferences("file", MODE_PRIVATE).getString("isLoc", "0").equals("1")) {
			if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "initializeLogic:>  requestLocationUpdates = true");
				gps.subscribeForLocationUpdates(0,0, 60, false, FilteringMode.OFF, gps_listener);
			} else {
				Log.e(TAG, "initializeLogic:>  requestLocationUpdates = false");
				ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						ACCESS_FINE_LOCATION
				);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == ACCESS_FINE_LOCATION) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						ACCESS_FINE_LOCATION
				);
			} else {
				gps.subscribeForLocationUpdates(0,0, 60, false, FilteringMode.OFF, gps_listener);
			}
		}
	}

	public void onSpeed(Location location, PlacemarkMapObject mark) {
		Log.d("onLocationChanged", String.valueOf(location.getPosition()));
		gps_pos = location.getPosition();
		mapview1.getMap().move(
				new CameraPosition(gps_pos, 17.0f, azimut, 0.0f),
				new Animation(Animation.Type.SMOOTH, 5),
				new Map.CameraCallback() {
					@Override
					public void onMoveFinished(boolean b) {
						if(mySensors.size() > 0 && b) {
							mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
						}
					}
				});isMoved = true;
		mark.setGeometry(gps_pos);

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mMessageReceiver);
		gps.unsubscribe(gps_listener);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		MapKitFactory.getInstance().onStart();
		mapview1.onStart();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		try {
			unregisterReceiver(mMessageReceiver);
		} catch (Exception e) {

		}
		try {
			mySensorManager.unregisterListener(mySensorEventListener);
		} catch (Exception e) {

		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(SCOOTER_GET_DATA_NAME_COMMAND);
		filter.addAction(SCOOTER_ERROR_CONNECT);
		filter.addAction(SCOOTER_GET_DATA_PARAMS_COMMAND);
		registerReceiver(mMessageReceiver, filter);
		List<Sensor> mySensors = mySensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
//		mySensorManager.registerListener(mySensorEventListener, mySensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	public void onStop() {
		mapview1.onStop();
		unregisterReceiver(mMessageReceiver);
		gps.unsubscribe(gps_listener);
		mySensorManager.unregisterListener(mySensorEventListener);
		MapKitFactory.getInstance().onStop();
		super.onStop();
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
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
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
