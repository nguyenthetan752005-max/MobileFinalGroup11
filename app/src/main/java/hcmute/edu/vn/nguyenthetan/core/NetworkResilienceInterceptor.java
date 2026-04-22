package hcmute.edu.vn.nguyenthetan.core;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class NetworkResilienceInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(chain.request());
        } catch (SocketTimeoutException | UnknownHostException e) {
            SessionEventBus.postNetworkOffline("Mất kết nối server. Tiến trình đang được xử lý ngoại tuyến.");
            throw e;
        }
    }
}
