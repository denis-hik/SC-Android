package sc.denishik.ru.ledApiBLU;

import android.bluetooth.BluetoothDevice;

public class LedBluDevice {
    BluetoothDevice device;
    String name;

    public LedBluDevice(BluetoothDevice device, String name) {
       this.device = device;
       this.name = name;
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return device.getAddress();
    }

}
