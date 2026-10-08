package sc.denishik.ru.tuya;

import android.content.SharedPreferences;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class pbbppqb {
    public abstract boolean bdpdqbp(Exception var1, KeyStore var2, List<String> var3, SharedPreferences var4);

    public void bdpdqbp(KeyStore var1, List<String> var2) throws KeyStoreException {
        if (var1 != null && var2 != null) {
            Iterator var3 = var2.iterator();

            while(var3.hasNext()) {
                String var4;
                if (var1.containsAlias(var4 = (String)var3.next())) {
                    var1.deleteEntry(var4);
                }
            }
        }

    }

    public void bdpdqbp(KeyStore var1) throws KeyStoreException {
        if (var1 != null) {
            Iterator var3 = Collections.list(var1.aliases()).iterator();

            while(var3.hasNext()) {
                String var2;
                if (var1.containsAlias(var2 = (String)var3.next())) {
                    var1.deleteEntry(var2);
                }
            }
        }

    }

    public void bdpdqbp(SharedPreferences var1) {
        if (var1 != null) {
            var1.edit().clear().apply();
        }

    }
}