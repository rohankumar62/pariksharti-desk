package in.pariksharti.desk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 80) String city,
        @Size(max = 500) String bio,
        Integer targetYear,
        @Pattern(regexp = "en|hi", message = "Medium must be en or hi") String medium) {
    public ProfileUpdateRequest(String fullName, String city, String bio, Integer targetYear) {
        this(fullName, city, bio, targetYear, null);
    }
}