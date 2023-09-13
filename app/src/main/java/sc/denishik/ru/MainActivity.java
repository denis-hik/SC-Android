package sc.denishik.ru;

import static sc.denishik.ru.other.Modals.showStartModal;
import static sc.denishik.ru.other.Utils.isServiceRunning;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yandex.mapkit.MapKitFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
	private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
	private static final int REQUEST_LOCATION_PERMISSION = 2;
	private static final int BLUETOOTH_PRIVILEGED = 3;
	private static final int ACCESS_FINE_LOCATION = 4;
	private ArrayList<HashMap<String, Object>> listScooters = new ArrayList<>();
	
	private LinearLayout linear1;
	private LinearLayout blu_err;
	private ImageView imageview1;
	private ImageView imageview2;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		MapKitFactory.setApiKey("d9c967a8-e55c-47ff-8d95-19924290e00b");
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_PRIVILEGED)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			// Request Bluetooth permissions
			ActivityCompat.requestPermissions(
					this,
					new String[]{
							Manifest.permission.BLUETOOTH,
							Manifest.permission.BLUETOOTH_ADMIN,
							Manifest.permission.BLUETOOTH_SCAN,
							Manifest.permission.BLUETOOTH_PRIVILEGED,
							Manifest.permission.ACCESS_COARSE_LOCATION,
							Manifest.permission.ACCESS_FINE_LOCATION
					},
					REQUEST_BLUETOOTH_PERMISSION
			);
			showMessage("enable permission blu");
			Log.d("", "onCreate: blu no");
			blu_err.setVisibility(View.VISIBLE);
		} else {
			Log.d("", "onCreate: blu yes");
			initializeLogic();
		}

	}

	@Override
	public void onDestroy() {
//		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == ACCESS_FINE_LOCATION) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						ACCESS_FINE_LOCATION
				);
			}
		}
		if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Bluetooth permission granted, check location permission
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED) {
					// Request location permission
					ActivityCompat.requestPermissions(
							this,
							new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
							REQUEST_LOCATION_PERMISSION
					);
				} else {
					initializeLogic();
					blu_err.setVisibility(View.GONE);
					Log.d("", "onRequestPermissionsResult: blu yes");
				}
			} else {
				showMessage("enable permision blu");
				Log.d("", "onRequestPermissionsResult: blu no");
				blu_err.setVisibility(View.VISIBLE);
			}
		} else if (requestCode == REQUEST_LOCATION_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				initializeLogic();
				blu_err.setVisibility(View.GONE);
				Log.d("", "onRequestPermissionsResult: blu yes");
			} else {
				showMessage("enable permision blu");
				Log.d("", "onRequestPermissionsResult: blu no");
				blu_err.setVisibility(View.VISIBLE);
			}
		} else if (requestCode == BLUETOOTH_PRIVILEGED) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

				Log.d("", "BLUETOOTH_PRIVILEGED: blu yes");
			} else {
				blu_err.setVisibility(View.VISIBLE);
				Log.d("", "BLUETOOTH_PRIVILEGED: blu no");
			}
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		blu_err = findViewById(R.id.blu_err);
		imageview1 = findViewById(R.id.imageview1);
		imageview2 = findViewById(R.id.imageview2);
	}
	
	private void initializeLogic() {
		if (isServiceRunning(ServiceScooter.class, getApplicationContext())) {

			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setClass(getApplicationContext(), HomeActivity.class);
			startActivity(i);
			finish();
		} else {
			_showConnect();
		}
	}
	
	public void _showConnect() {
		showStartModal(MainActivity.this, blu_err);
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
}
