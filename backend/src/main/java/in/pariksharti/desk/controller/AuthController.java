package in.pariksharti.desk.controller;

import in.pariksharti.desk.dto.LoginRequest;
import in.pariksharti.desk.dto.ProfileResponse;
import in.pariksharti.desk.dto.ProfileUpdateRequest;
import in.pariksharti.desk.dto.RegisterRequest;
import in.pariksharti.desk.exception.ApiException;
import in.pariksharti.desk.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private record Attempts(int count, long resetAt) {}

    private final ConcurrentHashMap<String, Attempts> failures = new ConcurrentHashMap<>();
    private final AccountService accounts;
    private final AuthenticationManager manager;
    private final SecurityContextRepository contexts;
    private final SessionAuthenticationStrategy sessions;

    public AuthController(AccountService accounts, AuthenticationManager manager,
            SecurityContextRepository contexts, SessionAuthenticationStrategy sessions) {
        this.accounts = accounts;
        this.manager = manager;
        this.contexts = contexts;
        this.sessions = sessions;
    }

    @GetMapping("/auth/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @GetMapping("/auth/config")
    public Map<String, Boolean> config() {
        return Map.of("registrationEnabled", accounts.registrationEnabled(), "inviteRequired", accounts.inviteRequired());
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody RegisterRequest body) { return accounts.register(body); }

    @PostMapping("/auth/login")
    public ProfileResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        String email = body.email().trim().toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();
        failures.entrySet().removeIf(e -> e.getValue().resetAt() < now);
        Attempts attempts = failures.get(email);
        if (attempts != null && attempts.count() >= 8)
            throw new ApiException(429, "LOGIN_LIMIT", "Too many unsuccessful attempts. Try again in 15 minutes.");
        Authentication auth;
        try {
            if (body.password().getBytes(StandardCharsets.UTF_8).length > 72)
                throw new BadCredentialsException("Invalid credentials");
            auth = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, body.password()));
        } catch (AuthenticationException e) {
            failures.compute(email, (key, prev) -> new Attempts(prev == null ? 1 : prev.count() + 1, prev == null ? now + 900000 : prev.resetAt()));
            throw new ApiException(401, "INVALID_CREDENTIALS", "Incorrect email or password.");
        }
        failures.remove(email);
        sessions.onAuthentication(auth, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return accounts.profile(auth.getName());
    }

    @GetMapping("/auth/me")
    public ProfileResponse me(Authentication auth) { return accounts.profile(auth.getName()); }

    @PutMapping("/profile")
    public ProfileResponse profile(@Valid @RequestBody ProfileUpdateRequest body, Authentication auth) {
        return accounts.update(auth.getName(), body);
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "ok"); }
}