package hcmute.edu.vn.nguyenthetan.ui.auth;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.edu.vn.nguyenthetan.core.SessionEventBus;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;

public final class AccountLockUiHandler {

    public static final String EXTRA_ACCOUNT_LOCKED_MESSAGE = "extra_account_locked_message";

    private AccountLockUiHandler() {
    }

    public static void attach(@NonNull AppCompatActivity activity) {
        SessionEventBus.getAccountLockedMessage().observe(activity, message -> {
            if (message == null || message.trim().isEmpty()) {
                return;
            }
            SessionEventBus.clearAccountLocked();
            Intent intent = new Intent(activity, OnboardingActivity.class);
            intent.putExtra(EXTRA_ACCOUNT_LOCKED_MESSAGE, message);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });

        SessionEventBus.getNetworkOfflineMessage().observe(activity, message -> {
            if (message != null && !message.trim().isEmpty()) {
                android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_LONG).show();
                SessionEventBus.clearNetworkOffline();
            }
        });
    }
}
