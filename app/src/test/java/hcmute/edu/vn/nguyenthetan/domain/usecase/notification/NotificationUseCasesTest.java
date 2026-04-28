package hcmute.edu.vn.nguyenthetan.domain.usecase.notification;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.GenericApiResponseDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileNotificationFeedDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileNotificationItemDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileNotificationSummaryDto;
import hcmute.edu.vn.nguyenthetan.domain.model.notification.NotificationFeed;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Unit tests for notification use cases:
 * {@link GetNotificationsUseCase}, {@link GetNotificationSummaryUseCase},
 * {@link MarkNotificationReadUseCase}, {@link MarkAllNotificationsReadUseCase}.
 */
public class NotificationUseCasesTest {

    private MobileApiService api;

    @Before
    public void setUp() {
        api = mock(MobileApiService.class);
    }

    // ────── GetNotificationsUseCase ──────

    @Test
    public void getNotifications_success_mapsDtoToDomain() throws Exception {
        GetNotificationsUseCase useCase = new GetNotificationsUseCase(api);

        MobileNotificationItemDto item = new MobileNotificationItemDto();
        item.id = 1L;
        item.type = "COMMENT_REPLY";
        item.title = "New reply";
        item.body = "Someone replied";
        item.read = false;

        MobileNotificationFeedDto feedDto = new MobileNotificationFeedDto();
        feedDto.unreadCount = 3;
        feedDto.items = Collections.singletonList(item);

        Call<MobileNotificationFeedDto> call = mockCall(Response.success(feedDto));
        when(api.getNotifications(anyInt())).thenReturn(call);

        NotificationFeed result = useCase.execute(20);
        assertNotNull(result);
        assertEquals(3, result.unreadCount);
        assertEquals(1, result.items.size());
        assertEquals("New reply", result.items.get(0).title);
    }

    @Test
    public void getNotifications_emptyItems() throws Exception {
        GetNotificationsUseCase useCase = new GetNotificationsUseCase(api);

        MobileNotificationFeedDto feedDto = new MobileNotificationFeedDto();
        feedDto.unreadCount = 0;
        feedDto.items = null;

        Call<MobileNotificationFeedDto> call = mockCall(Response.success(feedDto));
        when(api.getNotifications(anyInt())).thenReturn(call);

        NotificationFeed result = useCase.execute(20);
        assertNotNull(result);
        assertEquals(0, result.items.size());
    }

    @Test
    public void getNotifications_networkError_returnsNull() throws Exception {
        GetNotificationsUseCase useCase = new GetNotificationsUseCase(api);
        Call<MobileNotificationFeedDto> call = mockCallThrows(new IOException("timeout"));
        when(api.getNotifications(anyInt())).thenReturn(call);

        assertNull(useCase.execute(20));
    }

    @Test
    public void getNotifications_httpError_returnsNull() throws Exception {
        GetNotificationsUseCase useCase = new GetNotificationsUseCase(api);
        Call<MobileNotificationFeedDto> call = mockCall(
                Response.error(500, ResponseBody.create(null, "")));
        when(api.getNotifications(anyInt())).thenReturn(call);

        assertNull(useCase.execute(20));
    }

    // ────── GetNotificationSummaryUseCase ──────

    @Test
    public void getSummary_success_returnsUnreadCount() throws Exception {
        GetNotificationSummaryUseCase useCase = new GetNotificationSummaryUseCase(api);

        MobileNotificationSummaryDto dto = new MobileNotificationSummaryDto();
        dto.unreadCount = 7;

        Call<MobileNotificationSummaryDto> call = mockCall(Response.success(dto));
        when(api.getNotificationSummary()).thenReturn(call);

        assertEquals(7L, useCase.execute());
    }

    @Test
    public void getSummary_networkError_returnsZero() throws Exception {
        GetNotificationSummaryUseCase useCase = new GetNotificationSummaryUseCase(api);
        Call<MobileNotificationSummaryDto> call = mockCallThrows(new IOException("fail"));
        when(api.getNotificationSummary()).thenReturn(call);

        assertEquals(0L, useCase.execute());
    }

    // ────── MarkNotificationReadUseCase ──────

    @Test
    public void markRead_success() throws Exception {
        MarkNotificationReadUseCase useCase = new MarkNotificationReadUseCase(api);
        Call<GenericApiResponseDto> call = mockCall(Response.success(new GenericApiResponseDto()));
        when(api.markNotificationRead(eq(42L))).thenReturn(call);

        assertTrue(useCase.execute(42L));
    }

    @Test
    public void markRead_networkError() throws Exception {
        MarkNotificationReadUseCase useCase = new MarkNotificationReadUseCase(api);
        Call<GenericApiResponseDto> call = mockCallThrows(new IOException("fail"));
        when(api.markNotificationRead(anyLong())).thenReturn(call);

        assertFalse(useCase.execute(42L));
    }

    // ────── MarkAllNotificationsReadUseCase ──────

    @Test
    public void markAllRead_success() throws Exception {
        MarkAllNotificationsReadUseCase useCase = new MarkAllNotificationsReadUseCase(api);
        Call<GenericApiResponseDto> call = mockCall(Response.success(new GenericApiResponseDto()));
        when(api.markAllNotificationsRead()).thenReturn(call);

        assertTrue(useCase.execute());
    }

    @Test
    public void markAllRead_networkError() throws Exception {
        MarkAllNotificationsReadUseCase useCase = new MarkAllNotificationsReadUseCase(api);
        Call<GenericApiResponseDto> call = mockCallThrows(new IOException("fail"));
        when(api.markAllNotificationsRead()).thenReturn(call);

        assertFalse(useCase.execute());
    }

    // ────── Helpers ──────

    @SuppressWarnings("unchecked")
    private <T> Call<T> mockCall(Response<T> response) throws Exception {
        Call<T> call = mock(Call.class);
        when(call.execute()).thenReturn(response);
        return call;
    }

    @SuppressWarnings("unchecked")
    private <T> Call<T> mockCallThrows(Throwable t) throws Exception {
        Call<T> call = mock(Call.class);
        when(call.execute()).thenThrow(t);
        return call;
    }
}
