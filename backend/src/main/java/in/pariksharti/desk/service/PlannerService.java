package in.pariksharti.desk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import in.pariksharti.desk.exception.ApiException;
import in.pariksharti.desk.model.PlannerRecord;
import in.pariksharti.desk.repository.PlannerRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlannerService {
    private final PlannerRepository repository;
    private final ObjectMapper json;
    private final Set<String> topicIds = new HashSet<>(), optionalNames = new HashSet<>();
    private static final Set<String> PROMPTS = Set.of("constitution", "inflation", "urbanisation", "wetlands", "ethics", "monsoon");

    public PlannerService(PlannerRepository repository, ObjectMapper json) throws IOException {
        this.repository = repository;
        this.json = json;
        try (var stream = new ClassPathResource("catalog.json").getInputStream()) {
            JsonNode catalog = json.readTree(stream);
            catalog.get("topicIds").forEach(v -> topicIds.add(v.asText()));
            catalog.get("optionals").forEach(v -> optionalNames.add(v.asText()));
        }
    }

    public String initialJson() {
        ObjectNode root = json.createObjectNode();
        root.putObject("progress"); root.putObject("schedule"); root.putObject("sessions");
        ObjectNode settings = root.putObject("settings");
        settings.put("startDate", LocalDate.now(ZoneId.of("Asia/Kolkata")).toString());
        settings.put("months", 12); settings.put("hours", 4); settings.put("optional", "Undecided");
        settings.put("language", "en"); settings.putObject("practiceDrafts");
        return root.toString();
    }

    @Transactional(readOnly = true)
    public JsonNode read(String userId) { return parse(record(userId).getStateJson()); }

    @Transactional
    public void setMedium(String userId, String medium) {
        patch(userId, "settings", json.createObjectNode().put("language", medium));
    }

    private PlannerRecord record(String id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(404, "PLANNER_MISSING", "Your planner was not found."));
    }

    @Transactional
    public void patch(String userId, String field, JsonNode patch) {
        if (!Set.of("progress", "schedule", "sessions", "settings").contains(field == null ? "" : field)
                || patch == null || !patch.isObject() || patch.size() > 200 || patch.toString().length() > 40000)
            throw ApiException.invalid("Invalid planner update.");
        validate(field, patch);
        PlannerRecord row = repository.findForUpdate(userId)
                .orElseThrow(() -> new ApiException(404, "PLANNER_MISSING", "Your planner was not found."));
        ObjectNode state = (ObjectNode) parse(row.getStateJson());
        ObjectNode destination = (ObjectNode) state.get(field);
        patch.properties().forEach(e -> {
            if (e.getValue().isNull()) destination.remove(e.getKey());
            else if (field.equals("settings") && e.getKey().equals("practiceDrafts")) {
                ObjectNode drafts = (ObjectNode) destination.get("practiceDrafts");
                e.getValue().properties().forEach(d -> drafts.set(d.getKey(), d.getValue()));
            } else destination.set(e.getKey(), e.getValue());
        });
        if (state.get("sessions").size() > 10000) throw ApiException.invalid("The daily checklist history is full.");
        row.setStateJson(state.toString());
    }

    private JsonNode parse(String value) {
        try { return json.readTree(value); }
        catch (IOException e) { throw new IllegalStateException("Stored planner is unreadable.", e); }
    }

    private void validate(String field, JsonNode patch) {
        patch.properties().forEach(e -> {
            String key = e.getKey(); JsonNode value = e.getValue(); boolean valid;
            switch (field) {
                case "progress" -> valid = topicIds.contains(key) && (value.isNull() || value.isTextual()
                        && Set.of("not-started", "in-progress", "complete").contains(value.asText()));
                case "schedule" -> valid = topicIds.contains(key) && (value.isNull() || date(value));
                case "sessions" -> valid = key.matches("\\d{4}-\\d{2}-\\d{2}-(reading|revision|practice)")
                        && date(new TextNode(key.substring(0, Math.min(10, key.length()))))
                        && (value.isNull() || value.isBoolean());
                case "settings" -> valid = switch (key) {
                    case "startDate" -> date(value);
                    case "months" -> integer(value, 3, 24);
                    case "hours" -> integer(value, 1, 12);
                    case "optional" -> value.isTextual() && optionalNames.contains(value.asText());
                    case "language" -> value.isTextual() && Set.of("en", "hi").contains(value.asText());
                    case "practiceDrafts" -> validDrafts(value);
                    default -> false;
                };
                default -> valid = false;
            }
            if (!valid) throw ApiException.invalid("Invalid value for " + field + "." + key + ".");
        });
    }

    private boolean validDrafts(JsonNode value) {
        if (!value.isObject() || value.size() > PROMPTS.size()) return false;
        for (var e : value.properties()) {
            if (!PROMPTS.contains(e.getKey()) || !e.getValue().isTextual() || e.getValue().asText().length() > 5000) return false;
        }
        return true;
    }

    private static boolean integer(JsonNode value, int min, int max) {
        return value.isIntegralNumber() && value.canConvertToInt() && value.intValue() >= min && value.intValue() <= max;
    }

    private static boolean date(JsonNode value) {
        if (!value.isTextual() || !value.asText().matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try { LocalDate date = LocalDate.parse(value.asText()); return date.getYear() >= 2000 && date.getYear() <= 2100; }
        catch (DateTimeParseException e) { return false; }
    }
}