package sc.denishik.ru.dgisApi;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.webkit.*;
import android.view.View;
import sc.denishik.ru.mapsApi.SavedPlace;

/** Official 2GIS website; does not use undocumented tiles or SDK credentials. */
public final class DgisMapController {
    private final WebView view;
    private boolean loaded;
    @SuppressLint("SetJavaScriptEnabled")
    public DgisMapController(WebView view, android.widget.TextView status) {
        this.view = view;
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setUserAgentString(view.getSettings().getUserAgentString().replace("; wv", "").replace(" Version/4.0", ""));
        view.getSettings().setAllowFileAccess(false);
        view.getSettings().setAllowContentAccess(false);
        view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        view.setWebViewClient(new WebViewClient() {
            @Override public void onReceivedSslError(WebView web, SslErrorHandler handler, android.net.http.SslError error) {
                handler.cancel();
                android.util.Log.w("DgisMap", "TLS error " + error.getPrimaryError() + " host=" + Uri.parse(error.getUrl()).getHost());
                if (error.getUrl().equals(web.getUrl())) {
                    status.setText("2ГИС: ошибка защищённого соединения. Проверьте дату телефона и сеть.");
                    loaded = false;
                }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView web, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (!"https".equals(uri.getScheme()) || host == null) return true;
                for (String domain : new String[]{"2gis.ru", "2gis.kz", "2gis.kg", "2gis.uz", "2gis.com", "2gis.by", "2gis.az", "urbi.ae"}) {
                    if (host.equals(domain) || host.endsWith("." + domain)) return false;
                }
                return true;
            }
            @Override public void onReceivedError(WebView web, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) { loaded = false; status.setText("2ГИС недоступен. Проверьте интернет и выберите карту снова."); }
            }
        });
    }
    public void show() { view.setVisibility(View.VISIBLE); if (!loaded) { loaded = true; view.loadUrl("https://2gis.ru/"); } }
    public void route(double lat, double lon, SavedPlace destination) {
        show();
        view.loadUrl("https://2gis.ru/routeSearch/rsType/pedestrian/from/" + lon + "," + lat
                + "/to/" + destination.longitude + "," + destination.latitude);
    }
    public void locate(double lat, double lon) {
        show(); view.loadUrl("https://2gis.ru/geo/" + lon + "," + lat + "?m=" + lon + "," + lat + "%2F16");
    }
    public void hide() { view.setVisibility(View.GONE); }
    public void openInBrowser() {
        String url = view.getUrl();
        try {
            view.getContext().startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(url == null ? "https://2gis.ru/" : url)));
        } catch (android.content.ActivityNotFoundException error) {
            android.widget.Toast.makeText(view.getContext(), "Установите браузер для открытия 2ГИС", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    public void resume() { view.onResume(); }
    public void pause() { view.onPause(); }
    public void destroy() { view.stopLoading(); view.destroy(); }
}
