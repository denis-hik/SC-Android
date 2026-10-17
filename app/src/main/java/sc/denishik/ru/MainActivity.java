package sc.denishik.ru;

import static sc.denishik.ru.other.Modals.showPermissionModal;
import static sc.denishik.ru.other.Modals.showStartModal;
import static sc.denishik.ru.other.Utils.isServiceRunning;

import android.Manifest;
import android.content.Context;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.yandex.mapkit.MapKitFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import sc.denishik.ru.other.Modals;

public class MainActivity extends AppCompatActivity {
	private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
	private ArrayList<HashMap<String, Object>> listScooters = new ArrayList<>();
	private com.google.android.material.bottomsheet.BottomSheetDialog dialog;

	private LinearLayout linear1;
	private LinearLayout blu_err;
	private ImageView imageview1;
	private ImageView imageview2;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
        if (sc.denishik.ru.other.ConnectionPermissions.missing(this).length == 0) {
            initializeLogic();
        } else {
            dialog = showPermissionModal(this, sheet -> {
                dialog = sheet;
                String[] missing = sc.denishik.ru.other.ConnectionPermissions.missing(this);
                if (missing.length == 0) {
                    finishPermissionSetup();
                    return;
                }
                boolean blocked = false;
                for (String permission : missing) {
                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
                            && getSharedPreferences("permission_requests", MODE_PRIVATE).getBoolean(permission, false)
                            && !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) blocked = true;
                }
                if (blocked) {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:" + getPackageName())));
                } else {
                    android.content.SharedPreferences.Editor editor = getSharedPreferences("permission_requests", MODE_PRIVATE).edit();
                    for (String permission : missing) editor.putBoolean(permission, true);
                    editor.apply();
                    ActivityCompat.requestPermissions(this, missing, REQUEST_BLUETOOTH_PERMISSION);
                }
            });
        }
    }

    private boolean permissionSetupFinished;
    private void finishPermissionSetup() {
        if (permissionSetupFinished) return;
        permissionSetupFinished = true;
        if (dialog != null) dialog.dismiss();
        blu_err.setVisibility(View.GONE);
        initializeLogic();
    }

    @Override protected void onResume() {
        super.onResume();
        if (dialog != null && dialog.isShowing()) {
            if (sc.denishik.ru.other.ConnectionPermissions.missing(this).length == 0) finishPermissionSetup();
            else sc.denishik.ru.other.ConnectionPermissions.refresh(this, dialog);
        }
    }

	@Override
	public void onDestroy() {
        if (dialog != null) dialog.dismiss();
		super.onDestroy();
	}

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_BLUETOOTH_PERMISSION) return;
        if (sc.denishik.ru.other.ConnectionPermissions.missing(this).length == 0) finishPermissionSetup();
        else if (dialog != null) sc.denishik.ru.other.ConnectionPermissions.refresh(this, dialog);
    }

	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		blu_err = findViewById(R.id.blu_err);
		imageview1 = findViewById(R.id.imageview1);
		imageview2 = findViewById(R.id.imageview2);
	}

	private boolean restoringConnection;
	private void initializeLogic() {
        if (restoringConnection) return;
        restoringConnection = true;
        new Thread(() -> {
            String restoredId = "";
            try { restoredId = sc.denishik.ru.tuya.KugooProfileStore.restoreDeviceId(getApplicationContext()); }
            catch (Exception ignored) { }
            final boolean hasSavedConnection = !restoredId.isEmpty();
            runOnUiThread(() -> {
                restoringConnection = false;
                if (isFinishing() || isDestroyed()) return;
                openStartScreen(hasSavedConnection);
            });
        }, "restore-kugoo").start();
    }

    private void openStartScreen(boolean hasSavedConnection) {
        if (getIntent().getBooleanExtra("manual_disconnect", false)) { _showConnect(); return; }
		if (hasSavedConnection || isServiceRunning(ServiceScooter.class, getApplicationContext())) {
            Intent restore = new Intent(this, ServiceScooter.class);
            restore.putExtra("command", sc.denishik.ru.other.Config.SCOOTER_GET_DATA_NAME_COMMAND);
            startService(restore);

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
