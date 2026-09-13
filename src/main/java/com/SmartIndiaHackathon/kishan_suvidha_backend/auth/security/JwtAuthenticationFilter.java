package com.SmartIndiaHackathon.kishan_suvidha_backend.auth.security;

import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.entity.User;
import com.SmartIndiaHackathon.kishan_suvidha_backend.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Read Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        // 2. If there is no Bearer token, continue normally
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract JWT
        String token = authorizationHeader.substring(7);

        try {
            // 4. Validate JWT
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Extract user ID from JWT
            Long userId = jwtService.extractUserId(token);

            // 6. Load user from database
            User user = userRepository.findById(userId)
                    .orElse(null);

            // 7. Make sure user exists and is enabled
            if (user == null || !user.isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }
            // 8. Create Spring Security authority
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()
                    );
            // 9. Create authenticated user
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getId(),
                            null,
                            List.of(authority)
                    );
            // 10. Put authentication into SecurityContext
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        } catch (Exception exception) {

            // Invalid/malformed JWT should not crash the request.
            // The protected endpoint will subsequently return 401.
        }
        // 11. Continue request
        filterChain.doFilter(request, response);
    }
}