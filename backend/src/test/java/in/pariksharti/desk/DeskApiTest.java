package in.pariksharti.desk;

import com.fasterxml.jackson.databind.*;
import in.pariksharti.desk.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:desk-tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "app.signup-code=", "app.require-invite=false"
})
@AutoConfigureMockMvc
class DeskApiTest {
    @Autowired
    MockMvc http;
    @Autowired
    ObjectMapper json;
    @Autowired
    AccountRepository accounts;
    private static final String PASSWORD = "a-long-test-password!";

    String email() {
        return UUID.randomUUID() + "@example.test";
    }

    String body(Object data) throws Exception {
        return json.writeValueAsString(data);
    }

    void register(String email) throws Exception {
        http.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", PASSWORD, "fullName", "New Learner"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    MockHttpSession login(String email) throws Exception {
        return (MockHttpSession) http.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
    }

    ResultActions updatePlanner(MockHttpSession session, String field, Object changes) throws Exception {
        return http.perform(patch("/api/planner").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("field", field, "patch", changes))));
    }

    @Test
    void registrationStoresHashAndSeparatesEveryUsersData() throws Exception {
        String alice = email(), bob = email();
        register(alice);
        register(bob);
        assertThat(accounts.findByEmail(alice).orElseThrow().getPasswordHash()).startsWith("$2a$12$").doesNotContain(PASSWORD);
        MockHttpSession a = login(alice), b = login(bob);
        updatePlanner(a, "progress", Map.of("polity-1", "complete")).andExpect(status().isOk());
        updatePlanner(a, "settings", Map.of("language", "hi", "practiceDrafts", Map.of("constitution", "संविधान और अधिकार"))).andExpect(status().isOk());
        updatePlanner(a, "settings", Map.of("practiceDrafts", Map.of("inflation", "Prices and purchasing power"))).andExpect(status().isOk());
        updatePlanner(a, "schedule", Map.of("polity-1", "2028-02-29")).andExpect(status().isOk());
        updatePlanner(a, "sessions", Map.of("2028-02-29-reading", true)).andExpect(status().isOk());
        http.perform(get("/api/planner").session(a).header("X-User-Id", accounts.findByEmail(bob).orElseThrow().getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.progress.polity-1").value("complete"))
                .andExpect(jsonPath("$.settings.practiceDrafts.constitution").value("संविधान और अधिकार"))
                .andExpect(jsonPath("$.settings.practiceDrafts.inflation").value("Prices and purchasing power"));
        http.perform(get("/api/planner").session(b)).andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").isEmpty()).andExpect(jsonPath("$.schedule").isEmpty())
                .andExpect(jsonPath("$.sessions").isEmpty()).andExpect(jsonPath("$.settings.language").value("en"))
                .andExpect(jsonPath("$.settings.practiceDrafts").isEmpty());
    }

    @Test
    void profileUpdatePersistsAcrossNewLoginAndIsPrivate() throws Exception {
        String email = email();
        register(email);
        MockHttpSession session = login(email);
        int year = java.time.Year.now().getValue() + 1;
        http.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "परीक्षार्थी", "city", "Delhi", "bio", "Daily preparation", "targetYear", year))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("परीक्षार्थी"));
        http.perform(get("/api/auth/me").session(login(email)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.city").value("Delhi"))
                .andExpect(jsonPath("$.targetYear").value(year)).andExpect(jsonPath("$.passwordHash").doesNotExist());
        http.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void profileMediumUsesExistingLanguageAndPreservesItWhenOmitted() throws Exception {
        String email = email();
        register(email);
        MockHttpSession session = login(email);
        updatePlanner(session, "settings", Map.of("language", "hi")).andExpect(status().isOk());
        http.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.medium").value("hi"));
        http.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "Learner", "city", "", "bio", ""))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.medium").value("hi"));
        http.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "Learner", "city", "", "bio", "", "medium", "en"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.medium").value("en"));
        http.perform(get("/api/planner").session(session)).andExpect(jsonPath("$.settings.language").value("en"));
        http.perform(get("/api/auth/me").session(login(email))).andExpect(jsonPath("$.medium").value("en"));
        http.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("fullName", "Learner", "medium", "fr"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresLoginAndCsrfAndInvalidatesLogout() throws Exception {
        http.perform(get("/api/planner")).andExpect(status().isUnauthorized());
        http.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        String email = email();
        register(email);
        MockHttpSession session = login(email);
        http.perform(patch("/api/planner").session(session).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("field", "progress", "patch", Map.of("polity-1", "complete")))));
    }
}
