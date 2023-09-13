package sc.denishik.ru;

import static sc.denishik.ru.midwayApi.ScootersApi.connectScooter;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.LIGHT_KEY;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.LOCK_KEY;
import static sc.denishik.ru.midwayApi.base.KeysBaseParam.MAX_KEY;
import static sc.denishik.ru.midwayApi.help.other.joinToString$default;
import static sc.denishik.ru.other.Config.SCOOTER_CONNECT_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_ERROR_CONNECT;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_NAME_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_GET_DATA_PARAMS_COMMAND;
import static sc.denishik.ru.other.Config.SCOOTER_SEND_DATA_PARAMS_COMMAND;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;

import cn.wandersnail.ble.Connection;
import cn.wandersnail.ble.Device;
import cn.wandersnail.ble.EasyBLE;
import cn.wandersnail.ble.EventObserver;
import cn.wandersnail.ble.Request;
import cn.wandersnail.ble.RequestBuilderFactory;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.internal.ProgressionUtilKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.CharsKt;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import sc.denishik.ru.midwayApi.Config;
import sc.denishik.ru.midwayApi.Scooter;
import sc.denishik.ru.midwayApi.ScootersApi;
import sc.denishik.ru.midwayApi.base.BaseParams;
import sc.denishik.ru.midwayApi.base.FullAdvParams;
import sc.denishik.ru.midwayApi.help.ByteArrayExtKt;
import sc.denishik.ru.midwayApi.help.CRC16;
import sc.denishik.ru.midwayApi.help.Common;
import sc.denishik.ru.midwayApi.help.Constant;
import sc.denishik.ru.midwayApi.help.Convert;
import sc.denishik.ru.midwayApi.help.DataBuffer;
import sc.denishik.ru.midwayApi.help.ParamsList;
import sc.denishik.ru.midwayApi.help.StringExtKT;
import sc.denishik.ru.midwayApi.help.SubPackageOnce;
import sc.denishik.ru.midwayApi.help.ValueExtKt;

public class ServiceScooter extends Service implements EventObserver {

