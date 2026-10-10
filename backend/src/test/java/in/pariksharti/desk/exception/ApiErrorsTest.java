package in.pariksharti.desk.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

class ApiErrorsTest {
    private final ApiErrors errors = new ApiErrors();

    @Test
    void appErrorUsesExceptionStatusAndPublicErrorBody() {
        var response = errors.appError(new ApiException(409, "EMAIL_EXISTS", "Already registered"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(Map.of("code", "EMAIL_EXISTS", "error", "Already registered"), response.getBody());
    }

    @Test
    void unreadableRequestMapsToGenericBadInputResponse() {
        var unreadable = new HttpMessageNotReadableException("malformed",
                new MockHttpInputMessage(new byte[0]));

        var response = errors.badInput(unreadable);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("code", "INVALID_INPUT", "error", "Check the submitted fields and try again."), response.getBody());
    }
}