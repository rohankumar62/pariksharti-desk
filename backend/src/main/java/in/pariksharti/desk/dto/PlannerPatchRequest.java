package in.pariksharti.desk.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;

public record PlannerPatchRequest(@NotBlank String field, JsonNode patch) {}