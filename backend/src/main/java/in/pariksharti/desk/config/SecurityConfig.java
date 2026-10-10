package in.pariksharti.desk.config;

import in.pariksharti.desk.model.Account;
import in.pariksharti.desk.repository.AccountRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    UserDetailsService users(AccountRepository accounts) {
        return email -> {
            Account account = accounts.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
            // UUID is the authenticated identity; clients never choose a user ID.
            return User.withUsername(account.getId()).password(account.getPasswordHash()).roles("USER").build();
        };
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityContextRepository contextRepository() { return new HttpSessionSecurityContextRepository(); }

    @Bean
    CsrfTokenRepository csrfRepository() { return new HttpSessionCsrfTokenRepository(); }

    @Bean
    SessionAuthenticationStrategy sessionStrategy(CsrfTokenRepository csrf) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrf)));
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, SecurityContextRepository context, CsrfTokenRepository csrf) throws Exception {
        http.securityContext(c -> c.securityContextRepository(context))
            .csrf(c -> c.csrfTokenRepository(csrf))
            .requestCache(c -> c.disable())
            .authorizeHttpRequests(c -> c
                .requestMatchers("/api/auth/csrf", "/api/auth/config", "/api/auth/register", "/api/auth/login", "/api/health").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .exceptionHandling(c -> c
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(401); response.setContentType("application/json");
                    response.getWriter().write("{\"code\":\"AUTH_REQUIRED\",\"error\":\"Please sign in to continue.\"}");
                })
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(403); response.setContentType("application/json");
                    response.getWriter().write("{\"code\":\"FORBIDDEN\",\"error\":\"Refresh the page and try again.\"}");
                }))
            .logout(c -> c.logoutUrl("/api/auth/logout").deleteCookies("DESKSESSION")
                .logoutSuccessHandler((request, response, auth) -> response.setStatus(204)))
            .headers(c -> c.contentSecurityPolicy(p -> p.policyDirectives(
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'")));
        return http.build();
    }
}