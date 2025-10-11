package porunit.w8.realtydb.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Разрешаем preflight без ключа
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader("X-API-Key");
        if (provided != null) provided = provided.trim();

        if (StringUtils.hasText(expectedApiKey) && expectedApiKey.equals(provided)) {
            // ✅ Аутентифицируем запрос
            String finalProvided = provided;
            AbstractAuthenticationToken auth =
                    new AbstractAuthenticationToken(List.of(new SimpleGrantedAuthority("ROLE_API"))) {
                        @Override public Object getCredentials() { return finalProvided; }
                        @Override public Object getPrincipal() { return "api-key-user"; }
                    };
            auth.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
            return;
        }

        // ❌ Неверный/отсутствующий ключ → свой ответ с телом
        log.warn("Unauthorized request: path={}, method={}, hasHeader={}",
                request.getRequestURI(), request.getMethod(), (provided != null));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Unauthorized: invalid or missing X-API-Key\"}");
    }
}
