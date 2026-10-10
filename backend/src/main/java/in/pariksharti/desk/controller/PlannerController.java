package in.pariksharti.desk.controller;

import com.fasterxml.jackson.databind.JsonNode;
import in.pariksharti.desk.dto.PlannerPatchRequest;
import in.pariksharti.desk.service.PlannerService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planner")
public class PlannerController {
    private final PlannerService planners;

    public PlannerController(PlannerService planners) { this.planners = planners; }

    @GetMapping
    public JsonNode read(Authentication auth) { return planners.read(auth.getName()); }

    @PatchMapping
    public Map<String, Boolean> patch(@Valid @RequestBody PlannerPatchRequest body, Authentication auth) {
        planners.patch(auth.getName(), body.field(), body.patch());
        return Map.of("saved", true);
    }
}