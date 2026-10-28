package pe.upeu.ecorecicla.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.upeu.ecorecicla.dominio.Privilegio;
import pe.upeu.ecorecicla.dominio.Rol;
import pe.upeu.ecorecicla.dominio.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long expirationMinutes;

    public JwtService(
        @Value("${app.jwt.secret:c2VjdXJpdHktZXhhbWVuLXUyLWVjb3JlY2ljbGEtc2VjcmV0LWtleS0yMDI2}") String secret,
        @Value("${app.jwt.issuer:ecorecicla-auth}") String issuer,
        @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generarToken(Usuario usuario) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        List<String> privilegios = usuario.getTodosLosPrivilegios().stream()
            .map(Privilegio::getCodigo)
            .distinct()
            .collect(Collectors.toList());

        List<String> roles = usuario.getRoles().stream()
            .map(Rol::getNombre)
            .collect(Collectors.toList());

        return Jwts.builder()
            .subject(usuario.getId().toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("usuario", usuario.getUsername())
            .claim("nombre", usuario.getNombre())
            .claim("privilegios", privilegios)
            .claim("roles", roles)
            .signWith(key)
            .compact();
    }

    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Instant getExpiraEn(Claims claims) {
        return claims.getExpiration().toInstant();
    }
}

