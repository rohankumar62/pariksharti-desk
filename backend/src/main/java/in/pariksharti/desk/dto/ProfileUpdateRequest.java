package in.pariksharti.desk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 80) String city,
        @Size(max = 500) String bio,
        Integer targetYear) {}