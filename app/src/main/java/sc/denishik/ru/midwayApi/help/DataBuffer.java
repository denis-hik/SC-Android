package sc.denishik.ru.midwayApi.help;

import org.jetbrains.annotations.NotNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.LazyThreadSafetyMode;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public class DataBuffer {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final Lazy<DataBuffer> instance$delegate;
    @NotNull
    private ByteBuf cmdBuf;
    @NotNull
    private ByteBuf dataBuf;

    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final DataBuffer getInstance() {
            return (DataBuffer) DataBuffer.instance$delegate.getValue();
        }
    }

    static {
        Lazy<DataBuffer> lazy;
        lazy = LazyKt.lazy(LazyThreadSafetyMode.SYNCHRONIZED, (Function0) new Function0<DataBuffer>() { // from class: com.zydtech.library.core.DataBuffer$Companion$instance$2
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            @NotNull
            public final DataBuffer invoke() {
                return new DataBuffer();
            }
        });
        instance$delegate = lazy;
    }

    public DataBuffer() {
        ByteBuf buffer = Unpooled.buffer(1024);
        Intrinsics.checkNotNullExpressionValue(buffer, "buffer(1024)");
        this.dataBuf = buffer;
        ByteBuf buffer2 = Unpooled.buffer(1024);
        Intrinsics.checkNotNullExpressionValue(buffer2, "buffer(1024)");
        this.cmdBuf = buffer2;
    }

    public final void append(@NotNull byte[] data) {
        Intrinsics.checkNotNullParameter(data, "data");
        if ((data.length == 0) || !this.dataBuf.isWritable()) {
            return;
        }
        this.dataBuf.writeBytes(data);
    }

    public final void appendCmd(@NotNull byte[] data) {
        Intrinsics.checkNotNullParameter(data, "data");
        if ((data.length == 0) || !this.cmdBuf.isWritable()) {
            return;
        }
        this.cmdBuf.writeBytes(data);
    }

    @NotNull
    public final byte[] getAllCmd() {
        if (this.cmdBuf.hasArray()) {
            byte[] array = this.cmdBuf.array();
            Intrinsics.checkNotNullExpressionValue(array, "cmdBuf.array()");
            return array;
        }
        return new byte[0];
    }

    @NotNull
    public final byte[] getAllData() {
        if (this.dataBuf.hasArray()) {
            byte[] array = this.dataBuf.array();
            Intrinsics.checkNotNullExpressionValue(array, "dataBuf.array()");
            return array;
        }
        return new byte[0];
    }

    public final int getCmdSize() {
        return this.cmdBuf.readableBytes();
    }

    public final int getDataSize() {
        return this.dataBuf.readableBytes();
    }

    public final void init() {
        this.dataBuf.clear();
        ByteBuf byteBuf = this.dataBuf;
        byteBuf.setZero(0, byteBuf.capacity());
        this.cmdBuf.clear();
        this.cmdBuf.setZero(0, this.dataBuf.capacity());
    }

    public final void release(int i2) {
        try {
            if (this.dataBuf.hasArray() && this.dataBuf.isReadable()) {
                int readableBytes = this.dataBuf.readableBytes();
                int readerIndex = this.dataBuf.readerIndex();
                int writerIndex = this.dataBuf.writerIndex();
                StringExtKT.logA("release len: " + i2 + " size: " + readableBytes + ", r: " + readerIndex + ", w: " + writerIndex);
                this.dataBuf.readBytes(getDataSize() >= i2 ? i2 : getDataSize());
                this.dataBuf.discardReadBytes();
                int readableBytes2 = this.dataBuf.readableBytes();
                int readerIndex2 = this.dataBuf.readerIndex();
                int writerIndex2 = this.dataBuf.writerIndex();
                StringExtKT.logA("size: " + readableBytes2 + ", r: " + readerIndex2 + ", w: " + writerIndex2);
            }
        } catch (Exception e2) {
            String message = e2.getMessage();
            StringExtKT.logE("len: " + i2 + ", error: " + message);
        }
    }

    public final void releaseCmd(int i2) {
        try {
            if (this.cmdBuf.hasArray() && this.cmdBuf.isReadable()) {
                int readableBytes = this.cmdBuf.readableBytes();
                int readerIndex = this.cmdBuf.readerIndex();
                int writerIndex = this.cmdBuf.writerIndex();
                StringExtKT.logA("release len: " + i2 + " size: " + readableBytes + ", r: " + readerIndex + ", w: " + writerIndex);
                this.cmdBuf.readBytes(getCmdSize() >= i2 ? i2 : getCmdSize());
                this.cmdBuf.discardReadBytes();
                int readableBytes2 = this.cmdBuf.readableBytes();
                int readerIndex2 = this.cmdBuf.readerIndex();
                int writerIndex2 = this.cmdBuf.writerIndex();
                StringExtKT.logA("size: " + readableBytes2 + ", r: " + readerIndex2 + ", w: " + writerIndex2);
            }
        } catch (Exception e2) {
            String message = e2.getMessage();
            StringExtKT.logE("len: " + i2 + ", error: " + message);
        }
    }

    @NotNull
    public final byte[] take(int i2) {
        try {
            if (this.dataBuf.hasArray() && getDataSize() >= i2 && this.dataBuf.isReadable()) {
                byte[] result = this.dataBuf.readBytes(i2).array();
                this.dataBuf.discardReadBytes();
                Intrinsics.checkNotNullExpressionValue(result, "result");
                return result;
            }
            return new byte[0];
        } catch (Exception e2) {
            String message = e2.getMessage();
            StringExtKT.logE("len: " + i2 + ", error: " + message);
            return new byte[0];
        }
    }

    @NotNull
    public final byte[] takeCmd(int i2) {
        try {
            if (this.cmdBuf.hasArray() && getCmdSize() >= i2 && this.cmdBuf.isReadable()) {
                byte[] result = this.cmdBuf.readBytes(i2).array();
                this.cmdBuf.discardReadBytes();
                Intrinsics.checkNotNullExpressionValue(result, "result");
                return result;
            }
            return new byte[0];
        } catch (Exception e2) {
            String message = e2.getMessage();
            StringExtKT.logE("len: " + i2 + ", error: " + message);
            return new byte[0];
        }
    }

}
