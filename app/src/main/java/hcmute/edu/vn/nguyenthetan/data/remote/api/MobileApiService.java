package hcmute.edu.vn.nguyenthetan.data.remote.api;

import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MobileApiService {

    @GET("api/mobile/bootstrap")
    Call<MobileBootstrapDto> getBootstrap();
}
