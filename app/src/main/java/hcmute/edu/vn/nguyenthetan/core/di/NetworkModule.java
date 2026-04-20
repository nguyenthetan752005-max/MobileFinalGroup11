package hcmute.edu.vn.nguyenthetan.core.di;

import java.util.concurrent.TimeUnit;

import hcmute.edu.vn.nguyenthetan.BuildConfig;
import hcmute.edu.vn.nguyenthetan.core.AccountLockInterceptor;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkModule {

    private NetworkModule() {
    }

    public static MobileApiService createMobileApiService(UserSessionStore userSessionStore) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    String token = userSessionStore.getToken();
                    if (token != null && !token.isEmpty()) {
                        okhttp3.Request.Builder builder = original.newBuilder()
                                .header("Authorization", "Bearer " + token);
                        return chain.proceed(builder.build());
                    }
                    return chain.proceed(original);
                })
                .addInterceptor(new AccountLockInterceptor(userSessionStore))
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.TUNGTUNG_API_BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(MobileApiService.class);
    }
}