    private int startIdGlobal;
    private Scooter scooter;
    private Intent intent;
    private String uuid;
    private boolean isSendAdv = false;
    private int idStatus = 0;
    private DataBuffer dataBuffer;
    private BaseParams params = new BaseParams(0, false, false, false, false, false, false, 0, 0, 0, 0, 0.0f, 0, 0, null, null, 0, 0, 0, 0, 0, null, 4194303, null);
    private FullAdvParams fullAdvParams = new FullAdvParams(false, false, false, 0, 0, 0, 0, 0.0f, 0.0f, 0.0f, 0, 0.0f, 0, 0, 0, 0, 0, 131071, null);
    private int[] bitString = {0, 0, 0, 0, 0, 0, 0, 0};
    private Connection connection;
    private ParamsList myParamsList = new ParamsList(null, 1, null);
    private String device_name;
    private static List<Stack<Integer>> paramsList = new ArrayList();
    private EasyBLE blu;
    private Handler handler;
    private static byte[] paramsBuf = new byte[1024];
    private boolean isSendNotif = false;
    private String TAG = "ServiceScooter";
    private Runnable timeoutRunnable;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        blu = EasyBLE.getInstance();
        blu.initialize(getApplication());
        handler = new Handler(Looper.getMainLooper());
        dataBuffer = DataBuffer.Companion.getInstance();
        timeoutRunnable = new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                Log.d(TAG, "Handler> onStart");
                int i2;
                int i3;
                int i4;
                boolean z2;
                Handler handler;
                i2 = 0;
                int fail = i2 + 1;
                int failMax = 3;
                i3 = fail;
                i4 = failMax;
                if (scooter == null) {
                    Log.e(TAG, "Handler> scooter == null");
                    return;
                }
                if (blu.getConnection(scooter.getAddress()) == null) {
                    Log.e(TAG, "Handler> blu.getConnection(scooter.getAddress()) == null");
                    return;
                }
                if (blu.getConnection(scooter.getAddress()).getConnectionState().ordinal() < 4) {
                    Log.e(TAG, "Handler> blu.getConnection(scooter.getAddress()) state< 4 state=".concat(String.valueOf(blu.getConnection(scooter.getAddress()).getConnectionState().ordinal())));
                    return;
                }
                byte[] bytes = ("AT+PWD[888888]").getBytes(Charsets.UTF_8);
                sendCommandScooter(true, bytes);
                handler = ServiceScooter.this.handler;
                handler.postDelayed(timeoutRunnable, 200L);
            }
        };
        getParamsList();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.intent = intent;
        Log.d(TAG, "onStartCommand: I:".concat(String.valueOf(intent)
                .concat(" flags:").concat(String.valueOf(flags))
                .concat(" startId:")).concat(String.valueOf(startId)));
        startIdGlobal = startId;
        if (intent != null) {
            String command = intent.getStringExtra("command");

            switch (command) {
                case SCOOTER_CONNECT_COMMAND:
                    uuid = intent.getStringExtra("uuid");
                    device_name = intent.getStringExtra("name_device");
                    connectToScooter(intent.getStringExtra("uuid"),
                            intent.getStringExtra("address"),
                            intent.getStringExtra("name"),
                            intent.getStringExtra("name_device")
                    );
                    break;

                case SCOOTER_GET_DATA_NAME_COMMAND:
                    Intent intent1 = new Intent(SCOOTER_GET_DATA_NAME_COMMAND);
                    intent1.putExtra("name", scooter.getName());
                    sendBroadcast(intent);
                    break;

                case SCOOTER_SEND_DATA_PARAMS_COMMAND:
                    int key = intent.getIntExtra("key", -1);
                    Object value = (Object) intent.getSerializableExtra("value");
                    if (key == -1 || value == null) {
                        Log.e(TAG, "onStartCommand> SCOOTER_SEND_DATA_PARAMS_COMMAND> key=".concat(String.valueOf(value).concat(" value=").concat(String.valueOf(value))));
                        break;
                    }
                    handleParam(key, value);
                    break;

                default:
                    break;
            }

        }


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        blu.unregisterObserver(this);
        handler.removeCallbacks(timeoutRunnable);
        Log.d(TAG, "onDestroy");
    }

    public void onChanged(Object obj) {
        Log.d(TAG, "onChanged ".concat(String.valueOf(obj)));
    }

    @Override
    public void onConnectionStateChanged(@NonNull Device device) {
        int state = device.getConnectionState().ordinal();
        Log.d(TAG, "onConnectionStateChanged: deviceName:".concat(String.valueOf(device.getName()).concat("=").concat(device_name)).concat(" state:").concat(String.valueOf(state)));
        if (Objects.equals(device_name, device.getName()) && device.isConnected() && state == 1) {
            paramsBuf = new byte[1024];
            params = new BaseParams(0, false, false, false, false, false, false, 0, 0, 0, 0, 0.0f, 0, 0, null, null, 0, 0, 0, 0, 0, null, 4194303, null);
            fullAdvParams = new FullAdvParams(false, false, false, 0, 0, 0, 0, 0.0f, 0.0f, 0.0f, 0, 0.0f, 0, 0, 0, 0, 0, 131071, null);
            return;
        }
        if (Objects.equals(device_name, device.getName()) && device.isConnected() && state == 5) {
            connection = blu.getConnection(device);
            Constant constant = Constant.INSTANCE;
            myParamsList = new ParamsList();
            scooter = new Scooter(device.getName(), uuid, "", device.getAddress(), device);
            onSendNotif();
            return;
        }
    }

    @Override
    public void onConnectFailed(@NonNull Device device, int failType) {
        Log.e(TAG, "onConnectFailed: ".concat(String.valueOf(device)));
        if (Objects.equals(device_name, device.getName())) {
            Intent intent = new Intent(SCOOTER_CONNECT_COMMAND);
            // You can also include some extra data.
            isSendNotif = false;
            intent.putExtra("Status", false);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onConnectTimeout(@NonNull Device device, int type) {
        Log.e(TAG, "onConnectTimeout: ".concat(String.valueOf(device)));
        if (Objects.equals(device_name, device.getName())) {
            Intent intent = new Intent(SCOOTER_CONNECT_COMMAND);
            isSendNotif = false;
            intent.putExtra("Status", false);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onConnectionError(@NonNull Device device, int status) {
        Log.e(TAG, "onConnectionError: ".concat(String.valueOf(device)));
        if (Objects.equals(device_name, device.getName())) {
            Intent intent = new Intent(SCOOTER_CONNECT_COMMAND);
            // You can also include some extra data.
            isSendNotif = false;
            intent.putExtra("Status", false);
            sendBroadcast(intent);
            Intent intent2 = new Intent(SCOOTER_ERROR_CONNECT);
            intent2.putExtra("Status", false);
            sendBroadcast(intent2);
        }
    }

    @Override
    public void onNotificationChanged(@NotNull Request request, boolean z) {
        Log.d("onNotificationChanged", String.valueOf(request.getCharacteristic()));
        if (!isSendNotif) {
            isSendNotif = true;
            String str = "AT+PWD[888888]";
            byte[] bytes = str.getBytes(Charsets.UTF_8);
            sendCommandScooter(true, bytes);
            getSharedPreferences("login", Activity.MODE_PRIVATE).edit().putString("address", scooter.getAddress()).apply();
            handler.postDelayed(timeoutRunnable, 1000L);
        }
    }

    @Override
    public void onCharacteristicChanged(@NonNull Device device, @NonNull UUID service, @NonNull UUID characteristic, @NonNull byte[] value) {
        Constant constant = Constant.INSTANCE;
        if (Intrinsics.areEqual(characteristic, constant.getCMD_RX())) {
            Log.d("onCharacteristicChanged", "AT_RX".concat(" str value:").concat(new String(value, StandardCharsets.UTF_8)));
            String data = new String(value, StandardCharsets.UTF_8);
            Log.d(TAG, "hex get>"
                    .concat(Convert.toHexString$default(Convert.INSTANCE, value, false, 0, 0, 14, (Object) null))
                    .concat(" str>").concat(data));
            if (blu.isScanning()) {
                blu.stopScan();
            }
            byte b = value[0];
            byte b2 = value[1];
            Log.d(TAG, "b=".concat(String.valueOf(b).concat(" b2=" + String.valueOf(b2))));

            byte[] command = {-91, b, (byte) ((byte) 1), 0, 0, 0, 0, 90};
            sendCommandScooter(false, command);
        } else if (Intrinsics.areEqual(characteristic, constant.getDATA_RX())) {
            Log.d("onCharacteristicChanged", "DATA_RX".concat(" str value:").concat(new String(value, StandardCharsets.UTF_8)));
            byte b = value[0];
            byte b2 = value[1];
            Log.d(TAG, "b=".concat(String.valueOf(b).concat(" b2=" + String.valueOf(b2))));
            Intent intent = new Intent(SCOOTER_CONNECT_COMMAND);
            intent.putExtra("Status", true);
            sendBroadcast(intent);
            dataBuffer.append(value);
//            prepareDataRX(value);
            do {
            } while (SubPackageOnce.Companion.getInstance().subData(this::getBaseParams));

        }
//        Constant constant = Constant.INSTANCE;
//        if (Intrinsics.areEqual(service, constant.getCMD_RX())) {
//            String str = new String(value, StandardCharsets.UTF_8);
//            String now = Common.now$default(Common.INSTANCE, (String) null, 1, (Object) null);
//            String uuid = characteristic.toString();
//            String take = StringsKt.take(uuid, 8);
//            String hexString = ByteArrayExtKt.toHexString$default(value, false, false, 3, (Object) null);
//            Log.d(TAG, "hexString> ".concat(hexString));
//            if (StringsKt.contains(str, "OK+PWD:Y", false) || StringsKt.contains(str, "ERR+AT", false) || StringsKt.contains(str, "OK+PWD:N", false)) {
//                handler.removeCallbacksAndMessages(null);
//            }
//            dataBuffer.appendCmd(value);
//        } else if (SubPackageOnce.Companion.getInstance().subCmd()) {
//            String now = Common.now$default(Common.INSTANCE, (String) null, 1, null);
//            String uuid = characteristic.toString();
//            String take = StringsKt.take(uuid, 8);
//            int length = value.length;
//            String hexString = ByteArrayExtKt.toHexString$default(value, false, false, 3, null);
//            Log.d(TAG, "hexString> ".concat(hexString));
//            dataBuffer.append(value);
//        }

    }


    @Override
    public void onCharacteristicRead(@NonNull Request request, @NonNull byte[] value) {
        Intrinsics.checkNotNullParameter(request, "request");
        Intrinsics.checkNotNullParameter(value, "value");
        String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        int length = value.length;
        String hexString$default = ByteArrayExtKt.toHexString$default(value, false, false, 3, null);
        Log.d(TAG, now$default + " onCharacteristicWrite Size: " + length + ", Write: " + hexString$default);
    }

    private void connectToScooter(String uuid, String address, String name, String name_device) {
        blu.registerObserver(this);
        dataBuffer.init();
        connectScooter(new Scooter(name, uuid, "", address), blu, this, new ScootersApi.connectScooterCallback() {
            @Override
            public void onSuccess(Scooter scooterR) {
                scooter = scooterR;
                Intent intent = new Intent(SCOOTER_CONNECT_COMMAND);
                // You can also include some extra data.
                intent.putExtra("Status", true);
                sendBroadcast(intent);

            }

            @Override
            public void onError(String err) {
                Intent intent = new Intent(SCOOTER_CONNECT_COMMAND);
                // You can also include some extra data.
                intent.putExtra("Status", false);
                sendBroadcast(intent);
            }
        });
    }

    private boolean sendCommandScooter(boolean isCMD, byte[] bytes) {
        RequestBuilderFactory request = new RequestBuilderFactory();
        if (isCMD) {
            Constant constant = Constant.INSTANCE;
            request.getWriteCharacteristicBuilder(constant.getCMD_SERVICE(), constant.getCMD_TX(), bytes).build().execute(connection);
        } else {
            Constant constant = Constant.INSTANCE;
            request.getWriteCharacteristicBuilder(constant.getDATA_SERVICE(), constant.getDATA_TX(), bytes).build().execute(connection);
        }
        return true;
    }

    public void onSendNotif() {
        RequestBuilderFactory requestBuilderFactory = new RequestBuilderFactory();
        Constant constant = Constant.INSTANCE;
        requestBuilderFactory.getSetNotificationBuilder(constant.getCMD_SERVICE(), constant.getCMD_RX(), true).build().execute(connection);
        new RequestBuilderFactory().getSetNotificationBuilder(constant.getDATA_SERVICE(), constant.getDATA_RX(), true).build().execute(connection);
        new RequestBuilderFactory().getChangeMtuBuilder(200).build().execute(connection);
    }

    public void handleParam(int key, Object value) {

        Log.d(TAG, "handleParam> key=".concat(String.valueOf(key).concat(" value=").concat(String.valueOf(value))));
//        if (baseParams2 == null) {
//            Log.e(TAG, "baseparams = null");
//            return;
//        }
        Convert c = Convert.INSTANCE;
        switch (key) {
            case LIGHT_KEY:
                params.setHeadLightSw((Boolean) !params.getHeadLightSw());
                break;
            case LOCK_KEY:
                params.setLockSw((Boolean) !params.getLockSw());
                break;
            case MAX_KEY:
                params.setLimitMode3((int) value);
                break;
        }
        sendParamsData();
    }

    public final void sendParamsData() {
        LogUtils.d("Send to scooter> params:" + params);
        BaseParams baseParams2 = params.clone();
        byte[] bArr = new byte[10];
        Config config = Config.INSTANCE;
        bArr[0] = Constant.HEAD_MONITOR;
        bArr[1] = 0;
        bArr[2] = 10;
        byte b = new BigInteger(joinToString$default(ArraysKt.reversedArray(new int[]{
                Convert.INSTANCE.getBit(baseParams2.getGearPosition(), 0),
                Convert.INSTANCE.getBit(baseParams2.getGearPosition(), 1),
                baseParams2.getHeadLightSw() ? 1 : 0,
                baseParams2.getAtmosphereLightSw() ? 1 : 0,
                baseParams2.getCruiseControlSw() ? 1 : 0,
                baseParams2.getBootMode() ? 1 : 0,
                baseParams2.getMetricInchSw() ? 1 : 0,
                baseParams2.getLockSw() ? 1 : 0}
        ), "", null, null, 0, null, null, 62, null), CharsKt.checkRadix(2)).byteValue();
        bArr[3] = b;
        bArr[4] = (byte) baseParams2.getLimitCruise();
        bArr[5] = (byte) baseParams2.getLimitMode1();
        bArr[6] = (byte) baseParams2.getLimitMode2();
        bArr[7] = (byte) baseParams2.getLimitMode3();
        int MODBUS = CRC16.INSTANCE.MODBUS(bArr, 0, 8);
        bArr[8] = (byte) MODBUS;
        bArr[9] = (byte) (MODBUS >> 8);

        sendCommandScooter(false, bArr);
    }

    public final void parsingParams(byte[] bArr, DataBuffer dataBuffer) {
        int readUInt16BE = ByteArrayExtKt.readUInt16BE(bArr, 2);
        int i2 = bArr[4] >> 1;
        String hexString$default = ByteArrayExtKt.toHexString$default(bArr, false, false, 3, null);
        LogUtils.d("parsingParamsBuf:  " + hexString$default + " count: " + i2 + " dataBuf:" + dataBuffer.getDataSize() + " readUInt16BE:" + readUInt16BE);
        System.arraycopy(bArr, 5, paramsBuf, readUInt16BE * 2, i2 * 2);
        if (readUInt16BE == 6401) {
            int readInt16BE$default = ByteArrayExtKt.readInt16BE$default(paramsBuf, 0, 1, null);
            int registerZero = readInt16BE$default;
            fullAdvParams.setCruiseSw(ValueExtKt.toBool(ValueExtKt.getBit(readInt16BE$default, 9)));
            fullAdvParams.setMetric(ValueExtKt.toBool(ValueExtKt.getBit(registerZero, 6)));
            fullAdvParams.setZeroStart(ValueExtKt.toBool(ValueExtKt.getBit(registerZero, 5)));
            fullAdvParams.setMaxModulationDepth((ByteArrayExtKt.readInt16BE(paramsBuf, 4) * 50) / 21845);
            fullAdvParams.setMotorPolePairs(ByteArrayExtKt.readInt16BE(paramsBuf, 8));
            fullAdvParams.setAcceleratedThrottleResponse((ByteArrayExtKt.readInt16BE(paramsBuf, 18) * 10) / 30000);
            fullAdvParams.setAcceleratorBrakeResponse((ByteArrayExtKt.readInt16BE(paramsBuf, 20) * 10) / 30000);
            fullAdvParams.setMaxDischargeCurrent((float) ValueExtKt.addHalf(ByteArrayExtKt.readInt16BE(paramsBuf, 22) / 64.0d));
            fullAdvParams.setMaxBrakingCurrent((float) ValueExtKt.addHalf(ByteArrayExtKt.readInt16BE(paramsBuf, 24) / 64.0d));
            fullAdvParams.setVoltageProtection((float) ValueExtKt.addHalf(ByteArrayExtKt.readInt16BE(paramsBuf, 38) / 10.0d));
            fullAdvParams.setLimitedSpeedValue((int) ValueExtKt.addHalf(ByteArrayExtKt.readInt16BE(paramsBuf, 64) / 10.0d));
            fullAdvParams.setMotorDiameter((float) ValueExtKt.addHalf((ByteArrayExtKt.readInt16BE(paramsBuf, 46) * 10.0d) / 254.0d));
            fullAdvParams.setPwmFrequency(ByteArrayExtKt.readInt16BE(paramsBuf, 66));
            fullAdvParams.setCruiseTime(ByteArrayExtKt.readInt16BE(paramsBuf, 102));
            fullAdvParams.setShutdownTime(ByteArrayExtKt.readInt16BE(paramsBuf, 104));
            fullAdvParams.setServiceMileage(ByteArrayExtKt.readInt16BE(paramsBuf, 146));
            fullAdvParams.setLastServiceMileage(ByteArrayExtKt.readUInt32BEI(paramsBuf, 148));
            String hexString$default2 = ByteArrayExtKt.toHexString$default(paramsBuf, false, false, 3, null);
            LogUtils.e("高级参数解析    " + hexString$default2);
        } else if (readUInt16BE != 73) {
            if (readUInt16BE != 74) {
                return;
            }
            String hexString$default3 = ByteArrayExtKt.toHexString$default(bArr, false, false, 3, null);
            LogUtils.e("74号寄存器  " + hexString$default3);
            int readUInt32BEI = ByteArrayExtKt.readUInt32BEI(bArr, 5);
            fullAdvParams.setLastServiceMileage(readUInt32BEI);

        } else {
            String hexString$default4 = ByteArrayExtKt.toHexString$default(bArr, false, false, 3, null);
            LogUtils.e("73号寄存器  " + hexString$default4);
            int readUInt16BE2 = ByteArrayExtKt.readUInt16BE(bArr, 11);
            fullAdvParams.setServiceMileage(readUInt16BE2 == 0 ? 300 : readUInt16BE2);
            int limitService = readUInt16BE2 == 0 ? 300 : Integer.valueOf(readUInt16BE2);
            String testStr = ByteArrayExtKt.toHexString$default(bArr, false, false, 3, null);
        }

        scooter.setAdvBaseParams(fullAdvParams);
        scooter.setBaseParams(params);
    }

    public final void getBaseParams(byte[] bArr, DataBuffer dataBuffer) {
        if (fullAdvParams == null && !isSendAdv) {
            isSendAdv = true;
//            sendGetAdvParams(bArr);
        }
        if (bArr[0] != -85) {
            if (bArr[0] == 1 && bArr[1] != 3) {
                parsingParams(bArr, dataBuffer);
            }
            return;
        }
        if (bArr[1] != 0) {
            if (bArr[1] == 1) {
                params.setLimitCruise(bArr[3] & 255);
                params.setLimitMode1(bArr[4] & 255);
                params.setLimitMode2(bArr[5] & 255);
                params.setLimitMode3(bArr[6] & 255);
                String hexString$default = ByteArrayExtKt.toHexString$default(bArr, 18, 2, false, false, 8, null);
                String str = "V" + ((int) bArr[20]) + "." + ((int) bArr[21]) + "." + ((int) bArr[22]);
                if (!Intrinsics.areEqual(hexString$default, "0000")) {
                    params.setDisplayName(hexString$default);
                    params.setDisplayVersion(str);
                }
            }
        } else {
            int registerZero = ByteArrayExtKt.readUInt16BE(bArr, 21);
            int readUInt16BE = ByteArrayExtKt.readUInt16BE(bArr, 6);
            int readUInt16BE2 = ByteArrayExtKt.readUInt16BE(bArr, 8);
            params.setGearPosition(bArr[4] & 255);
            bitString[0] = ValueExtKt.getBit(registerZero, 0);
            bitString[1] = ValueExtKt.getBit(registerZero, 1);
            bitString[2] = ValueExtKt.getBit(registerZero, 2);
            bitString[3] = ValueExtKt.getBit(registerZero, 15);
            bitString[4] = ValueExtKt.getBit(registerZero, 9);
            bitString[5] = ValueExtKt.getBit(registerZero, 5);
            bitString[6] = ValueExtKt.getBit(registerZero, 6);
            bitString[7] = ValueExtKt.getBit(registerZero, 11);

            params.setSpeed(ValueExtKt.maxDecimal(readUInt16BE2 / 1000.0f, 1));
            float current = ValueExtKt.maxDecimal(ByteArrayExtKt.readShortBE(bArr, 12) / 64.0f, 1);
            float voltage = ValueExtKt.maxDecimal(ByteArrayExtKt.readUInt16BE(bArr, 10) / 10.0f, 1);
            params.setPower(ValueExtKt.maxDecimal(voltage, 1));
            params.setHeadLightSw(ValueExtKt.toBool(bitString[2]));
            params.setAtmosphereLightSw(ValueExtKt.toBool(bitString[3]));
            params.setCruiseControlSw(ValueExtKt.toBool(bitString[4]));
            params.setBootMode(ValueExtKt.toBool(bitString[5]));
            params.setMetricInchSw(ValueExtKt.toBool(bitString[6]));
            params.setLockSw(ValueExtKt.toBool(bitString[7]));
            LogUtils.d("baseParams> " + params.toString());
            Intent intent = new Intent(SCOOTER_GET_DATA_PARAMS_COMMAND);
            intent.putExtra("data", params.toObject());
            sendBroadcast(intent);
        }


    }

    private final void getParamsList() {
        try {
            InputStream inputStream = Utils.getApp().getResources().openRawResource(R.raw.default_parameter);
            byte[] bArr = new byte[inputStream.available()];
            int read = inputStream.read(bArr);
            Charset UTF_8 = StandardCharsets.UTF_8;
            Intrinsics.checkNotNullExpressionValue(UTF_8, "UTF_8");
            String str = new String(bArr, UTF_8);
            inputStream.close();
            Object fromJson = new Gson().fromJson(str, ParamsList.class);
            LogUtils.d(TAG, "fromJson> ", String.valueOf(fromJson));
            myParamsList = (ParamsList) fromJson;
            StringExtKT.logD("Parameter length:" + read);
//            for (ParamsList.Parameter parameter : myParamsList.getParameter()) {
//                int mode = parameter.getMode();
//                if (mode == 3) {
//                    double d2 = 10;
//                    int min = (int) (parameter.getMin() * d2);
//                    int max = parameter.getMax() * 10;
//                    int step = (int) (parameter.getStep() * d2);
//                    if (step > 0) {
//                        int progressionLastElement = ProgressionUtilKt.getProgressionLastElement(min, max, step);
//                        if (min <= progressionLastElement) {
//                            while (true) {
//                                List<String> values = parameter.getValues();
//                                String format = String.format(Locale.ENGLISH, "%.1f", Arrays.copyOf(new Object[]{Float.valueOf(min / 10.0f)}, 1));
//                                Intrinsics.checkNotNullExpressionValue(format, "format(locale, format, *args)");
//                                values.add(format);
//                                if (min != progressionLastElement) {
//                                    min += step;
//                                }
//                            }
//                        }
//                    } else {
//                        throw new IllegalArgumentException("Step must be positive, was: " + step + ".");
//                    }
//                } else if (mode != 5) {
//                    continue;
//                } else {
//                    double d3 = 10;
//                    int min2 = (int) (parameter.getMin() * d3);
//                    int max2 = parameter.getMax() * 10;
//                    int step2 = (int) (parameter.getStep() * d3);
//                    if (step2 > 0) {
//                        int progressionLastElement2 = ProgressionUtilKt.getProgressionLastElement(min2, max2, step2);
//                        if (min2 <= progressionLastElement2) {
//                            while (true) {
//                                parameter.getValues().add(String.valueOf(min2));
//                                if (min2 != progressionLastElement2) {
//                                    min2 += step2;
//                                }
//                            }
//                        }
//                    } else {
//                        throw new IllegalArgumentException("Step must be positive, was: " + step2 + ".");
//                    }
//                }
//            }
            ArrayList arrayList = new ArrayList();
            int size = myParamsList.getParameter().size();
            for (int i3 = 0; i3 < size; i3++) {
                if (myParamsList.getParameter().get(i3).getAddress() != 255) {
                    arrayList.add(Integer.valueOf(myParamsList.getParameter().get(i3).getAddress()));
                }
            }
            CollectionsKt.sort(arrayList);
            paramsList = Common.INSTANCE.distinct(CollectionsKt.toIntArray(CollectionsKt.toMutableSet(arrayList)));
        } catch (Exception e) {
            LogUtils.e(TAG, String.valueOf(e));
        }

    }

    private void sendGetAdvParams(byte[] bArr) {
        int readUInt16BE$default = ByteArrayExtKt.readUInt16BE$default(new byte[]{bArr[2], bArr[3]}, 0, 1, null);
        byte[] bArr2 = new byte[8];
        Config config = Config.INSTANCE;
        bArr2[0] = config.isCustomHead() ? config.getCustomHeadEsc() : (byte) 1;
        bArr2[1] = (byte) 8;
        int i2 = readUInt16BE$default + 8;
        bArr2[2] = (byte) (i2 >> 8);
        bArr2[3] = (byte) (i2 & 255);
        bArr2[4] = (byte) (4 >> 8);
        bArr2[5] = (byte) (4 & 255);
        int MODBUS = CRC16.INSTANCE.MODBUS(bArr2, 0, 6);
        bArr2[6] = (byte) MODBUS;
        bArr2[7] = (byte) (MODBUS >> 8);

        sendCommandScooter(false, bArr2);
    }


    public final void parsingSaveParams(byte[] bArr) {
        double addHalf;
        String hexString$default = ByteArrayExtKt.toHexString$default(bArr, false, false, 3, null);
        LogUtils.d("parsingParamsBuf1:  " + hexString$default);
        int registerZero = ByteArrayExtKt.readInt16BE$default(bArr, 0, 1, null);
        int readUInt16BE = ByteArrayExtKt.readUInt16BE(bArr, 2);
        if (readUInt16BE == 0) {
            registerZero = ByteArrayExtKt.readUInt16BE(bArr, 5);
        }
        List<ParamsList.Parameter> parameter = myParamsList.getParameter();
        ArrayList<ParamsList.Parameter> arrayList = new ArrayList();
        for (ParamsList.Parameter obj : parameter) {
            if (obj.getAddress() == readUInt16BE) {
                arrayList.add(obj);
            }
        }
        LogUtils.w(fullAdvParams.toString());
        if (!arrayList.isEmpty()) {
            for (ParamsList.Parameter parameter2 : arrayList) {
                switch (parameter2.getMode()) {
                    case 0:
                    case 1:
                        parameter2.setNow(ValueExtKt.getBit(registerZero, parameter2.getBit()));
                        int no2 = parameter2.getNo();
                        if (no2 == 0) {
                            fullAdvParams.setCruiseSw(ValueExtKt.toBool(parameter2.getNow()));
                        } else if (no2 == 1) {
                            fullAdvParams.setMetric(ValueExtKt.toBool(parameter2.getNow()));
                        } else if (no2 == 2) {
                            fullAdvParams.setZeroStart(ValueExtKt.toBool(parameter2.getNow()));
                        }
//                        CoreListener coreListener = Config.INSTANCE.getCoreListener();
//                        if (coreListener != null) {
//                            coreListener.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Boolean.valueOf(ValueExtKt.toBool(parameter2.getNow()))));
//                            break;
//                        } else {
//                            break;
//                        }
                    case 2:
                        parameter2.setNow((ByteArrayExtKt.readUInt16BE(bArr, 5) * parameter2.getMax()) / parameter2.getReal());
                        int no3 = parameter2.getNo();
                        if (no3 == 3) {
                            fullAdvParams.setMaxModulationDepth(parameter2.getNow());
                        } else if (no3 == 5) {
                            fullAdvParams.setAcceleratedThrottleResponse(parameter2.getNow());
                        } else if (no3 == 6) {
                            fullAdvParams.setAcceleratorBrakeResponse(parameter2.getNow());
                        }
                        if (parameter2.getNow() == 0) {
                            LogUtils.d("");
                        }
//                        CoreListener coreListener2 = Config.INSTANCE.getCoreListener();
//                        if (coreListener2 != null) {
//                            coreListener2.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Integer.valueOf(parameter2.getNow())));
//                            break;
//                        } else {
//                            break;
//                        }
                        break;
                    case 3:
                    case 5:
                        if (parameter2.getMode() == 3) {
                            addHalf = ValueExtKt.addHalf(ByteArrayExtKt.readUInt16BE(bArr, 5) / parameter2.getOpv());
                        } else {
                            addHalf = ValueExtKt.addHalf((ByteArrayExtKt.readUInt16BE(bArr, 5) * 10.0f) / (parameter2.getOpv() * 10.0f));
                        }
                        float f2 = (float) addHalf;
                        switch (parameter2.getNo()) {
                            case 7:
                                fullAdvParams.setMaxDischargeCurrent(f2);
                                break;
                            case 8:
                                fullAdvParams.setMaxBrakingCurrent(f2);
                                break;
                            case 9:
                                fullAdvParams.setVoltageProtection(f2);
                                break;
                            case 10:
                                fullAdvParams.setLimitedSpeedValue((int) f2);
                                break;
                            case 11:
                                fullAdvParams.setMotorDiameter(f2);
                                break;
                        }
                        if (parameter2.getNo() == 10) {
//                            CoreListener coreListener3 = Config.INSTANCE.getCoreListener();
//                            if (coreListener3 != null) {
//                                coreListener3.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Integer.valueOf((int) f2)));
//                                break;
//                            } else {
//                                break;
//                            }
                            break;
                        } else {
//                            CoreListener coreListener4 = Config.INSTANCE.getCoreListener();
//                            if (coreListener4 != null) {
//                                coreListener4.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Float.valueOf(f2)));
//                                break;
//                            } else {
//                                break;
//                            }
                            break;
                        }
                    case 4:
                        int no4 = parameter2.getNo();
                        if (no4 == 4) {
                            parameter2.setNow(ByteArrayExtKt.readUInt16BE(bArr, 5));
                            fullAdvParams.setMotorPolePairs(parameter2.getNow());
                        } else if (no4 == 13) {
                            parameter2.setNow(ByteArrayExtKt.readUInt16BE(bArr, 5));
                            fullAdvParams.setCruiseTime(parameter2.getNow());
                        } else if (no4 == 14) {
                            parameter2.setNow(ByteArrayExtKt.readUInt16BE(bArr, 5));
                            fullAdvParams.setShutdownTime(parameter2.getNow());
                        }
//                        CoreListener coreListener5 = Config.INSTANCE.getCoreListener();
//                        if (coreListener5 != null) {
//                            coreListener5.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Integer.valueOf(parameter2.getNow())));
//                            break;
//                        } else {
//                            break;
//                        }
                        break;
                    case 6:
                        parameter2.setNow(ByteArrayExtKt.readUInt16BE(bArr, 5));
                        fullAdvParams.setPwmFrequency(parameter2.getNow());
//                        CoreListener coreListener6 = Config.INSTANCE.getCoreListener();
//                        if (coreListener6 != null) {
//                            coreListener6.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Integer.valueOf(parameter2.getNow())));
//                            break;
//                        } else {
//                            break;
//                        }
                        break;
                    case 7:
                        parameter2.setNow(ByteArrayExtKt.readUInt16BE(bArr, 5));
                        fullAdvParams.setServiceMileage(parameter2.getNow());
//                        limitService = Integer.valueOf(parameter2.getNow());
//                        CoreListener coreListener7 = Config.INSTANCE.getCoreListener();
//                        if (coreListener7 != null) {
//                            coreListener7.onAdvParamsChanged(new AdvParams(parameter2.getNo(), Integer.valueOf(parameter2.getNow())));
//                            break;
//                        } else {
//                            break;
//                        }
                        break;
                }
            }
        }
    }
}
