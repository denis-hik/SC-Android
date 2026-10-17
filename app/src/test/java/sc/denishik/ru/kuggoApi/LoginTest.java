package sc.denishik.ru.kuggoApi;
import org.junit.Test;
import org.json.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

public class LoginTest {
 @Test public void http200DoesNotImplySuccess() throws Exception {
  try { Login.unwrap(new JSONObject().put("success",false).put("errorCode","ILLEGAL_CLIENT_ID")); fail(); }
  catch(Login.Failure e){assertEquals("ILLEGAL_CLIENT_ID",e.code);}
 }
 @Test public void backendMessagesCannotLeakThroughErrorCode() throws Exception {
  try { Login.unwrap(new JSONObject().put("errorCode","password: secret")); fail(); }
  catch(Login.Failure e){assertEquals("BACKEND_ERROR",e.code);}
 }
 @Test public void loginUsesNewSessionAndEncryptedPassword() throws Exception {
  KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
  KeyPair pair=generator.generateKeyPair();RSAPublicKey publicKey=(RSAPublicKey)pair.getPublic();
  List<String> calls=new ArrayList<>();
  List<JSONObject> profiles=Login.authenticate((api,version,payload,session)->{
   calls.add(api);
   if(api.endsWith("token.get")){
    assertNull(session);
    return new JSONObject().put("token","test-token").put("publicKey",publicKey.getModulus().toString()).put("exponent",publicKey.getPublicExponent().toString());
   }
   if(api.endsWith("password.login")){
    assertNull(session);assertEquals("test-token",payload.getString("token"));assertEquals(1,payload.getInt("ifencrypt"));
    String encrypted=payload.getString("passwd");byte[] bytes=new byte[encrypted.length()/2];
    for(int i=0;i<bytes.length;i++)bytes[i]=(byte)Integer.parseInt(encrypted.substring(i*2,i*2+2),16);
    Cipher rsa=Cipher.getInstance("RSA/ECB/PKCS1Padding");rsa.init(Cipher.DECRYPT_MODE,pair.getPrivate());
    String hash=new String(rsa.doFinal(bytes),StandardCharsets.UTF_8);
    assertEquals("5f4dcc3b5aa765d61d8327deb882cf99",hash);
    return new JSONObject().put("sid","fresh-session").put("ecode","fresh-ecode");
   }
   assertEquals("fresh-session",session.sid);assertEquals("fresh-ecode",session.ecode);
   if(api.equals("m.life.home.space.list"))return new JSONArray().put(new JSONObject().put("gid",123));
   if(api.equals("smartlife.m.device.ref.info.list")) {
    assertEquals("test-product", payload.getJSONArray("productIds").getString(0));
    return new JSONArray().put(new JSONObject().put("id", "test-product").put("schemaInfo", new JSONObject()));
   }
   assertEquals("m.life.my.group.device.list",api);assertEquals(123,payload.getInt("gid"));
   return new JSONArray().put(new JSONObject().put("devId","test-device").put("uuid","test-uuid").put("productId","test-product").put("localKey","test-key"));
  },"7","test@example.invalid","password");
  assertEquals(5,calls.size());assertEquals(1,profiles.size());assertEquals("test-key",profiles.get(0).getString("localKey"));
  assertEquals("test-product", profiles.get(0).getJSONObject("_product").getString("id"));
 }
}
