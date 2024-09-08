package kr.ychoi.otplugin;

import java.awt.Window;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.json.*;

import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.machinetranslators.BaseCachedTranslate;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.core.machinetranslators.MachineTranslators;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

public class DeepLAPIFree extends BaseCachedTranslate {
    private static final String API_KEY = System.getProperty("deepl-api-free.api.key").trim();
    private static final String API_URL = "https://api-free.deepl.com/v2/translate";
    private static final String ALLOW_DEEPL_API_FREE = "allow_deep_api_free";
    protected static final ResourceBundle res = ResourceBundle.getBundle("DeepLAPIFree", Locale.getDefault());

    @Override
    protected String getPreferenceName() {
        return ALLOW_DEEPL_API_FREE;
    }

    @Override
    public String getName() {
        if (API_KEY == null) {
            return res.getString("DEEPL_KEY_REQUIRED");
        } else {
            return res.getString("DEEPL_NAME");
        }
    }

    // Plugin setup
    public static void loadPlugins() {
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                MachineTranslators.add(new DeepLAPIFree());
            }

            @Override
            public void onApplicationShutdown() {
                /* empty */
            }
        });
    }

    public static void unloadPlugins() {
        /* empty */
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        if (API_KEY == null) {
            return res.getString("KEY_NOT_FOUND");
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
            return res.getString("TRANSLATION_FAILED");
        } catch (Exception e) {
            return res.getString("ERROR_CONTACT") + e.getMessage();
        }
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {
        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                String key = panel.valueField1.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(API_KEY, key, temporary);
            }
        };
        dialog.panel.valueLabel1.setText(getName());
        dialog.panel.valueField1.setText(getCredential(API_KEY));
        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(API_KEY));
        dialog.show();
    }
}
