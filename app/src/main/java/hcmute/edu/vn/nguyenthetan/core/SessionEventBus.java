package hcmute.edu.vn.nguyenthetan.core;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public final class SessionEventBus {

    private static final MutableLiveData<String> ACCOUNT_LOCKED_MESSAGE = new MutableLiveData<>();
    private static final MutableLiveData<String> NETWORK_OFFLINE_MESSAGE = new MutableLiveData<>();

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

    public static LiveData<String> getNetworkOfflineMessage() {
        return NETWORK_OFFLINE_MESSAGE;
    }

    public static void postNetworkOffline(String message) {
        NETWORK_OFFLINE_MESSAGE.postValue(message);
    }

    public static void clearNetworkOffline() {
        NETWORK_OFFLINE_MESSAGE.postValue(null);
    }
}
