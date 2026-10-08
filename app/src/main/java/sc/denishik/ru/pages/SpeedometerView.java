package sc.denishik.ru.pages;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

/** Telemetry foreground; the real MapKit surface remains interactive underneath. */
public final class SpeedometerView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double speed = Double.NaN, voltage = Double.NaN, battery = Double.NaN;
    private String gear = "—";
    private boolean online;
    private boolean recording;
    private sc.denishik.ru.mapsApi.MapsController recordingMap;
    public void setRecording(boolean value, sc.denishik.ru.mapsApi.MapsController maps) {
        recording = value;
        recordingMap = value ? maps : null;
        invalidate();
    }
    public SpeedometerView(Context context, AttributeSet attrs) { super(context, attrs); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES); }
    public void update(double value, String gear, double voltage, double battery, boolean online) {
        this.speed = value; this.gear = gear; this.voltage = voltage; this.online = online;
        this.battery = battery >= 0 && battery <= 100 ? battery : Double.NaN;
        setContentDescription("Скорость " + (online ? String.format(Locale.getDefault(), "%.0f", value) : "нет данных")
                + ", режим " + gear + ", напряжение " + (Double.isNaN(voltage) ? "нет данных" : voltage)
                + ", заряд " + (online && !Double.isNaN(this.battery) ? String.format(Locale.getDefault(), "%.0f процентов", this.battery) : "нет данных"));
        invalidate();
    }
    private void text(Canvas canvas, String value, float x, float y, float size, int color, boolean bold) {
        paint.setShader(null); paint.setStyle(Paint.Style.FILL); paint.setColor(color);
        paint.setTypeface(bold ? Typeface.create("sans-serif", Typeface.BOLD) : Typeface.create("sans-serif", Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(size); canvas.drawText(value, x, y, paint);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth(), height = getHeight();
        if (width <= 0 || height <= 0) return;
        // The map fills the upper part; telemetry occupies the former map area below.
        float panelTop = height * .62f;
        if (recording) {
            canvas.drawColor(0xff00ff00);
            if (recordingMap != null) recordingMap.drawRecordingMap(canvas);
        } else {
        paint.setShader(new LinearGradient(0, height * .56f, 0, panelTop,
                0x00160f1b, 0xff160f1b, Shader.TileMode.CLAMP));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, height * .56f, width, height, paint);
        paint.setShader(null);
        }
        float w = Math.min(width, height * .36f / 1.1f);
        float cx = w / 2, cy = w * .48f, radius = w * .44f;
        canvas.save();
        canvas.translate((width - w) / 2, panelTop);
        RectF arc = new RectF(cx-radius, cy-radius, cx+radius, cy+radius);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(w*.018f); paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xff513859); canvas.drawArc(arc, 150, 240, false, paint);
        if (online && !Double.isNaN(speed)) {
            paint.setShader(new LinearGradient(0, 0, w, 0, 0xffd4a1dd, 0xffb477c2, Shader.TileMode.CLAMP));
            canvas.drawArc(arc, 150, (float)(Math.max(0, Math.min(100, speed)) * 2.4), false, paint); paint.setShader(null);
        }
        for (int i=0; i<=100; i+=2) {
            double angle = Math.toRadians(150 + i * 2.4);
            float outer=radius-w*.035f, inner=outer-w*(i%10==0?.030f:.014f);
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(w*.003f); paint.setColor(i%10==0?0xff9aaecb:0xff40516b);
            canvas.drawLine(cx+(float)Math.cos(angle)*inner,cy+(float)Math.sin(angle)*inner,
                    cx+(float)Math.cos(angle)*outer,cy+(float)Math.sin(angle)*outer,paint);
            if(i%10==0) text(canvas,String.valueOf(i),cx+(float)Math.cos(angle)*(radius-w*.092f),
                    cy+(float)Math.sin(angle)*(radius-w*.092f)+w*.012f,w*.035f,0xffafbdd2,false);
        }
        text(canvas,online && !Double.isNaN(speed)?String.format(Locale.getDefault(),"%.0f",speed):"—",cx,cy+w*.09f,w*.16f,Color.WHITE,true);
        text(canvas,"км/ч",cx,cy+w*.15f,w*.036f,0xffa7b5ca,false);
        float row=cy+w*.065f;
        text(canvas,online?gear:"—",w*.27f,row,w*.065f,recording ? 0xffeeeeff : 0xff6cddd1,true);
        text(canvas,"РЕЖИМ",w*.27f,row+w*.055f,w*.025f,0xff91a3bc,false);
        text(canvas,online && !Double.isNaN(voltage)?String.format(Locale.getDefault(),"%.1f",voltage):"—",w*.73f,row,w*.055f,0xffd2def4,true);
        text(canvas,"ВОЛЬТ",w*.73f,row+w*.055f,w*.025f,0xff91a3bc,false);
        text(canvas,online?"ДАННЫЕ САМОКАТА":"ОЖИДАНИЕ ДАННЫХ",cx,cy+radius*.80f,w*.026f,0xff91a3bc,false);
        float batteryRadius = w * .14f;
        float batteryY = cy + radius * .80f + w * .20f;
        RectF batteryArc = new RectF(cx-batteryRadius, batteryY-batteryRadius,
                cx+batteryRadius, batteryY+batteryRadius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(w * .014f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(recording ? 0xff513859 : 0xff23453d);
        canvas.drawArc(batteryArc, 180, 180, false, paint);
        boolean hasBattery = online && !Double.isNaN(battery);
        if (hasBattery && battery > 0) {
            paint.setColor(recording ? 0xffd4a1dd : 0xff58df94);
            canvas.drawArc(batteryArc, 180, (float)(battery * 1.8), false, paint);
        }
        text(canvas, hasBattery ? String.format(Locale.getDefault(), "%.0f%%", battery) : "—",
                cx, batteryY-w*.01f, w*.05f, recording ? 0xffd4a1dd : 0xff58df94, true);
        canvas.restore();
        if (recording) postInvalidateDelayed(33);
    }
}
