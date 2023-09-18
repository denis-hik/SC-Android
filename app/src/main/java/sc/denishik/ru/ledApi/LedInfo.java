package sc.denishik.ru.ledApi;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class LedInfo {
    private String id;
    private boolean isConnected;
    private String message;
    private String date;

    public LedInfo(String message, boolean isConnected) {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        this.id = generatedString;
        this.message = message;
        this.isConnected = isConnected;
        this.date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
    }

    public String getMessage() {
        return this.message;
    }
    public String getId() {
        return this.id;
    }
    public String getDate() {
        return this.date;
    }
    public boolean getIsConnected() {
        return this.isConnected;
    }
}
