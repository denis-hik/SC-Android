package sc.denishik.ru;

import static sc.denishik.ru.other.Config.SCOOTER_ERROR_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_NAME_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.Random;

import sc.denishik.ru.midwayApi.Scooter;
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

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		MapKitFactory.setApiKey("d9c967a8-e55c-47ff-8d95-19924290e00b");
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
	}

	private void initializeLogic() {
		scooter = new Scooter("", "", "", "");
		viewPager.setPagingEnabled(false);
		BottomSheetBehavior behavior = BottomSheetBehavior.from(linear2);
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

					default:
						break;
				}
			}
		};
		viewPager.setAdapter(new Adapters.ViewsAdapter(getSupportFragmentManager(), this));
		IntentFilter filter = new IntentFilter();
		filter.addAction(SCOOTER_GET_DATA_NAME_COMMAND);
		filter.addAction(SCOOTER_ERROR_CONNECT);
		filter.addAction(SCOOTER_GET_DATA_PARAMS_COMMAND);
		registerReceiver(mMessageReceiver, filter);

		gps_pos = new Point(59.945933, 30.320045);
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
		MapObjectCollection mapObjects = mapview1.getMap().getMapObjects().addCollection();
		PlacemarkMapObject mark = mapObjects.addPlacemark(gps_pos);

		gps = MapKitFactory.getInstance().createLocationManager();
		gps_listener = new LocationListener() {
			@Override
			public void onLocationUpdated(@NonNull Location location) {
				Log.d("onLocationChanged", String.valueOf(location.getPosition()));
				gps_pos = location.getPosition();
				mapview1.getMap().move(
						new CameraPosition(gps_pos, 17.0f, 0.0f, 0.0f),
						new Animation(Animation.Type.SMOOTH, 5),
						null);
				mark.setGeometry(gps_pos);
				if (location.getSpeed() != null && location.getSpeed() > 5) {
					speed.setText(String.valueOf(location.getSpeed()));
					save_scale_type.setVisibility(View.VISIBLE);
				}
//				behavior.setPeekHeight(70, true);
			}

			@Override
			public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus) {

			}
		};
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
	}
	
	@Override
	public void onStop() {
		mapview1.onStop();
		unregisterReceiver(mMessageReceiver);
		gps.unsubscribe(gps_listener);
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
