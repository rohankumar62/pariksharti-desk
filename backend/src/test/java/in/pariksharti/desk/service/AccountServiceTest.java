package in.pariksharti.desk.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import in.pariksharti.desk.dto.ProfileUpdateRequest;
import in.pariksharti.desk.dto.RegisterRequest;
import in.pariksharti.desk.exception.ApiException;
import in.pariksharti.desk.model.Account;
import in.pariksharti.desk.model.PlannerRecord;
import in.pariksharti.desk.repository.AccountRepository;
import in.pariksharti.desk.repository.PlannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("null")
class AccountServiceTest {
    private final AccountRepository accounts = mock(AccountRepository.class);
    private final PlannerRepository planners = mock(PlannerRepository.class);
    private final PlannerService state = mock(PlannerService.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accounts, planners, state, encoder, true, "", false);
        when(state.initialJson()).thenReturn("{}");
        var saved = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        saved.putObject("settings").put("language", "en");
        when(state.read(any())).thenReturn(saved);
        when(encoder.encode(any())).thenReturn("encoded-password");
    }

    @Test
    void registrationNormalizesEmailAndCreatesPlanner() {
        when(accounts.existsByEmail("learner@example.test")).thenReturn(false);
        when(accounts.saveAndFlush(any(Account.class))).thenAnswer(call -> call.getArgument(0));
        RegisterRequest input = new RegisterRequest("Learner", " LEARNER@EXAMPLE.TEST ", "long-enough-password", null);

        var profile = service.register(input);

        assertEquals("learner@example.test", profile.email());
        assertEquals("Learner", profile.fullName());
        verify(encoder).encode("long-enough-password");
        ArgumentCaptor<PlannerRecord> planner = ArgumentCaptor.forClass(PlannerRecord.class);
        verify(planners).save(planner.capture());
        assertEquals("{}", planner.getValue().getStateJson());
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        when(accounts.existsByEmail("learner@example.test")).thenReturn(true);

        ApiException error = assertThrows(ApiException.class, () -> service.register(
                new RegisterRequest("Learner", "learner@example.test", "long-enough-password", null)));

        assertEquals(409, error.getStatus());
        assertEquals("EMAIL_EXISTS", error.getCode());
        verify(accounts, never()).saveAndFlush(any());
    }

    @Test
    void registrationRejectsPasswordsOverBcryptByteLimit() {
        String password = "é".repeat(37);

        ApiException error = assertThrows(ApiException.class, () -> service.register(
                new RegisterRequest("Learner", "learner@example.test", password, null)));

        assertEquals(400, error.getStatus());
        verifyNoInteractions(accounts);
    }

    @Test
    void registrationRejectsInviteCodeMismatch() {
        AccountService invited = new AccountService(accounts, planners, state, encoder, true, "a-private-invite-code", true);

        ApiException error = assertThrows(ApiException.class, () -> invited.register(
                new RegisterRequest("Learner", "learner@example.test", "long-enough-password", "wrong-code")));

        assertEquals("INVITE_REQUIRED", error.getCode());
        verifyNoInteractions(accounts);
    }

    @Test
    void concurrentEmailConflictIsTranslatedToApiConflict() {
        when(accounts.existsByEmail("learner@example.test")).thenReturn(false);
        when(accounts.saveAndFlush(any(Account.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        ApiException error = assertThrows(ApiException.class, () -> service.register(
                new RegisterRequest("Learner", "learner@example.test", "long-enough-password", null)));

        assertEquals(409, error.getStatus());
        assertEquals("EMAIL_EXISTS", error.getCode());
        verifyNoInteractions(planners);
    }

    @Test
    void updateTrimsProfileFieldsAndRejectsOutOfRangeYear() {
        Account account = new Account("learner@example.test", "hash", "Old Name");
        when(accounts.findById("user")).thenReturn(java.util.Optional.of(account));

        var updated = service.update("user", new ProfileUpdateRequest(" New Name ", " Delhi ", " Bio ", null));

        assertEquals("New Name", updated.fullName());
        assertEquals("Delhi", updated.city());
        assertEquals("Bio", updated.bio());
        int tooFarAhead = java.time.Year.now().getValue() + 11;
        assertThrows(ApiException.class, () -> service.update("user",
                new ProfileUpdateRequest("Name", "", "", tooFarAhead)));
    }
}