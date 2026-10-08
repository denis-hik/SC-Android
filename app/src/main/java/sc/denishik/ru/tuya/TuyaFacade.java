package sc.denishik.ru.tuya;

import android.app.Application;

public interface TuyaFacade {
    boolean isAvailable();

    void init(Application application) throws Exception;

    boolean isLoggedIn() throws Exception;

    void loginWithEmail(String countryCode, String email, String password) throws Exception;

    void sendRegisterCode(String countryCode, String email) throws Exception;

    void registerWithEmail(String countryCode, String email, String password, String code) throws Exception;

    void logout() throws Exception;
}
