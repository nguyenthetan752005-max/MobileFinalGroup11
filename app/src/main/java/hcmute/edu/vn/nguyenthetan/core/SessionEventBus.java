package hcmute.edu.vn.nguyenthetan.core;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public final class SessionEventBus {

    private static final MutableLiveData<String> ACCOUNT_LOCKED_MESSAGE = new MutableLiveData<>();

    private SessionEventBus() {
    }

    public static LiveData<String> getAccountLockedMessage() {
        return ACCOUNT_LOCKED_MESSAGE;
    }

    public static void postAccountLocked(String message) {
        ACCOUNT_LOCKED_MESSAGE.postValue(message);
    }

    public static void clearAccountLocked() {
        ACCOUNT_LOCKED_MESSAGE.postValue(null);
    }
}
