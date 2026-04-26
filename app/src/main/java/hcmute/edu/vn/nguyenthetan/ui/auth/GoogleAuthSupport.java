package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import hcmute.edu.vn.nguyenthetan.BuildConfig;
import hcmute.edu.vn.nguyenthetan.R;

public final class GoogleAuthSupport {

    private static final String TAG = "GoogleAuthSupport";

    public interface Callback {
        void onGoogleIdTokenReceived(@NonNull String idToken);

        void onGoogleAuthLoadingChanged(boolean loading);
    }

    private final AppCompatActivity activity;
    private final Callback callback;
    private final ActivityResultLauncher<Intent> launcher;

    public GoogleAuthSupport(@NonNull AppCompatActivity activity, @NonNull Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Google sign-in activity returned. resultCode=" + result.getResultCode());
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        callback.onGoogleAuthLoadingChanged(false);
                        Log.w(TAG, "Google sign-in canceled or returned empty intent.");
                        return;
                    }
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account == null || account.getIdToken() == null || account.getIdToken().trim().isEmpty()) {
                            callback.onGoogleAuthLoadingChanged(false);
                            Log.w(TAG, "Google account is null or missing ID token.");
                            Toast.makeText(activity, "Failed to get Google ID token.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.d(
                                TAG,
                                "Google ID token received. email=" + account.getEmail()
                                        + ", tokenLength=" + account.getIdToken().length()
                        );
                        callback.onGoogleIdTokenReceived(account.getIdToken());
                    } catch (ApiException exception) {
                        callback.onGoogleAuthLoadingChanged(false);
                        Log.e(TAG, "Google sign-in failed with statusCode=" + exception.getStatusCode(), exception);
                        Toast.makeText(activity, R.string.error_google_login_failed, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void launch() {
        if (BuildConfig.TUNGTUNG_GOOGLE_WEB_CLIENT_ID == null
                || BuildConfig.TUNGTUNG_GOOGLE_WEB_CLIENT_ID.trim().isEmpty()) {
            Log.e(TAG, "Missing Google Web Client ID in BuildConfig.");
            Toast.makeText(activity, R.string.error_google_login_failed, Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(
                TAG,
                "Launching Google sign-in. webClientIdSuffix="
                        + BuildConfig.TUNGTUNG_GOOGLE_WEB_CLIENT_ID.substring(
                        Math.max(0, BuildConfig.TUNGTUNG_GOOGLE_WEB_CLIENT_ID.length() - 12)
                )
        );

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.TUNGTUNG_GOOGLE_WEB_CLIENT_ID)
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(activity, options);
        callback.onGoogleAuthLoadingChanged(true);
        // Sign out trước để xóa cache tài khoản cũ, luôn hiện bảng chọn tài khoản Google
        client.signOut().addOnCompleteListener(activity, task -> launcher.launch(client.getSignInIntent()));
    }
}
