package sc.denishik.ru.yandexApi;

import android.graphics.Color;
import androidx.annotation.NonNull;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.GeoObject;
import com.yandex.mapkit.GeoObjectCollection;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.search.*;
import com.yandex.mapkit.transport.TransportFactory;
import com.yandex.mapkit.transport.masstransit.PedestrianRouter;
import com.yandex.mapkit.transport.masstransit.Route;
import com.yandex.mapkit.transport.masstransit.TimeOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sc.denishik.ru.mapsApi.SavedPlace;

public final class YandexMapController {
    public interface SearchResult { void complete(List<SavedPlace> places, String error); }
    public interface RouteResult { void complete(String message); }
    private final MapView view;
    private final MapObjectCollection routes;
    private com.yandex.mapkit.geometry.Polyline recordingRoute;
    public com.yandex.mapkit.geometry.Polyline recordingRoute() { return recordingRoute; }
    private final SearchManager search = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
    private final PedestrianRouter router = TransportFactory.getInstance().createPedestrianRouter();
    private com.yandex.mapkit.search.Session searchSession;
    private com.yandex.mapkit.search.Session.SearchListener searchListener;
    private com.yandex.mapkit.transport.masstransit.Session routeSession;
    private com.yandex.mapkit.transport.masstransit.Session.RouteListener routeListener;
    private int searchGeneration, routeGeneration;
    private final com.yandex.mapkit.transport.masstransit.BicycleRouterV2 bicycle = TransportFactory.getInstance().createBicycleRouterV2();
    private final com.yandex.mapkit.transport.masstransit.BicycleRouterV2 scooter = TransportFactory.getInstance().createScooterRouter();
    private final com.yandex.mapkit.directions.driving.DrivingRouter driving = com.yandex.mapkit.directions.DirectionsFactory.getInstance().createDrivingRouter();
    private com.yandex.mapkit.directions.driving.DrivingSession drivingSession;
    private com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener drivingListener;

    public YandexMapController(MapView view) {
        this.view = view;
        routes = view.getMap().getMapObjects().addCollection();
        view.setVisibility(android.view.View.VISIBLE);
        view.getMap().getLogo().setPadding(new com.yandex.mapkit.logo.Padding(12,
                (int) (136 * view.getResources().getDisplayMetrics().density)));
    }
    public void locate(Point point, boolean riding, float heading) {
        view.getMap().move(new CameraPosition(point, riding ? 17.5f : 16f, riding ? heading : 0, riding ? 40 : 0),
                new Animation(Animation.Type.SMOOTH, 0.5f), null);
    }
    public void search(String query, SearchResult result) {
        cancelSearch();
        final int request = searchGeneration;
        searchListener = new com.yandex.mapkit.search.Session.SearchListener() {
            public void onSearchResponse(@NonNull Response response) {
                if (request != searchGeneration) return;
                List<SavedPlace> places = new ArrayList<>();
                for (GeoObjectCollection.Item item : response.getCollection().getChildren()) {
                    GeoObject object = item.getObj();
                    if (object == null) continue;
                    for (Geometry geometry : object.getGeometry()) {
                        Point point = geometry.getPoint();
                        if (point == null) continue;
                        String label = object.getName() == null ? query : object.getName();
                        if (object.getDescriptionText() != null) label += ", " + object.getDescriptionText();
                        places.add(new SavedPlace("", label, point.getLatitude(), point.getLongitude()));
                        break;
                    }
                }
                result.complete(places, places.isEmpty() ? "Адрес не найден. Укажите город, улицу и дом." : null);
            }
            public void onSearchError(@NonNull com.yandex.runtime.Error error) {
                if (request == searchGeneration) result.complete(new ArrayList<>(), "Не удалось найти адрес. Проверьте интернет и повторите поиск.");
            }
        };
        searchSession = search.submit(query, Geometry.fromPoint(view.getMap().getCameraPosition().getTarget()),
                new SearchOptions().setResultPageSize(5), searchListener);
    }
    public void route(Point from, SavedPlace to, sc.denishik.ru.mapsApi.TravelMode mode, RouteResult result) {
        clearRoute();
        final int request = routeGeneration;
        routeListener = new com.yandex.mapkit.transport.masstransit.Session.RouteListener() {
            public void onMasstransitRoutes(@NonNull List<Route> values) {
                if (request != routeGeneration) return;
                if (values.isEmpty()) { result.complete("Маршрут не найден"); return; }
                drawRoute(values.get(0).getGeometry());
                result.complete(mode.label + " • " + to.title);
            }
            public void onMasstransitRoutesError(@NonNull com.yandex.runtime.Error error) {
                if (request == routeGeneration) result.complete("Маршрут недоступен. Проверьте интернет и повторите.");
            }
        };
        List<RequestPoint> points = Arrays.asList(new RequestPoint(from, RequestPointType.WAYPOINT, null),
                new RequestPoint(new Point(to.latitude, to.longitude), RequestPointType.WAYPOINT, null));
        switch (mode) {
            case SCOOTER: routeSession = scooter.requestRoutes(points, new TimeOptions(), routeListener); break;
            case BICYCLE: routeSession = bicycle.requestRoutes(points, new TimeOptions(), routeListener); break;
            case WALK: routeSession = router.requestRoutes(points, new TimeOptions(), routeListener); break;
            case CAR:
                drivingListener = new com.yandex.mapkit.directions.driving.DrivingSession.DrivingRouteListener() {
                    public void onDrivingRoutes(@NonNull List<com.yandex.mapkit.directions.driving.DrivingRoute> values) {
                        if (request != routeGeneration) return;
                        if (values.isEmpty()) { result.complete("Маршрут не найден"); return; }
                        drawRoute(values.get(0).getGeometry()); result.complete(mode.label + " • " + to.title);
                    }
                    public void onDrivingRoutesError(@NonNull com.yandex.runtime.Error error) {
                        if (request == routeGeneration) result.complete("Автомаршрут недоступен. Повторите запрос или выберите другой тип.");
                    }
                };
                drivingSession = driving.requestRoutes(points, new com.yandex.mapkit.directions.driving.DrivingOptions(),
                        new com.yandex.mapkit.directions.driving.VehicleOptions(), drivingListener);
                break;
        }
    }
    private void drawRoute(com.yandex.mapkit.geometry.Polyline geometry) {
        recordingRoute = geometry;
        com.yandex.mapkit.map.PolylineMapObject line = routes.addPolyline(geometry);
        line.setStrokeColor(Color.rgb(49, 105, 235)); line.setStrokeWidth(5);
        CameraPosition camera = view.getMap().cameraPosition(com.yandex.mapkit.geometry.BoundingBoxHelper.getBounds(geometry));
        view.getMap().move(new CameraPosition(camera.getTarget(), Math.max(1, camera.getZoom() - 0.7f), 0, 0));
    }

    public void cancelSearch() { searchGeneration++; if (searchSession != null) searchSession.cancel(); searchSession = null; }
    public void clearRoute() { recordingRoute = null; routeGeneration++; if (drivingSession != null) drivingSession.cancel(); drivingSession = null; if (routeSession != null) routeSession.cancel(); routeSession = null; routes.clear(); }
    public void destroy() { cancelSearch(); clearRoute(); }
}
