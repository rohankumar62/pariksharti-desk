package in.pariksharti.desk.dto;

import java.time.Instant;

public record ProfileResponse(String id, String email, String fullName, String city,
        String bio, Integer targetYear, Instant createdAt) {}