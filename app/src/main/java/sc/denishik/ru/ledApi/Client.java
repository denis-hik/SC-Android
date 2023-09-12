package sc.denishik.ru.ledApi;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import sc.denishik.ru.other.RequestNetwork;
import sc.denishik.ru.other.RequestNetworkController;
import tech.gusavila92.websocketclient.WebSocketClient;


public class Client {
    private String ip_host = Config.IP_HOST_DEFAULT;
    private String port_client = Config.PORT_CLIENT_DEFAULT;
    private String ip_client = Config.IP_CLIENT_DEFAULT;
    private String mask_client = Config.MASK_CLIENT_DEFAULT;
    private AppCompatActivity activity;
    private WebSocketClient client;
    private CallBack callBack;
    private URI url;

    public static interface CallBack {
        void onOpen();
        void onGetText(String s);
        void onError(String err);
        void onStop();
    }

    public Client(String ip_host, String ip_client, AppCompatActivity activity, CallBack callBack) {
        this.ip_client = ip_client;
        this.ip_host = ip_host;
        this.activity = activity;
        checkServer();
        this.callBack = callBack;
    }

    public Client(AppCompatActivity activity, CallBack callBack) {
        this.activity = activity;
        this.callBack = callBack;
        checkServer();
    }

    public String getUrl() {
        return String.valueOf(url);
    }

    private void checkServer() {
        try {
            URI url = new URI("ws://".concat(ip_client));
            this.url = url;
            Log.i("WebSocket", "open: ".concat(String.valueOf(url)));
            client = new WebSocketClient(url) {
                @Override
                public void onOpen() {
                    Log.i("WebSocket", "Session is starting");
                    Client.this.callBack.onOpen();
                }

                @Override
                public void onTextReceived(String s) {
                    Log.i("WebSocket", "Message received");
                    final String message = s;
                    Client.this.callBack.onGetText(s);
                }

                @Override
                public void onBinaryReceived(byte[] data) {
                }

                @Override
                public void onPingReceived(byte[] data) {
                }

                @Override
                public void onPongReceived(byte[] data) {
                }

                @Override
                public void onException(Exception e) {
                    Log.e("WebSocket", String.valueOf(e.getMessage()));
                    Client.this.callBack.onError(String.valueOf(e.getMessage()));
                }

                @Override
                public void onCloseReceived() {
                    Log.i("WebSocket", "Closed ");
                    System.out.println("onCloseReceived");
                    Client.this.callBack.onStop();
                }
            };

            client.setConnectTimeout(10000);
            client.setReadTimeout(60000);
            client.connect();
        } catch (Exception e) {


        }
    }

    public void stop() {
        try {
            client.close();
        } catch (Exception e) {

        }
    }
}
