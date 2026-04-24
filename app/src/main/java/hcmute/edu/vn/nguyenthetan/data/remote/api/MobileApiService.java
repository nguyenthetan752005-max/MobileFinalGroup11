package hcmute.edu.vn.nguyenthetan.data.remote.api;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.remote.dto.AuthResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.CheckDictationRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.CreateCommentRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.DictationResultDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GoogleAuthRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.HealthStatusDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.InProgressLessonDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.LoginRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryCollectionDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLeaderboardDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLessonDetailDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileReminderSettingsDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.NotificationPreferenceRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.ProgressUpdateRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.RegisterRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.SpeakingResultDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.TimeTrackingRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UserProfileDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.UsernameUpdateRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.VoteCommentRequestDto;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface MobileApiService {

    @GET("api/health")
    Call<HealthStatusDto> getHealth();

    @GET("api/mobile/categories")
    Call<List<MobileCategoryDto>> getCategories();

    @GET("api/mobile/categories/{categorySlug}/sections")
    Call<MobileCategoryCollectionDto> getCategoryCollection(@Path("categorySlug") String categorySlug);

    @GET("api/mobile/sections/{sectionId}/lessons")
    Call<List<hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLessonDto>> getSectionLessons(@Path("sectionId") long sectionId);

    @GET("api/mobile/catalog/bootstrap-lite")
    Call<MobileBootstrapDto> getBootstrapLite();

    @GET("api/mobile/lessons/{lessonId}")
    Call<MobileLessonDetailDto> getLessonDetail(@Path("lessonId") long lessonId);

    @GET("api/mobile/bootstrap")
    Call<MobileBootstrapDto> getBootstrap();

    @GET("api/mobile/app-settings/reminder")
    Call<MobileReminderSettingsDto> getReminderSettings();

    @POST("api/mobile/auth/login")
    Call<AuthResponseDto> login(@Body LoginRequestDto request);

    @POST("api/mobile/auth/register")
    Call<AuthResponseDto> register(@Body RegisterRequestDto request);

    @POST("api/mobile/auth/google")
    Call<AuthResponseDto> googleAuth(@Body GoogleAuthRequestDto request);

    @GET("api/mobile/profile/{userId}")
    Call<UserProfileDto> getProfile(@Path("userId") long userId);

    @PUT("api/mobile/profile/{userId}/password")
    Call<GenericApiResponseDto> changePassword(
            @Path("userId") long userId,
            @Body java.util.Map<String, String> request
    );

    @PUT("api/mobile/profile/{userId}/notifications")
    Call<GenericApiResponseDto> updateNotificationPreference(
            @Path("userId") long userId,
            @Body NotificationPreferenceRequestDto request
    );

    @POST("api/mobile/auth/forgot-password")
    Call<AuthResponseDto> forgotPassword(@Body java.util.Map<String, String> request);

    @PUT("api/mobile/profile/{userId}/username")
    Call<GenericApiResponseDto> updateUsername(
            @Path("userId") long userId,
            @Body UsernameUpdateRequestDto request
    );

    @POST("api/mobile/progress/update")
    Call<GenericApiResponseDto> updateProgress(@Body ProgressUpdateRequestDto request);

    @POST("api/mobile/progress/complete")
    Call<GenericApiResponseDto> completeSentence(@Body ProgressUpdateRequestDto request);

    @POST("api/mobile/progress/skip")
    Call<GenericApiResponseDto> skipSentence(@Body ProgressUpdateRequestDto request);

    @GET("api/mobile/progress/in-progress")
    Call<List<InProgressLessonDto>> getInProgressLessons(@Query("userId") long userId);

    @POST("api/mobile/tracking/time")
    Call<GenericApiResponseDto> trackTime(@Body TimeTrackingRequestDto request);

    @GET("api/mobile/sentences/{sentenceId}/comments")
    Call<List<MobileBootstrapCommentDto>> getSentenceComments(@Path("sentenceId") long sentenceId);

    @GET("api/mobile/profile/{userId}/comments")
    Call<List<MobileBootstrapCommentDto>> getUserComments(@Path("userId") long userId);

    @GET("api/mobile/comments/{commentId}/replies")
    Call<List<MobileBootstrapCommentDto>> getCommentReplies(@Path("commentId") long commentId);

    @POST("api/mobile/comments")
    Call<MobileBootstrapCommentDto> addComment(@Body CreateCommentRequestDto request);

    @POST("api/mobile/comments/{commentId}/vote")
    Call<GenericApiResponseDto> voteComment(
            @Path("commentId") long commentId,
            @Body VoteCommentRequestDto request
    );

    @DELETE("api/mobile/comments/{commentId}")
    Call<GenericApiResponseDto> deleteComment(
            @Path("commentId") long commentId,
            @Query("userId") long userId
    );

    @POST("api/mobile/dictation/check")
    Call<DictationResultDto> checkDictation(@Body CheckDictationRequestDto request);

    @POST("api/mobile/dictation/skip")
    Call<DictationResultDto> skipDictation(@Body ProgressUpdateRequestDto request);

    @Multipart
    @POST("api/mobile/speaking/evaluate")
    Call<SpeakingResultDto> evaluateSpeaking(
            @Part MultipartBody.Part audio,
            @Part("referenceText") RequestBody referenceText,
            @Part("sentenceId") RequestBody sentenceId
    );

    @GET("api/mobile/speaking/results")
    Call<SpeakingResultDto> getSpeakingResults(
            @Query("sentenceId") long sentenceId
    );

    @GET("api/mobile/leaderboard")
    Call<MobileLeaderboardDto> getLeaderboard();
}
