package sc.denishik.ru.midwayApi.help;

import com.blankj.utilcode.util.LogUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import cn.wandersnail.ble.EasyBLE;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.LazyThreadSafetyMode;
import kotlin.collections.ArraysKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import sc.denishik.ru.ServiceScooter;
import sc.denishik.ru.midwayApi.Config;

public class SubPackageOnce {
    @NotNull
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final Lazy<SubPackageOnce> instance$delegate;
    @NotNull
    private final DataBuffer dataBuffer = DataBuffer.Companion.getInstance();

    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final SubPackageOnce getInstance() {
            return (SubPackageOnce) SubPackageOnce.instance$delegate.getValue();
        }
    }

    static {
        Lazy<SubPackageOnce> lazy;
        lazy = LazyKt.lazy(LazyThreadSafetyMode.SYNCHRONIZED, (Function0) new Function0<SubPackageOnce>() { // from class: com.zydtech.library.core.SubPackageOnce$Companion$instance$2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            @NotNull
            public final SubPackageOnce invoke() {
                return new SubPackageOnce();
            }
        });
        instance$delegate = lazy;
    }

    private final boolean isInArray(byte b2) {
        boolean contains;
        Config config = Config.INSTANCE;
        if (!config.isCustomHead()) {
            return b2 == -85 || b2 == 1;
        }
        if (b2 != config.getCustomHeadEsc()) {
            contains = ArraysKt.contains(config.getCustomHeadMonitors(), b2);
            if (!contains) {
                return false;
            }
            config.setCurrentHeadMonitor(b2);
        }
        return true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x004a, code lost:
        if (r12.dataBuffer.getCmdSize() >= 8) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x004c, code lost:
        r1 = 8;
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x005d, code lost:
        if (r12.dataBuffer.getCmdSize() >= 9) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x005f, code lost:
        r1 = 9;
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x0070, code lost:
        if (r12.dataBuffer.getCmdSize() >= 9) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:27:0x0081, code lost:
        if (r12.dataBuffer.getCmdSize() >= 9) goto L30;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x0092, code lost:
        if (r12.dataBuffer.getCmdSize() >= 8) goto L25;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x00de, code lost:
        if (r12.dataBuffer.getCmdSize() >= 9) goto L30;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final boolean subCmd() {
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:236:0x02d9  */
    /* JADX WARN: Removed duplicated region for block: B:240:0x02ed  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public interface subDataCallback {
        void onSuccess(byte[] arr, DataBuffer dataBuffer);
    }

    public final boolean subData(subDataCallback callback) {
        int i2;
//            InternalListener internalListener;
        byte[] allData = this.dataBuffer.getAllData();
        int i3 = 4;
        if (this.dataBuffer.getDataSize() >= 4) {
            Config config = Config.INSTANCE;
            if (config.isCustomHead()) {
                byte b2 = allData[0];
                if (b2 == config.getCustomHeadEsc()) {
                    byte b3 = allData[1];
                    if (((b3 == 3 || b3 == 7) || b3 == 8) || b3 == 23) {
                        int readUInt16BE$default = ByteArrayExtKt.readUInt16BE$default(new byte[]{0, allData[4]}, 0, 1, null);
                        i2 = readUInt16BE$default + 7;
                        if (this.dataBuffer.getDataSize() >= i2) {
                            int i4 = readUInt16BE$default + 5;
                            int MODBUS = CRC16.INSTANCE.MODBUS(allData, 0, i4);
                            if (allData[i4] != ((byte) MODBUS) || allData[readUInt16BE$default + 6] != ((byte) (MODBUS >> 8))) {
                                this.dataBuffer.release(1);
                                return true;
                            }
                            i3 = i2;
                            if (i3 > 0 && i3 <= this.dataBuffer.getDataSize()) {
                                byte[] take = this.dataBuffer.take(i3 & 255);
//                                    internalListener = this.listener;
//                                    if (internalListener != null) {
//                                        internalListener.onMessageResponseClient(take, DataType.Data);
//                                    }
                                LogUtils.d("onStart onSuc");
                                callback.onSuccess(take, dataBuffer);
                                return true;
                            }
                        }
                    } else {
                        if (b3 == 16) {
                            if (this.dataBuffer.getDataSize() >= 8) {
                                int MODBUS2 = CRC16.INSTANCE.MODBUS(allData, 0, 6);
                                if (allData[6] != ((byte) MODBUS2) || allData[7] != ((byte) (MODBUS2 >> 8))) {
                                    this.dataBuffer.release(1);
                                    return true;
                                }
                                i3 = 8;
                            }
                        } else if (b3 != 80) {
                            if ((((b3 == -125 || b3 == -121) || b3 == -120) || b3 == -112) || b3 == -105) {
                                if (this.dataBuffer.getDataSize() >= 5) {
                                    int MODBUS3 = CRC16.INSTANCE.MODBUS(allData, 0, 3);
                                    if (allData[3] != ((byte) MODBUS3) || allData[4] != ((byte) (MODBUS3 >> 8))) {
                                        this.dataBuffer.release(1);
                                        return true;
                                    }
                                    i3 = 5;
                                }
                            } else {
                                int MODBUS4 = CRC16.INSTANCE.MODBUS(allData, 0, 2);
                                if (allData[2] != ((byte) MODBUS4) || allData[3] != ((byte) (MODBUS4 >> 8))) {
                                    this.dataBuffer.release(1);
                                    return true;
                                }
                            }
                        } else if (this.dataBuffer.getDataSize() >= 8) {
                            int MODBUS5 = CRC16.INSTANCE.MODBUS(allData, 0, 6);
                            if (allData[6] != ((byte) MODBUS5) || allData[7] != ((byte) (MODBUS5 >> 8))) {
                                this.dataBuffer.release(1);
                                return true;
                            }
                            i3 = 8;
                        }
                        if (i3 > 0) {
                            byte[] take2 = this.dataBuffer.take(i3 & 255);
//                                internalListener = this.listener;
//                                if (internalListener != null) {
//                                     internalListener.onMessageResponseClient(take, DataType.Data);
//                                }
                            LogUtils.d("onStart onSuc");
                            callback.onSuccess(take2, dataBuffer);
                            return true;
                        }
                    }
                } else if (b2 == config.getCurrentHeadMonitor()) {
                    if (this.dataBuffer.getDataSize() >= 25) {
                        int MODBUS6 = CRC16.INSTANCE.MODBUS(allData, 0, 23);
                        if (allData[23] != ((byte) MODBUS6) || allData[24] != ((byte) (MODBUS6 >> 8))) {
                            this.dataBuffer.release(1);
                            return true;
                        }
                        i3 = 25;
                        if (i3 > 0) {
                            byte[] take2 = this.dataBuffer.take(i3 & 255);
//                                internalListener = this.listener;
//                                if (internalListener != null) {
//                                     internalListener.onMessageResponseClient(take, DataType.Data);
//                                }
                            LogUtils.d("onStart onSuc");
                            callback.onSuccess(take2, dataBuffer);
                            return true;
                        }
                    }
                } else if (this.dataBuffer.getDataSize() > 25) {
                    int dataSize = this.dataBuffer.getDataSize() - 1;
                    int i5 = 0;
                    while (i5 < dataSize) {
                        if (isInArray(allData[i5])) {
                            DataBuffer dataBuffer = this.dataBuffer;
                            if (i5 == 0) {
                                i5 = 1;
                            }
                            dataBuffer.release(i5);
                            return true;
                        }
                        i5++;
                    }
                }
                i3 = -1;
                if (i3 > 0) {
                    byte[] take2 = this.dataBuffer.take(i3 & 255);
//                                internalListener = this.listener;
//                                if (internalListener != null) {
//                                     internalListener.onMessageResponseClient(take, DataType.Data);
//                                }
                    LogUtils.d("onStart onSuc");
                    callback.onSuccess(take2, dataBuffer);
                    return true;
                }
            } else {
                byte b4 = allData[0];
                if (b4 == 1) {
                    byte b5 = allData[1];
                    if (((b5 == 3 || b5 == 7) || b5 == 8) || b5 == 23) {
                        int readUInt16BE$default2 = ByteArrayExtKt.readUInt16BE$default(new byte[]{0, allData[4]}, 0, 1, null);
                        i2 = readUInt16BE$default2 + 7;
                        if (this.dataBuffer.getDataSize() >= i2) {
                            int i6 = readUInt16BE$default2 + 5;
                            int MODBUS7 = CRC16.INSTANCE.MODBUS(allData, 0, i6);
                            if (allData[i6] != ((byte) MODBUS7) || allData[readUInt16BE$default2 + 6] != ((byte) (MODBUS7 >> 8))) {
                                this.dataBuffer.release(1);
                                return true;
                            }
                            i3 = i2;
                        }
                    } else if (b5 == 16) {
                        if (this.dataBuffer.getDataSize() >= 8) {
                            int MODBUS8 = CRC16.INSTANCE.MODBUS(allData, 0, 6);
                            if (allData[6] != ((byte) MODBUS8) || allData[7] != ((byte) (MODBUS8 >> 8))) {
                                this.dataBuffer.release(1);
                                return true;
                            }
                            i3 = 8;
                        }
                    } else if (b5 != 80) {
                        if ((((b5 == -125 || b5 == -121) || b5 == -120) || b5 == -112) || b5 == -105) {
                            i2 = 5;
                            if (this.dataBuffer.getDataSize() >= 5) {
                                int MODBUS9 = CRC16.INSTANCE.MODBUS(allData, 0, 3);
                                if (allData[3] != ((byte) MODBUS9) || allData[4] != ((byte) (MODBUS9 >> 8))) {
                                    this.dataBuffer.release(1);
                                    return true;
                                }
                                i3 = i2;
                            }
                        } else {
                            int MODBUS10 = CRC16.INSTANCE.MODBUS(allData, 0, 2);
                            if (allData[2] != ((byte) MODBUS10) || allData[3] != ((byte) (MODBUS10 >> 8))) {
                                this.dataBuffer.release(1);
                                return true;
                            }
                        }
                    } else if (this.dataBuffer.getDataSize() >= 8) {
                        int MODBUS11 = CRC16.INSTANCE.MODBUS(allData, 0, 6);
                        if (allData[6] != ((byte) MODBUS11) || allData[7] != ((byte) (MODBUS11 >> 8))) {
                            this.dataBuffer.release(1);
                            return true;
                        }
                        i3 = 8;
                    }
                    if (i3 > 0) {
                        byte[] take2 = this.dataBuffer.take(i3 & 255);
//                                internalListener = this.listener;
//                                if (internalListener != null) {
//                                     internalListener.onMessageResponseClient(take, DataType.Data);
//                                }
                        LogUtils.d("onStart onSuc");
                        callback.onSuccess(take2, dataBuffer);
                        return true;
                    }
                } else if (b4 == -85) {
                    if (this.dataBuffer.getDataSize() >= 25) {
                        int MODBUS12 = CRC16.INSTANCE.MODBUS(allData, 0, 23);
                        if (allData[23] != ((byte) MODBUS12) || allData[24] != ((byte) (MODBUS12 >> 8))) {
                            this.dataBuffer.release(1);
                            return true;
                        }
                        i3 = 25;
                        if (i3 > 0) {
                            byte[] take2 = this.dataBuffer.take(i3 & 255);
//                                internalListener = this.listener;
//                                if (internalListener != null) {
//                                     internalListener.onMessageResponseClient(take, DataType.Data);
//                                }
                            callback.onSuccess(take2, dataBuffer);
                            return true;
                        }
                    }
                } else if (this.dataBuffer.getDataSize() > 25) {
                    int dataSize2 = this.dataBuffer.getDataSize() - 1;
                    int i7 = 0;
                    while (i7 < dataSize2) {
                        if (isInArray(allData[i7])) {
                            DataBuffer dataBuffer2 = this.dataBuffer;
                            if (i7 == 0) {
                                i7 = 1;
                            }
                            dataBuffer2.release(i7);
                            return true;
                        }
                        i7++;
                    }
                }
                i3 = -1;
                if (i3 > 0) {
                    byte[] take2 = this.dataBuffer.take(i3 & 255);
//                                internalListener = this.listener;
//                                if (internalListener != null) {
//                                     internalListener.onMessageResponseClient(take, DataType.Data);
//                                }
                    LogUtils.d("onStart onSuc");
                    callback.onSuccess(take2, dataBuffer);
                    return true;
                }
            }
        }
        return false;
    }

}
