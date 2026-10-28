package pe.upeu.ecorecicla.seguridad;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                Claims claims = jwtService.validarYObtenerClaims(token);
                String usuarioId = claims.getSubject();
                String username = claims.get("usuario", String.class);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                @SuppressWarnings("unchecked")
                List<String> privilegios = (List<String>) claims.get("privilegios");
                if (privilegios != null) {
                    for (String p : privilegios) {
                        authorities.add(new SimpleGrantedAuthority(p));
                    }
                }

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");
                if (roles != null) {
                    for (String r : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + r));
                    }
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    usuarioId,
                    authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

