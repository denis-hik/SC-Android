package sc.denishik.ru.mapsApi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.*;
import com.yandex.mapkit.mapview.MapView;
import sc.denishik.ru.HomeActivity;
import sc.denishik.ru.R;
import sc.denishik.ru.dgisApi.DgisMapController;
import sc.denishik.ru.yandexApi.YandexMapController;

public final class MapsController {
    public static final int LOCATION_REQUEST = 710;
    private final HomeActivity activity;
    private final PlacesStore store;
    private final MapView mapView;
    private final YandexMapController yandex;
    private final DgisMapController dgis;
    private final ModalPlaces places;
    private final TextView status;
    private final LocationManager gps;
    private final LocationListener listener;
    private Point position;
    private long positionAt;
    private boolean riding, started, following = true;
    private SavedPlace destination;
    private boolean pendingRoute;
    private boolean speedometer;
    private float heading;
    private final com.yandex.mapkit.user_location.UserLocationLayer userLocation;
    private final sc.denishik.ru.yandexApi.LocationIconController locationIcon;

    public MapsController(HomeActivity activity) {
        this.activity = activity; store = new PlacesStore(activity);
        mapView = activity.findViewById(R.id.mapview1);
        status = activity.findViewById(R.id.map_status);
        yandex = new YandexMapController(mapView);
        userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapView.getMapWindow());
        userLocation.setVisible(true);
        userLocation.setHeadingEnabled(true);
        locationIcon = new sc.denishik.ru.yandexApi.LocationIconController(activity, userLocation);
        dgis = new DgisMapController(activity.findViewById(R.id.dgis_map), status);
        WebView web = activity.findViewById(R.id.dgis_map);
        android.widget.RelativeLayout.LayoutParams webLayout = (android.widget.RelativeLayout.LayoutParams) web.getLayoutParams();
        float density = activity.getResources().getDisplayMetrics().density;
        webLayout.topMargin = (int) (170 * density);
        webLayout.bottomMargin = (int) (126 * density);
        web.setLayoutParams(webLayout);
        places = new ModalPlaces(activity, store, yandex, this::refreshPlaces);
        gps = MapKitFactory.getInstance().createLocationManager();
        listener = new LocationListener() {
            @Override public void onLocationUpdated(Location location) {
                if ("none".equals(store.provider())) return;
                position = location.getPosition(); positionAt = SystemClock.elapsedRealtime();
                heading = location.getHeading() == null ? 0 : location.getHeading().floatValue();
                if (speedometer) { positionSpeedometer(); return; }
                if (pendingRoute && destination != null) { pendingRoute = false; buildRoute(); return; }
                if ("yandex".equals(store.provider()) && (riding || following)) {
                    yandex.locate(position, riding, location.getHeading() == null ? 0 : location.getHeading().floatValue());
                    following = false;
                }

            }
            @Override public void onLocationStatusUpdated(LocationStatus value) {
                if (value != LocationStatus.AVAILABLE) status.setText("Ожидаем местоположение…");
            }
        };
        activity.findViewById(R.id.image).setVisibility(View.GONE);
        activity.findViewById(R.id.home).setOnClickListener(v -> selectPlace("home"));
        activity.findViewById(R.id.garden).setOnClickListener(v -> selectPlace("garden"));
        activity.findViewById(R.id.edit_places).setOnClickListener(v -> places.show());
        activity.findViewById(R.id.search_route).setOnClickListener(v -> places.searchRoute(place -> {
            destination = new SavedPlace(place.address, place.address, place.latitude, place.longitude);
            buildRoute();
        }));
        activity.findViewById(R.id.locate_me).setOnClickListener(v -> locate());
        activity.findViewById(R.id.open_dgis_browser).setOnClickListener(v -> dgis.openInBrowser());
        activity.findViewById(R.id.clear_route).setOnClickListener(v -> {
            destination = null; pendingRoute = false; yandex.clearRoute();
            activity.findViewById(R.id.clear_route).setVisibility(View.GONE);
            status.setText("");
            updateRoutePanel();
            locate();
        });
        refreshPlaces(); selectProvider(store.provider());
    }
    private void refreshPlaces() {
        for (String slot : new String[]{"home", "garden"}) {
            SavedPlace place = store.get(slot);
            TextView label = activity.findViewById("home".equals(slot) ? R.id.home_label : R.id.garden_label);
            label.setText(place == null ? ModalPlaces.defaultTitle(slot) : place.title);
            activity.findViewById("home".equals(slot) ? R.id.home : R.id.garden).setContentDescription(
                    place == null ? "Добавить адрес: " + ModalPlaces.defaultTitle(slot) : "Маршрут: " + place.title + ", " + place.address);
        }
    }
    private void updateRoutePanel() {
        ((View) status.getParent()).setVisibility(!speedometer && !"none".equals(store.provider())
                && (destination != null || "dgis".equals(store.provider())) ? View.VISIBLE : View.GONE);
    }
    public String provider() { return store.provider(); }
    private final android.graphics.Paint recordingPaint = new android.graphics.Paint(
            android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.FILTER_BITMAP_FLAG);
    private final android.graphics.Path recordingPath = new android.graphics.Path();

    /** Draw only our route and marker, projected through the current 3D camera. */
    public void drawRecordingMap(android.graphics.Canvas canvas) {
        if (!"yandex".equals(store.provider()) || !started) return;
        canvas.save();
        canvas.clipRect(0, 0, canvas.getWidth(), canvas.getHeight() * .56f);
        com.yandex.mapkit.geometry.Polyline route = yandex.recordingRoute();
        float density = activity.getResources().getDisplayMetrics().density;
        if (route != null) {
            recordingPath.reset();
            boolean connected = false;
            java.util.List<Point> points = route.getPoints();
            int step = Math.max(1, points.size() / 1500);
            for (int i = 0; i < points.size(); i = Math.min(points.size() - 1, i + step)) {
                com.yandex.mapkit.ScreenPoint point = mapView.getMapWindow().worldToScreen(points.get(i));
                if (point == null) connected = false;
                else {
                    if (connected) recordingPath.lineTo(point.getX(), point.getY());
                    else recordingPath.moveTo(point.getX(), point.getY());
                    connected = true;
                }
                if (i == points.size() - 1) break;
            }
            recordingPaint.setStyle(android.graphics.Paint.Style.STROKE);
            recordingPaint.setStrokeJoin(android.graphics.Paint.Join.ROUND);
            recordingPaint.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            recordingPaint.setStrokeWidth(5 * density);
            recordingPaint.setColor(0xff3169eb);
            canvas.drawPath(recordingPath, recordingPaint);
        }
        if (hasPosition()) {
            com.yandex.mapkit.ScreenPoint point = mapView.getMapWindow().worldToScreen(position);
            android.graphics.Bitmap icon = locationIcon.recordingIcon();
            if (point != null && icon != null) {
                float r = 24 * density;
                recordingPaint.setStyle(android.graphics.Paint.Style.FILL);
                canvas.drawBitmap(icon, null, new android.graphics.RectF(point.getX()-r, point.getY()-r,
                        point.getX()+r, point.getY()+r), recordingPaint);
            }
        }
        canvas.restore();
    }
    private void positionSpeedometer() {
        if (position == null || mapView.getWidth() == 0) return;
        mapView.getMap().move(new com.yandex.mapkit.map.CameraPosition(position, 18f, heading, 60f),
                new com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, .5f), null);
    }
    public void speedometer(boolean enabled) {
        speedometer = enabled;
        updateRoutePanel();
        if (enabled && "none".equals(store.provider())) {
            mapView.setVisibility(View.GONE); dgis.hide();
            activity.findViewById(R.id.image).setVisibility(View.GONE);
            return;
        }
        if (enabled) {
            dgis.hide(); mapView.setVisibility(View.VISIBLE);
            mapView.post(() -> {
                if (!speedometer) return;
                float x=mapView.getWidth()*.5f, y=mapView.getHeight()*.40f;
                mapView.getMapWindow().setFocusPoint(new com.yandex.mapkit.ScreenPoint(x,y));
                userLocation.setAnchor(new android.graphics.PointF(x,y), new android.graphics.PointF(x,y));
                mapView.getMap().getLogo().setPadding(new com.yandex.mapkit.logo.Padding(12,
                        (int)(mapView.getHeight() * .45f)));
                positionSpeedometer();
            });
        } else {
            mapView.getMapWindow().setFocusPoint(null); userLocation.resetAnchor();
            mapView.getMap().getLogo().setPadding(new com.yandex.mapkit.logo.Padding(12,(int)(136*activity.getResources().getDisplayMetrics().density)));
            selectProvider(store.provider());
        }
    }
    public TravelMode travelMode() { return store.travelMode(); }
    public void travelMode(TravelMode mode) {
        store.travelMode(mode);
        if (destination != null && "yandex".equals(store.provider())) buildRoute();
    }
    public void locationIcon(String name) { locationIcon.select(name); }
    public void importLocationIcon(android.net.Uri uri) {
        locationIcon.importImage(uri, ok -> Toast.makeText(activity,
                ok ? "Иконка местоположения сохранена" : "Не удалось загрузить картинку. Выберите другой файл.", Toast.LENGTH_LONG).show());
    }
    public void selectProvider(String provider) {
        store.provider("none".equals(provider) ? "none" : "dgis".equals(provider) ? "dgis" : "yandex");
        boolean hidden = "none".equals(store.provider());
        activity.setMapEnabled(!hidden);
        activity.findViewById(R.id.image).setVisibility(hidden && !speedometer ? View.VISIBLE : View.GONE);
        if (hidden) {
            places.dismiss(); pendingRoute = false; destination = null;
            yandex.clearRoute(); mapView.setVisibility(View.GONE); dgis.hide();
            gps.unsubscribe(listener);
            activity.findViewById(R.id.clear_route).setVisibility(View.GONE);
            activity.findViewById(R.id.open_dgis_browser).setVisibility(View.GONE);
            status.setText(""); updateRoutePanel();
            return;
        }
        if (started) requestLocation();
        boolean nativeMap = "yandex".equals(store.provider());
        mapView.setVisibility(nativeMap ? View.VISIBLE : View.GONE);
        activity.findViewById(R.id.open_dgis_browser).setVisibility(nativeMap ? View.GONE : View.VISIBLE);
        if (nativeMap) dgis.hide(); else dgis.show();
        status.setText(nativeMap ? "" : "2ГИС • веб-карта");
        updateRoutePanel();
        if (destination != null) buildRoute();
        else if (nativeMap && position != null) yandex.locate(position, riding, 0);
    }
    private boolean hasPosition() { return position != null && SystemClock.elapsedRealtime() - positionAt < 120000; }
    private void selectPlace(String slot) {
        SavedPlace place = store.get(slot);
        if (place == null) { places.edit(slot, () -> selectPlace(slot)); return; }
        destination = place; buildRoute();
    }
    private void buildRoute() {
        if ("none".equals(store.provider())) return;
        updateRoutePanel();
        activity.findViewById(R.id.clear_route).setVisibility(View.VISIBLE);
        if (!hasPosition()) {
            pendingRoute = true;
            status.setText("Ожидаем местоположение для построения маршрута…");
            requestLocation(); return;
        }
        following = false;
        pendingRoute = false;
        activity.findViewById(R.id.clear_route).setVisibility(View.VISIBLE);
        status.setText("Строим маршрут • " + destination.title);
        if ("dgis".equals(store.provider())) {
            yandex.clearRoute(); dgis.route(position.getLatitude(), position.getLongitude(), destination);
            status.setText("2ГИС • запрос маршрута к «" + destination.title + "»");
        } else yandex.route(position, destination, store.travelMode(), message -> status.setText(message));
        activity.collapseForMap();
    }
    private void locate() {
        if (!hasPosition()) { following = true; requestLocation(); status.setText("Ожидаем местоположение…"); return; }
        if ("dgis".equals(store.provider())) dgis.locate(position.getLatitude(), position.getLongitude());
        else yandex.locate(position, riding, 0);
    }
    public void ride(boolean value) {
        riding = value;
        if (value) places.dismiss();
        if (speedometer) positionSpeedometer();
        else if (position != null && "yandex".equals(store.provider())) yandex.locate(position, riding, 0);
    }
    public void requestLocation() {
        if ("none".equals(store.provider())) { gps.unsubscribe(listener); return; }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (started) { gps.unsubscribe(listener); gps.subscribeForLocationUpdates(0, 1000, 0, false, FilteringMode.ON, listener); }
        } else ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
    }
    public void start() { started = true; mapView.onStart(); dgis.resume(); requestLocation(); }
    public void stop() { started = false; gps.unsubscribe(listener); mapView.onStop(); dgis.pause(); }
    public void destroy() { places.dismiss(); gps.unsubscribe(listener); locationIcon.destroy(); userLocation.setObjectListener(null); yandex.destroy(); dgis.destroy(); }
}
