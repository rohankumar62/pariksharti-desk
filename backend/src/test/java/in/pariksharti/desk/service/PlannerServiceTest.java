package in.pariksharti.desk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.pariksharti.desk.exception.ApiException;
import in.pariksharti.desk.model.PlannerRecord;
import in.pariksharti.desk.repository.PlannerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlannerServiceTest {
    private final PlannerRepository repository = mock(PlannerRepository.class);
    private final ObjectMapper json = new ObjectMapper();
    private PlannerService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new PlannerService(repository, json);
    }

    @Test
    void initialJsonContainsDefaultPlannerSectionsAndSettings() throws Exception {
        JsonNode initial = json.readTree(service.initialJson());

        assertTrue(initial.get("progress").isObject());
        assertTrue(initial.get("schedule").isObject());
        assertTrue(initial.get("sessions").isObject());
        assertEquals(12, initial.at("/settings/months").intValue());
        assertEquals(4, initial.at("/settings/hours").intValue());
        assertEquals("Undecided", initial.at("/settings/optional").asText());
        assertEquals("en", initial.at("/settings/language").asText());
    }

    @Test
    void readReturnsNotFoundWhenPlannerDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> service.read("missing"));

        assertEquals(404, error.getStatus());
        assertEquals("PLANNER_MISSING", error.getCode());
    }

    @Test
    void patchMergesSettingsDraftsAndRemovesNullProgressValues() throws Exception {
        PlannerRecord row = new PlannerRecord("user", service.initialJson());
        when(repository.findForUpdate("user")).thenReturn(Optional.of(row));
        JsonNode patch = json.readTree("""
                {"language":"hi","practiceDrafts":{"constitution":"संविधान"}}
                """);

        service.patch("user", "settings", patch);
        service.patch("user", "progress", json.readTree("{\"polity-1\":\"complete\"}"));
        service.patch("user", "progress", json.readTree("{\"polity-1\":null}"));

        JsonNode saved = json.readTree(row.getStateJson());
        assertEquals("hi", saved.at("/settings/language").asText());
        assertEquals(4, saved.at("/settings/hours").intValue());
        assertEquals("संविधान", saved.at("/settings/practiceDrafts/constitution").asText());
        assertEquals(12, saved.at("/settings/months").intValue());
        assertFalse(saved.at("/progress").has("polity-1"));
    }

    @Test
    void patchRejectsUnknownTopicsAndInvalidCalendarDates() throws Exception {
        assertThrows(ApiException.class, () -> service.patch("user", "progress",
                json.readTree("{\"unknown-topic\":\"complete\"}")));
        assertThrows(ApiException.class, () -> service.patch("user", "schedule",
                json.readTree("{\"polity-1\":\"2027-02-29\"}")));

        verifyNoInteractions(repository);
    }

    @Test
    void patchRejectsUnsupportedFieldBeforeLoadingPlanner() throws Exception {
        ApiException error = assertThrows(ApiException.class,
                () -> service.patch("user", "userId", json.createObjectNode()));

        assertEquals(400, error.getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void patchReturnsNotFoundWhenLockedPlannerIsMissing() throws Exception {
        when(repository.findForUpdate("missing")).thenReturn(Optional.empty());

        ApiException error = assertThrows(ApiException.class, () -> service.patch("missing", "progress",
                json.readTree("{\"polity-1\":\"complete\"}")));

        assertEquals("PLANNER_MISSING", error.getCode());
    }
}