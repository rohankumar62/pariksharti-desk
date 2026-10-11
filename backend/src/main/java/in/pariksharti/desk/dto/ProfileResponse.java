package in.pariksharti.desk.dto;

import java.time.Instant;

public record ProfileResponse(String id, String email, String fullName, String city,
                              String bio, Integer targetYear, Instant createdAt, String medium) {
    public ProfileResponse(String id, String email, String fullName, String city,
                           String bio, Integer targetYear, Instant createdAt) {
        this(id, email, fullName, city, bio, targetYear, createdAt, "en");
    }

    public ProfileResponse withMedium(String medium) {
        return new ProfileResponse(id, email, fullName, city, bio, targetYear, createdAt, medium);
    }
}