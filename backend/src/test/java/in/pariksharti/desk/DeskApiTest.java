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
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired AccountRepository accounts;
    private static final String PASSWORD = "a-long-test-password!";
    String email() { return UUID.randomUUID() + "@example.test"; }
    String body(Object data) throws Exception { return json.writeValueAsString(data); }
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
    @Test void registrationStoresHashAndSeparatesEveryUsersData() throws Exception {
        String alice = email(), bob = email(); register(alice); register(bob);
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
    @Test void profileUpdatePersistsAcrossNewLoginAndIsPrivate() throws Exception {
        String email = email(); register(email); MockHttpSession session = login(email);
        int year = java.time.Year.now().getValue() + 1;
        http.perform(put("/api/profile").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("fullName", "परीक्षार्थी", "city", "Delhi", "bio", "Daily preparation", "targetYear", year))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("परीक्षार्थी"));
        http.perform(get("/api/auth/me").session(login(email)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.city").value("Delhi"))
            .andExpect(jsonPath("$.targetYear").value(year)).andExpect(jsonPath("$.passwordHash").doesNotExist());
        http.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
    @Test void requiresLoginAndCsrfAndInvalidatesLogout() throws Exception {
        http.perform(get("/api/planner")).andExpect(status().isUnauthorized());
        http.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        String email = email(); register(email); MockHttpSession session = login(email);
        http.perform(patch("/api/planner").session(session).contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("field", "progress", "patch", Map.of("polity-1", "complete")))))
            .andExpect(status().isForbidden());
        http.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        http.perform(get("/api/planner")).andExpect(status().isUnauthorized());
    }
    @Test void browserCsrfFlowRotatesSessionAndTokenOnLogin() throws Exception {
        String email = email(); register(email);
        MvcResult initial = http.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) initial.getRequest().getSession();
        String oldSessionId = session.getId();
        JsonNode token = json.readTree(initial.getResponse().getContentAsString());
        http.perform(post("/api/auth/login").session(session).header(token.get("headerName").asText(), token.get("token").asText())
            .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("email", email, "password", PASSWORD))))
            .andExpect(status().isOk());
        assertThat(session.getId()).isNotEqualTo(oldSessionId);
        http.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
        http.perform(patch("/api/planner").session(session).header(token.get("headerName").asText(), token.get("token").asText())
            .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("field", "progress", "patch", Map.of("polity-1", "complete")))))
            .andExpect(status().isForbidden());
        JsonNode fresh = json.readTree(http.perform(get("/api/auth/csrf").session(session)).andReturn().getResponse().getContentAsString());
        http.perform(patch("/api/planner").session(session).header(fresh.get("headerName").asText(), fresh.get("token").asText())
            .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("field", "progress", "patch", Map.of("polity-1", "complete")))))
            .andExpect(status().isOk());
    }
    @Test void rejectsInvalidPlannerFieldsWithoutChangingSavedState() throws Exception {
        String email = email(); register(email); MockHttpSession session = login(email);
        updatePlanner(session, "progress", Map.of("someone-elses-topic", "complete")).andExpect(status().isBadRequest());
        updatePlanner(session, "progress", Map.of("polity-1", "complete", "polity-2", "bad-status")).andExpect(status().isBadRequest());
        updatePlanner(session, "settings", Map.of("hours", 0)).andExpect(status().isBadRequest());
        updatePlanner(session, "settings", Map.of("hours", 2.5)).andExpect(status().isBadRequest());
        updatePlanner(session, "settings", Map.of("optional", "Made-up subject")).andExpect(status().isBadRequest());
        updatePlanner(session, "settings", Map.of("startDate", "2027-02-29")).andExpect(status().isBadRequest());
        updatePlanner(session, "schedule", Map.of("polity-1", "2027-02-29")).andExpect(status().isBadRequest());
        updatePlanner(session, "sessions", Map.of("2027-02-30-reading", true)).andExpect(status().isBadRequest());
        updatePlanner(session, "settings", Map.of("practiceDrafts", Map.of("ethics", "x".repeat(5001)))).andExpect(status().isBadRequest());
        updatePlanner(session, "settings", Map.of("practiceDrafts", Map.of("unknown", "text"))).andExpect(status().isBadRequest());
        updatePlanner(session, "userId", Map.of()).andExpect(status().isBadRequest());
        http.perform(get("/api/planner").session(session)).andExpect(jsonPath("$.progress").isEmpty())
            .andExpect(jsonPath("$.settings.hours").value(4)).andExpect(jsonPath("$.settings.practiceDrafts").isEmpty());
    }
    @Test void duplicateEmailIsCaseInsensitiveAndLoginUsesNormalizedEmail() throws Exception {
        String email = email(); register(email);
        http.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email", email.toUpperCase(Locale.ROOT), "password", PASSWORD, "fullName", "Duplicate"))))
            .andExpect(status().isConflict());
        login(email.toUpperCase(Locale.ROOT));
    }
    @Test void wrongPasswordDoesNotCreateSessionAndHasAttemptLimit() throws Exception {
        String email = email(); register(email);
        for (int n = 0; n < 8; n++) {
            http.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", n == 0 ? "अ".repeat(50) : "incorrect-password"))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        http.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content(body(Map.of("email", email, "password", PASSWORD))))
            .andExpect(status().isTooManyRequests());
    }
}
