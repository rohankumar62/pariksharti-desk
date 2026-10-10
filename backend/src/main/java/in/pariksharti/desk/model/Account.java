package in.pariksharti.desk.model;

import in.pariksharti.desk.dto.ProfileResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id @Column(length = 36) String id;
    @Column(nullable = false, unique = true, length = 254) String email;
    @Column(name = "password_hash", nullable = false, length = 100) String passwordHash;
    @Column(name = "full_name", nullable = false, length = 100) String fullName;
    @Column(nullable = false, length = 80) String city;
    @Column(nullable = false, length = 500) String bio;
    @Column(name = "target_year") Integer targetYear;
    @Column(name = "created_at", nullable = false) Instant createdAt;

    protected Account() {}

    public Account(String email, String passwordHash, String fullName) {
        id = UUID.randomUUID().toString();
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        city = "";
        bio = "";
        createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }

    public ProfileResponse toProfileResponse() {
        return new ProfileResponse(id, email, fullName, city, bio, targetYear, createdAt);
    }

    public void updateProfile(String fullName, String city, String bio, Integer targetYear) {
        this.fullName = fullName;
        this.city = city;
        this.bio = bio;
        this.targetYear = targetYear;
    }
}