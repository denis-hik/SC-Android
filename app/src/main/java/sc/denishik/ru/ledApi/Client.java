package sc.denishik.ru.ledApi;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import sc.denishik.ru.other.RequestNetwork;
import sc.denishik.ru.other.RequestNetworkController;
import tech.gusavila92.websocketclient.WebSocketClient;


public class Client {
    private final String TAG = "ledApi.Client";
    private String ip_host = Config.IP_HOST_DEFAULT;
    private String port_client = Config.PORT_CLIENT_DEFAULT;
    private String ip_client = Config.IP_CLIENT_DEFAULT;
    private String mask_client = Config.MASK_CLIENT_DEFAULT;
    private String id_board = Config.ID_BOARD;
    private AppCompatActivity activity;
    private WebSocketClient client;
    private CallBack callBack;
    private checkServerCallback callBackCheck;
    private boolean isCheck = false;
    private URI url;

    public static interface CallBack {
        void onOpen();
        void onDiscovered();
        void onGetText(String s);
        void onError(String err);
        void onStop();
    }

    public Client(String ip_host, String ip_client, AppCompatActivity activity, CallBack callBack) {
        this.ip_client = ip_client;
        this.ip_host = ip_host;
        this.activity = activity;
        callBackCheck = new checkServerCallback() {
            @Override
            public void onSuccess() {
                callBack.onDiscovered();
                isCheck = true;
            }

            @Override
            public void onError(String err) {
                callBack.onError(err);
                isCheck = false;
            }
        };
        checkServer(callBackCheck);
        this.callBack = callBack;
    }

    public Client(AppCompatActivity activity, CallBack callBack) {
        this.activity = activity;
        this.callBack = callBack;
        callBackCheck = new checkServerCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess");
                callBack.onDiscovered();
            }

            @Override
            public void onError(String err) {
                callBack.onError(err);
            }
        };
        try {
            url = new URI("ws://".concat(ip_host).concat(":").concat(port_client).concat("/hub/").concat(id_board));
        } catch (Exception e) {

        }
        connectServer();
    }

    public String getUrl() {
        return String.valueOf(url);
    }

    public interface checkServerCallback {
        void onSuccess();
        void onError(String err);
    }
    public void checkServer(checkServerCallback callback) {
//        RequestNetwork requestNetwork = new RequestNetwork(activity);
//        RequestNetwork.RequestListener listener = new RequestNetwork.RequestListener() {
//            @Override
//            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {
//                try {
//                    if (response.contains("OK")) {
//                        isCheck = true;
//                        url = new URI("ws://".concat(ip_host).concat(":").concat(port_client).concat("/hub/").concat(id_board));
//                        Log.i(TAG, "Check>" +  String.valueOf(response));
//                        callback.onSuccess();
//                        return;
//                    }
//                    Log.e(TAG, "Check> not found Ok res:".concat(String.valueOf(response)) );
//                    callback.onError("not found Ok");
//                } catch (Exception e) {
//                    callback.onError(String.valueOf(e.getMessage()));
//                    Log.e(TAG,"Check>" +  String.valueOf(e.getMessage()));
//                }
//            }
//
//            @Override
//            public void onErrorResponse(String tag, String message) {
//                Log.e(TAG,"Check> " +  String.valueOf(message));
//                callback.onError(message);
//            }
//        };
//        Log.d("CheckServer WS", getUrlAll().concat("/hub_discover_all"));
//        requestNetwork.startRequestNetwork(RequestNetworkController.GET, getUrlAll().concat("/hub_discover_all"), null, listener);

    }

    public void connectServer() {
        try {
            Log.i(TAG, "WS onStart url:".concat(String.valueOf(url)));
            client = new WebSocketClient(url) {
                @Override
                public void onOpen() {
                    Log.i(TAG, "WebSocket> Session is starting");
                    Client.this.callBack.onOpen();
                }

                @Override
                public void onTextReceived(String s) {
                    Log.i(TAG, "WebSocket> Message received msg:".concat(s));
                    Client.this.callBack.onGetText(s);
                }

                @Override
                public void onBinaryReceived(byte[] data) {
                    Log.d(TAG,"WebSocket> onBinaryReceived" + String.valueOf(data));
                }

                @Override
                public void onPingReceived(byte[] data) {
                    Log.d(TAG,"WebSocket> onPingReceived".concat(String.valueOf(data)));
                    String s = new String(data, StandardCharsets.UTF_8);
                    Log.d(TAG,"WebSocket> onPingReceived>" + String.valueOf(s));
                }

                @Override
                public void onPongReceived(byte[] data) {
                    Log.d(TAG,"WebSocket> onPongReceived" + String.valueOf(data));
                }

                @Override
                public void onException(Exception e) {
                    Log.e(TAG,"WebSocket>" + String.valueOf(e.getMessage()));
                    Client.this.callBack.onError(String.valueOf(e.getMessage()));
                }

                @Override
                public void onCloseReceived() {
                    Log.i(TAG, "WebSocket> Closed ");
                    Client.this.callBack.onStop();
                }
            };

            client.enableAutomaticReconnection(100);
            client.connect();
        } catch (Exception e) {
            Log.e(TAG, "connectServer: ".concat(String.valueOf(e.getMessage())) );

        }
    }

    private String getUrlAll() {
        return "http://".concat(ip_host.concat(":").concat(port_client));
    }

    public void sendWS(String key, String value) {
        if (client == null) {
            //MyDevices/b4794cda/8a7b88e5/set/_n9=146
            Log.e(TAG, "sendWS: client == null" );
            return;
        }
        String url = "MyDevices/".concat(id_board).concat("/8a7b88e5/set/").concat(key).concat("=".concat(value));
        Log.d(TAG, "onSend> ".concat(url));
        client.send(url);
    }


    public void stop() {
        try {
            client.onCloseReceived();
            client.close();
        } catch (Exception e) {

        }
    }
}
