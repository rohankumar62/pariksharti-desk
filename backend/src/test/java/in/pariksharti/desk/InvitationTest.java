package in.pariksharti.desk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:invitation-tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "app.signup-code=private-test-invitation-code", "app.require-invite=true"
})
@AutoConfigureMockMvc
class InvitationTest {
    @Autowired MockMvc http;
    @Test void requiresInvitationAndNeverReturnsTheSecret() throws Exception {
        http.perform(get("/api/auth/config")).andExpect(status().isOk())
            .andExpect(jsonPath("$.inviteRequired").value(true)).andExpect(jsonPath("$.signupCode").doesNotExist());
        String email = UUID.randomUUID() + "@example.test";
        String base = "{\"email\":\"" + email + "\",\"fullName\":\"Learner\",\"password\":\"a-long-test-password\",\"inviteCode\":\"";
        http.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(base + "wrong\"}"))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("INVITE_REQUIRED"));
        http.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(base + "private-test-invitation-code\"}"))
            .andExpect(status().isCreated());
    }
}
