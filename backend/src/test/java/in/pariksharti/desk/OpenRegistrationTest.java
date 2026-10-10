package in.pariksharti.desk;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:open-registration-tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "app.signup-code=stale-render-invite-code", "app.require-invite=false"
})
@AutoConfigureMockMvc
class OpenRegistrationTest {
    @Autowired MockMvc http;

    @Test void staleSignupCodeDoesNotBlockSelfRegistration() throws Exception {
        http.perform(get("/api/auth/config")).andExpect(status().isOk())
            .andExpect(jsonPath("$.inviteRequired").value(false));

        String email = UUID.randomUUID() + "@example.test";
        String body = "{\"email\":\"" + email + "\",\"fullName\":\"Learner\",\"password\":\"a-long-test-password\"}";
        http.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
    }
}
