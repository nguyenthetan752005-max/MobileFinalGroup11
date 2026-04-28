package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.GeneralSecurityException;
import java.io.IOException;

/**
 * Stores the active user's JWT in {@link EncryptedSharedPreferences} (AES256-backed).
 * Non-sensitive identity fields (id/username/email) live in a normal SharedPreferences
 * file so that read paths stay cheap and a corrupted keystore can't lock the user out
 * of guest mode.
 *
 * On first run after this change, any plaintext token previously written to the legacy
 * "user_session" file is migrated into the encrypted store and then wiped.
 */
public class UserSessionStore {

    private static final String TAG = "UserSessionStore";

    private static final String PREF_NAME = "user_session";
    private static final String SECURE_PREF_NAME = "user_session_secure";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "jwt_token";

    private final Context appContext;
    private final SharedPreferences preferences;
    private final SharedPreferences securePreferences;

    public UserSessionStore(Context context) {
        appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        securePreferences = createSecurePreferences(appContext);
        migratePlaintextTokenIfNeeded();
    }

    public void saveUser(Long userId, String username, String email, String token) {
        preferences.edit()
                .putLong(KEY_USER_ID, userId == null ? -1L : userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .apply();
        securePreferences.edit()
                .putString(KEY_TOKEN, token == null ? "" : token)
                .apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getToken() {
        return securePreferences.getString(KEY_TOKEN, "");
    }

    public boolean isLoggedIn() {
        return getUserId() > 0L;
    }

    public void clear() {
        preferences.edit().clear().apply();
        securePreferences.edit().clear().apply();
        AppearancePreferenceStore.clearSessionOverride(appContext);
    }

    /**
     * Build an encrypted SharedPreferences instance. If the keystore is unavailable
     * (rare: hardware-backed keystore corruption, downgrade scenarios), fall back to
     * plaintext rather than crashing the app — the user can re-login to recover.
     */
    private static SharedPreferences createSecurePreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "EncryptedSharedPreferences unavailable, falling back to plaintext", e);
            return context.getSharedPreferences(SECURE_PREF_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    /**
     * One-time migration: if the legacy plaintext file still has a JWT under
     * "jwt_token", copy it into the encrypted store and remove the plaintext
     * value so it can't be recovered later.
     */
    private void migratePlaintextTokenIfNeeded() {
        if (!preferences.contains(KEY_TOKEN)) {
            return;
        }
        String legacyToken = preferences.getString(KEY_TOKEN, "");
        if (legacyToken != null && !legacyToken.isEmpty()
                && !securePreferences.contains(KEY_TOKEN)) {
            securePreferences.edit().putString(KEY_TOKEN, legacyToken).apply();
        }
        preferences.edit().remove(KEY_TOKEN).apply();
    }
}
