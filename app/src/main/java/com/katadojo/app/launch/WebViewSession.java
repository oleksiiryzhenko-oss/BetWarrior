package com.katadojo.app.launch;

import android.content.Context;
import android.content.SharedPreferences;

/** Durable routing state for users who have entered the verification WebView. */
public final class WebViewSession {
    private final SharedPreferences preferences;

    public WebViewSession(Context context) {
        this(context, "sienna_state_y9c3");
    }

    WebViewSession(Context context, String preferenceName) {
        preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }


    public boolean hasOpenedWebView() { return preferences.getBoolean("shown_k5h8", false); }
    public String initialEntryUrl() { return preferences.getString("source_f2p6", null); }
    public String lastWebViewUrl() { return preferences.getString("page_v9m4", null); }
    public boolean lastUrlWorks() { return preferences.getBoolean("valid_d8z1", false); }

    public void recordFirstRedirect(String entryUrl) {
        if (hasOpenedWebView()) return;
        preferences.edit().putBoolean("shown_k5h8", true)
                .putString("source_f2p6", entryUrl).putBoolean("valid_d8z1", false).commit();
    }

    public void recordWorkingPage(String url) {
        preferences.edit().putBoolean("shown_k5h8", true)
                .putString("page_v9m4", url).putBoolean("valid_d8z1", true).apply();
    }

    public void markLastUrlBroken() { preferences.edit().putBoolean("valid_d8z1", false).apply(); }

    public boolean wasTokenAccepted(String token) {
        return tokenHash(token).equals(preferences.getString("token_b6r3", ""));
    }

    public void recordAcceptedToken(String token) {
        preferences.edit().putString("token_b6r3", tokenHash(token)).commit();
    }

    private static String tokenHash(String token) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
