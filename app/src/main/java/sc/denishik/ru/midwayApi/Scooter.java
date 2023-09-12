package sc.denishik.ru.midwayApi;

import static java.lang.System.out;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import cn.wandersnail.ble.Device;
import sc.denishik.ru.midwayApi.base.BaseParams;
import sc.denishik.ru.midwayApi.base.FullAdvParams;

public class Scooter {
    public static final String NAME = "name";
    private String name;

    public static final String UUID = "uuid";
    private String uuid;

    public static final String BATTERY = "battery";
    private String battery;

    public static final String ADDRESS = "address";
    private String address;

    public static final String DEVICE = "device";
    private Device device;

    public static final String BLU_DEVICE = "blu_device";
    private BluetoothDevice blu_device;

    public static final String IS_BLU_DEVICE = "is_blu_device";
    private boolean is_blu_device;

    public static final String BASE_PARAMS = "base_params";
    private BaseParams baseParams;

    public static final String ADV_BASE_PARAMS = "adv_base_params";
    private FullAdvParams advBaseParams;

    public Scooter(String name, String uuid, String battery, String address) {
        this.name = name;
        this.battery = battery;
        this.uuid = uuid;
        this.address = address;
    }
    public Scooter(String name, String uuid, String battery, String address, Device device) {
        this.name = name;
        this.battery = battery;
        this.uuid = uuid;
        this.address = address;
        this.device = device;
        this.is_blu_device = false;
    }
    public Scooter(String name, String uuid, String battery, String address, BluetoothDevice device) {
        this.name = name;
        this.battery = battery;
        this.uuid = uuid;
        this.address = address;
        this.blu_device = device;
        this.is_blu_device = true;
    }

    public BaseParams getBaseParams() {
        return this.baseParams;
    }
    public FullAdvParams getAdvBaseParams() {
        return this.advBaseParams;
    }

    public String getName() {
        return this.name;
    }

    public String getUUID() {
        return this.uuid;
    }

    public String getBattery() {
        return this.battery;
    }

    public String getAddress() {
        return this.address;
    }

    public Device getDevice() {
        return this.device;
    }
    public BluetoothDevice getBluDevice() {
        return this.blu_device;
    }
    public boolean getIsBluDevice() {
        return this.is_blu_device;
    }

    public int hashCode() {
        return Objects.hash(name, uuid, battery);
    }

    public String toString() {
        return "class User {\n" +
                "    Name: " + toIndentedString(name) + "\n" +
                "    UUID: " + toIndentedString(uuid) + "\n" +
                "    Battery: " + toIndentedString(battery) + "\n" +
                "    Address: " + toIndentedString(address) + "\n" +
                "}";
    }

    public void setBaseParams(BaseParams params) {
        this.baseParams = params;
    }
    public void setAdvBaseParams(FullAdvParams advBaseParams) {
        this.advBaseParams = advBaseParams;
    }

    public void setData(String name, String uuid, String battery, String address) {
        this.name = name;
        this.battery = battery;
        this.uuid = uuid;
        this.address = address;
    }

    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
