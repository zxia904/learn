import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;


public class SignInWithAppleHelper {

    JSONArray keysJsonArray;

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {

        //ios传给后端的jwt串
        String jwt="eyJraWQiOiJlWGF1bm1MIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2FwcGxlaWQuYXBwbGUuY29tIiwiYXVkIjoiY29tLmtlYmlkYS5kdXNodSIsImV4cCI6MTU4NjUwNzEzMSwiaWF0IjoxNTg2NTA2NTMxLCJzdWIiOiIwMDE2OTcuNmFiZWVhYzgyZDM1NDhjYThlM2E2MzU4ODBkNDJhZDkuMDYxNCIsImNfaGFzaCI6Ind3d1RCb2l0ZVdwS1hHLVpNNjNIVlEiLCJlbWFpbCI6Im41cmR3eTJkOTVAcHJpdmF0ZXJlbGF5LmFwcGxlaWQuY29tIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiaXNfcHJpdmF0ZV9lbWFpbCI6InRydWUiLCJhdXRoX3RpbWUiOjE1ODY1MDY1MzEsIm5vbmNlX3N1cHBvcnRlZCI6dHJ1ZX0.1urNEFeyZVzdZfGY2Km2B93zxKECSHbRYVK8JPS50K8Cbx3EZSgr5rsrd53h31eP_nB3G7Qyw38fiSSqmh2t-SSESiRgTGd9op0QzUVw6C9P1Jnfa7LvaQ8YTYWpZPNON0glkSppTfXp_zb8qg-kNawLS_rje1hN3dA7NUaNVAV7VgPeLjnwvRp5uwq1k5joHTj8ex8o1Thfemml9gD6XWpC8tbCwUzWMwsgeguu5DXj3veEtERDHJyjt9Jnc269PMU4mxNNpkzGvUV6YkcXYWnIiCuwgl7Kyu1cXQJ2jMhIJT_fND9frQFN2rqrmiPWCvy7uI_cpSbB1qdWtj-XXQ";

        SignInWithAppleHelper signInWithAppleHelper = new SignInWithAppleHelper();
        System.out.println(signInWithAppleHelper.verify(jwt));
    }

    /**
     * 解密个人信息
     *
     * @param identityToken APP获取的identityToken
     * @return 解密参数：失败返回null
     */
    public Map<String, Object> verify(String identityToken) {
        String sub = "";
        boolean result = false;
        Map<String, Object> data = null;
        try {
            String[] identityTokens = identityToken.split("\\.");
            Map<String, Object> data0 = JSONObject.parseObject(new String(Base64Decoder.decode(identityTokens[0]), StandardCharsets.UTF_8));
            data = JSONObject.parseObject(new String(Base64Decoder.decode(identityTokens[1]), StandardCharsets.UTF_8));
            String aud = (String) data.get("aud");
            sub = (String) data.get("sub");
            String kid = (String) data0.get("kid");
            result = verify(identityToken, aud, sub, kid);
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            // throw new PassportException(APIResultStatus.UC_EXPRIED_JWT.getCode(), APIResultStatus.UC_EXPRIED_JWT.getMsg());
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                updateAppleKeys();
            }
            e.printStackTrace();
            // throw new PassportException(APIResultStatus.UC_VERIFY_FAIL.getCode(), APIResultStatus.UC_VERIFY_FAIL.getMsg());
        }
        if (!result) {
            return null;
        }
        return data;
    }

    /**
     * 验证
     *
     * @param identityToken APP获取的identityToken
     * @param aud           您在您的Apple Developer帐户中的client_id
     * @param sub           用户的唯一标识符对应APP获取到的：user
     * @return true/false
     */
    public boolean verify(String identityToken, String aud, String sub, String kid) {
        PublicKey publicKey = getPublicKey(kid);
        JwtParser jwtParser = Jwts.parser().setSigningKey(publicKey);
        jwtParser.requireIssuer("https://appleid.apple.com");
        jwtParser.requireAudience(aud);
        jwtParser.requireSubject(sub);
        Jws<Claims> claim = jwtParser.parseClaimsJws(identityToken);
        if (claim != null && claim.getBody().containsKey("auth_time")) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return 构造好的公钥
     */
    public PublicKey getPublicKey(String kid) {
        try {
            if (keysJsonArray == null || keysJsonArray.size() == 0) {
                updateAppleKeys();
            }
            String n = "";
            String e = "";
            for (int i = 0; i < keysJsonArray.size(); i++) {
                JSONObject jsonObject = keysJsonArray.getJSONObject(i);
                if (jsonObject.getString("kid").equals(kid)) {
                    n = jsonObject.getString("n");
                    e = jsonObject.getString("e");
                }
            }
            final BigInteger modulus = new BigInteger(1, Base64Decoder.decode(n));
            final BigInteger publicExponent = new BigInteger(1, Base64Decoder.decode(e));

            final RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateAppleKeys() {
        HttpRequest get = HttpUtil.createGet("https://appleid.apple.com/auth/keys");
        String body = get.execute().body();
        if (body == null || body.trim().length() == 0) {
            return;
        }
        JSONObject data = JSONObject.parseObject(body);
        keysJsonArray = data.getJSONArray("keys");
    }

}
