package hcmute.edu.vn.nguyenthetan.core;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionStore {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "jwt_token";

    private final Context appContext;
    private final SharedPreferences preferences;

    public UserSessionStore(Context context) {
        appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(Long userId, String username, String email, String token) {
        preferences.edit()
                .putLong(KEY_USER_ID, userId == null ? -1L : userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .putString(KEY_TOKEN, token)
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
        return preferences.getString(KEY_TOKEN, "");
    }

    public boolean isLoggedIn() {
        return getUserId() > 0L;
    }

    public void clear() {
        preferences.edit().clear().apply();
        AppearancePreferenceStore.clearSessionOverride(appContext);
    }
}
