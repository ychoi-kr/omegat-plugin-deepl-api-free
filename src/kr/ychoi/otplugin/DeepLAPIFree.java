package kr.ychoi.otplugin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.omegat.core.machinetranslators.BaseCachedTranslate;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.json.*;

public class DeepLAPIFree extends BaseCachedTranslate {
    private static final String API_KEY = System.getProperty("deepl-api-free.api.key");
    private static final String API_URL = "https://api-free.deepl.com/v2/translate";

    @Override
    protected String getPreferenceName() {
        return "allow_deep_api_free";
    }

    @Override
    public String getName() {
        if (API_KEY == null) {
            return "DeepL API Free (API Key Required)";
        } else {
            return "DeepL API Free";
        }
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        if (API_KEY == null) {
            return "API key not found";
        }
        
        String cachedResult = getCachedTranslation(sLang, tLang, text);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Map<String, String> params = new HashMap<>();
        params.put("text", text);
        params.put("target_lang", tLang.getLanguageCode().toUpperCase());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "DeepL-Auth-Key " + API_KEY);
        String result = "";

        try {
            // HttpConnectionUtils.post 메서드를 사용하여 form-urlencoded 데이터를 전송
            String response = new String(HttpConnectionUtils.post(API_URL, params, headers).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            JSONObject jsonResponse = new JSONObject(response);

            JSONArray translations = jsonResponse.getJSONArray("translations");
            if (translations.length() > 0) {
                result = translations.getJSONObject(0).getString("text").trim();
                putToCache(sLang, tLang, text, result);
                return result;
            }
            return "Translation failed";
        } catch (Exception e) {
            return "Error contacting DeepL API: " + e.getMessage();
        }
    }

}
