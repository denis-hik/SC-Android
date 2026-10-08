package sc.denishik.ru.kuggoApi;

import cn.wandersnail.ble.Device;

public class Scooter {
    public static final String NAME = "name";
    private String name;

    public static final String UUID = "uuid";
    private String uuid;

    // Tuya product metadata (fetched from cloud by productId/deviceUuid/mac)
    private String productName;
    private String iconUrl;
    private String deviceUuid;

    public static final String ADDRESS = "address";
    private String address;

    public static final String DEVICE = "device";
    private Device device;

    public static final String BINDED = "binded";
    private boolean binded;

    public static final String DEV_ID = "devId";
    private String devId;

    public static final String DEVICE_TYPE = "deviceType";
    private int deviceType;

    public static final String RAW_ADV = "rawAdv";
    private byte[] rawAdv;

    public static final String RSSI = "rssi";
    private int rssi;

    public Scooter(String name, String uuid, String address, Device device, boolean binded, String devId) {
        this.name = name;
        this.uuid = uuid;
        this.address = address;
        this.device = device;
        this.binded = binded;
        this.devId = devId;
        this.deviceType = 0;
        this.rawAdv = null;
        this.rssi = 0;
        this.productName = null;
        this.iconUrl = null;
        this.deviceUuid = null;
    }

    public Scooter(String name, String uuid, String address, Device device, boolean binded, String devId, int deviceType) {
        this.name = name;
        this.uuid = uuid;
        this.address = address;
        this.device = device;
        this.binded = binded;
        this.devId = devId;
        this.deviceType = deviceType;
        this.rawAdv = null;
        this.rssi = 0;
        this.productName = null;
        this.iconUrl = null;
        this.deviceUuid = null;
    }

    public Scooter(String name, String uuid, String address, Device device, boolean binded, String devId, int deviceType, byte[] rawAdv, int rssi) {
        this.name = name;
        this.uuid = uuid;
        this.address = address;
        this.device = device;
        this.binded = binded;
        this.devId = devId;
        this.deviceType = deviceType;
        this.rawAdv = rawAdv;
        this.rssi = rssi;
        this.productName = null;
        this.iconUrl = null;
        this.deviceUuid = null;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getProductId() {
        return uuid;
    }

    public String getProductName() {
        return productName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setProductInfo(String productName, String iconUrl) {
        if (productName != null && !productName.trim().isEmpty()) this.productName = productName.trim();
        if (iconUrl != null && !iconUrl.trim().isEmpty()) this.iconUrl = iconUrl.trim();
    }

    public void setDeviceUuid(String deviceUuid) {
        if (deviceUuid != null && !deviceUuid.trim().isEmpty()) this.deviceUuid = deviceUuid.trim();
    }

    public String getDisplayName() {
        if (productName != null && !productName.trim().isEmpty()) return productName.trim();
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Device getDevice() {
        return device;
    }

    public boolean isBinded() {
        return binded;
    }

    public String getDevId() {
        return devId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public byte[] getRawAdv() {
        return rawAdv;
    }

    public int getRssi() {
        return rssi;
    }
}
