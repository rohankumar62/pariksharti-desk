package in.pariksharti.desk.service;

import in.pariksharti.desk.dto.ProfileResponse;
import in.pariksharti.desk.dto.ProfileUpdateRequest;
import in.pariksharti.desk.dto.RegisterRequest;
import in.pariksharti.desk.exception.ApiException;
import in.pariksharti.desk.model.Account;
import in.pariksharti.desk.model.PlannerRecord;
import in.pariksharti.desk.repository.AccountRepository;
import in.pariksharti.desk.repository.PlannerRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Year;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final PlannerRepository planners;
    private final PlannerService state;
    private final PasswordEncoder encoder;
    private final boolean registrationEnabled;
    private final String signupCode;

    public AccountService(AccountRepository accounts, PlannerRepository planners, PlannerService state,
            PasswordEncoder encoder,
            @Value("${app.registration-enabled}") boolean enabled,
            @Value("${app.signup-code}") String code,
            @Value("${app.require-invite}") boolean requireInvite) {
        this.accounts = accounts;
        this.planners = planners;
        this.state = state;
        this.encoder = encoder;
        registrationEnabled = enabled;
        signupCode = code;
        if (enabled && (code.length() > 200 || requireInvite && code.length() < 16))
            throw new IllegalStateException("Set APP_SIGNUP_CODE to 16–200 characters before enabling production registration.");
    }

    public boolean registrationEnabled() { return registrationEnabled; }
    public boolean inviteRequired() { return !signupCode.isEmpty(); }

    @Transactional
    public ProfileResponse register(RegisterRequest input) {
        if (!registrationEnabled) throw new ApiException(403, "REGISTRATION_CLOSED", "New registrations are currently closed.");
        if (!signupCode.isEmpty() && !MessageDigest.isEqual(signupCode.getBytes(StandardCharsets.UTF_8),
                (input.inviteCode() == null ? "" : input.inviteCode()).getBytes(StandardCharsets.UTF_8)))
            throw new ApiException(403, "INVITE_REQUIRED", "Enter a valid invitation code.");
        if (input.password().length() < 10 || input.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw ApiException.invalid("Use a password with at least 10 characters and at most 72 UTF-8 bytes.");
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (accounts.existsByEmail(email)) throw new ApiException(409, "EMAIL_EXISTS", "An account with this email already exists. Sign in instead.");
        Account account = new Account(email, encoder.encode(input.password()), clean(input.fullName(), 100, false));
        try { accounts.saveAndFlush(account); }
        catch (DataIntegrityViolationException e) { throw new ApiException(409, "EMAIL_EXISTS", "An account with this email already exists."); }
        planners.save(new PlannerRecord(account.getId(), state.initialJson()));
        return account.toProfileResponse();
    }

    public ProfileResponse profile(String id) { return account(id).toProfileResponse(); }

    private Account account(String id) {
        return accounts.findById(id).orElseThrow(() -> new ApiException(401, "AUTH_REQUIRED", "Please sign in again."));
    }

    @Transactional
    public ProfileResponse update(String id, ProfileUpdateRequest input) {
        Account account = account(id);
        int year = Year.now().getValue();
        if (input.targetYear() != null && (input.targetYear() < year || input.targetYear() > year + 10))
            throw ApiException.invalid("Choose a target year within the next 10 years.");
        account.updateProfile(clean(input.fullName(), 100, false), clean(input.city(), 80, true),
                clean(input.bio(), 500, true), input.targetYear());
        return account.toProfileResponse();
    }

    private static String clean(String value, int max, boolean emptyAllowed) {
        String result = value == null ? "" : value.trim();
        if ((!emptyAllowed && result.isEmpty()) || result.length() > max || result.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n'))
            throw ApiException.invalid("Check your profile fields and try again.");
        return result;
    }
}