package hcmute.edu.vn.nguyenthetan.domain.usecase.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AuthResultTest {

    @Test
    public void networkError_factory_setsStatusAndNullData() {
        AuthResult result = AuthResult.networkError();
        assertEquals(AuthResult.Status.NETWORK_ERROR, result.status);
        assertNull(result.userId);
        assertFalse(result.isSuccess());
    }

    @Test
    public void httpError_factory_carriesMessage() {
        AuthResult result = AuthResult.httpError("invalid credentials");
        assertEquals(AuthResult.Status.HTTP_ERROR, result.status);
        assertEquals("invalid credentials", result.message);
        assertFalse(result.isSuccess());
    }

    @Test
    public void accountLocked_factory_carriesMessage() {
        AuthResult result = AuthResult.accountLocked("locked until tomorrow");
        assertEquals(AuthResult.Status.ACCOUNT_LOCKED, result.status);
        assertEquals("locked until tomorrow", result.message);
        assertFalse(result.isSuccess());
    }

    @Test
    public void success_isSuccessOnly_whenUserIdPresent() {
        AuthResult result = AuthResult.success(123L, "user", "user@test.com", "token-abc", "ok");
        assertTrue(result.isSuccess());
        assertEquals(Long.valueOf(123L), result.userId);
        assertEquals("user", result.username);
        assertEquals("user@test.com", result.email);
        assertEquals("token-abc", result.token);
        assertEquals("ok", result.message);
    }

    @Test
    public void success_withNullUserId_isNotSuccess() {
        AuthResult result = AuthResult.success(null, "user", "user@test.com", "token-abc", "ok");
        assertEquals(AuthResult.Status.SUCCESS, result.status);
        assertFalse(result.isSuccess());
    }
}
