package sc.denishik.ru.other;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import cn.wandersnail.ble.Device;
import cn.wandersnail.ble.EasyBLE;
import cn.wandersnail.ble.callback.ScanListener;
import sc.denishik.ru.other.BluetoothController;

public class BluetoothConnect {
	private static final String DEFAULT_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	private Activity activity;

	private BluetoothAdapter bluetoothAdapter;

	public BluetoothConnect(Activity activity) {
		this.activity = activity;
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public boolean isBluetoothEnabled() {
		if (bluetoothAdapter != null) return true;

		return false;
	}

	public boolean isBluetoothActivated() {
		if (bluetoothAdapter == null) return false;

		return bluetoothAdapter.isEnabled();
	}

	public void activateBluetooth() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivity(intent);
	}

	public String getRandomUUID() {
		return String.valueOf(UUID.randomUUID());
	}

	public interface getAllDevicesCallback {
		void onScan(ArrayList<BluetoothDevice> result, ArrayList<Device> devices);

		void onError();
	}

	public void getAllDevices(getAllDevicesCallback callback) {
		ArrayList<String> scaned = new ArrayList<>();
		ArrayList<BluetoothDevice> allDevices = new ArrayList<>();
		ArrayList<Device> allDevices2 = new ArrayList<>();
		if (!isBluetoothActivated()) {
			activateBluetooth();
		}
		ScanListener scanListener = new ScanListener() {
			@Override
			public void onScanStart() {
				//搜索开始
			}

			@Override
			public void onScanStop() {
				//搜索停止
			}

			@Override
			public void onScanResult(@NonNull Device device, boolean isConnectedBySys) {
				if (scaned.contains(device.getAddress())) {
					return;
				}
				scaned.add(device.getAddress());
				allDevices2.add(device);
				callback.onScan(allDevices, allDevices2);
			}

			@Override
			public void onScanError(int errorCode, @NotNull String errorMsg) {
				Log.e("EasyBLE", errorMsg);
				callback.onError();
				switch(errorCode) {
					case ScanListener.ERROR_LACK_LOCATION_PERMISSION://缺少定位权限
						break;
					case ScanListener.ERROR_LOCATION_SERVICE_CLOSED://位置服务未开启
						break;
					case ScanListener.ERROR_LACK_SCAN_PERMISSION://targetSdkVersion大于等于Android12时，缺少搜索权限(发现附近设备)
						break;
					case ScanListener.ERROR_LACK_CONNECT_PERMISSION://targetSdkVersion大于等于Android12时，缺少连接权限
						break;
					case ScanListener.ERROR_SCAN_FAILED://搜索失败
						break;
				}
			}
		};
		EasyBLE.getInstance().addScanListener(scanListener);
		EasyBLE.getInstance().startScan();
		if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
			Log.d("BLU", "Only paried!");
			getPairedDevices2(allDevices);
			callback.onScan(allDevices, allDevices2);
			return;
		}
		getPairedDevices2(allDevices);
		bluetoothAdapter.startDiscovery();
		BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("BLU", " onReceiver".concat(String.valueOf(action)));

				//Finding devices
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a ListView
					if (scaned.contains(device.getAddress())) {
						return;
					}
					scaned.add(device.getAddress());
					if (!allDevices.contains(device)) {
						allDevices.add(device);
						callback.onScan(allDevices, allDevices2);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		activity.registerReceiver(mReceiver, filter);

	}

	public void getPairedDevices(ArrayList<HashMap<String, Object>> results) {
		if (!isBluetoothActivated()) {
			activateBluetooth();
		}
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				HashMap<String, Object> result = new HashMap<>();
				result.put("name", device.getName());
				result.put("address", device.getAddress());

				results.add(result);
			}
		}
	}

	public void getPairedDevices2(ArrayList<BluetoothDevice> results) {
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		Log.d("BLU", "getPairedDevices2: size>".concat(String.valueOf(pairedDevices.size())));
		if (pairedDevices.size() > 0) {
			results.addAll(pairedDevices);
		}
	}

	public void readyConnection(BluetoothConnectionListener listener, String tag) {
		if (BluetoothController.getInstance().getState().equals(BluetoothController.STATE_NONE)) {
			BluetoothController.getInstance().start(this, listener, tag, UUID.fromString(DEFAULT_UUID), bluetoothAdapter);
		}
	}

	public void readyConnection(BluetoothConnectionListener listener, String uuid, String tag) {
		if (BluetoothController.getInstance().getState().equals(BluetoothController.STATE_NONE)) {
			BluetoothController.getInstance().start(this, listener, tag, UUID.fromString(uuid), bluetoothAdapter);
		}
	}


	public void startConnection(BluetoothConnectionListener listener, String address, String tag) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

		BluetoothController.getInstance().connect(device, this, listener, tag, UUID.fromString(DEFAULT_UUID), bluetoothAdapter);
	}

	public void startConnection(BluetoothConnectionListener listener, String uuid, String address, String tag, byte[] pass) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//		device.setPairingConfirmation(true);
		device.setPin(pass);

		BluetoothController.getInstance().connect(device, this, listener, tag, UUID.fromString(DEFAULT_UUID), bluetoothAdapter);
	}
	
	public void startConnection(BluetoothConnectionListener listener, String uuid, String address, String tag) {
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		
		BluetoothController.getInstance().connect(device, this, listener, tag, UUID.fromString(uuid), bluetoothAdapter);
	}
	
	public void stopConnection(BluetoothConnectionListener listener, String tag) {
		BluetoothController.getInstance().stop(this, listener, tag);
	}
	
	public void sendData(BluetoothConnectionListener listener, String data, String tag) {
		String state = BluetoothController.getInstance().getState();
		
		if (!state.equals(BluetoothController.STATE_CONNECTED)) {
			listener.onConnectionError(tag, state, "Bluetooth is not connected yet");
			return;
		}
		
		BluetoothController.getInstance().write(data.getBytes());
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public interface BluetoothConnectionListener {
		void onConnected(String tag, HashMap<String, Object> deviceData);
		void onDataReceived(String tag, byte[] data, int bytes);
		void onDataSent(String tag, byte[] data);
		void onConnectionError(String tag, String connectionState, String message);
		void onConnectionStopped(String tag);
	}
}
