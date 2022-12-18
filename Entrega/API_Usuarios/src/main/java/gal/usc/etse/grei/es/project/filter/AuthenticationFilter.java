package gal.usc.etse.grei.es.project.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager manager;
    private final Key key;

    // Establecemos unha duración para os tokens
    private static final long TOKEN_DURATION = Duration.ofMinutes(120).toMillis();

    public AuthenticationFilter(AuthenticationManager manager, Key key){
        this.manager = manager;
        this.key = key;
    }

    // Método que tenta autenticar ao usuario a partir da chamada HTTP
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // Obtemos o obxecto JSON do body da request HTTP
        JsonNode credentials = null;
        try {
            credentials = new ObjectMapper().readValue(request.getInputStream(), JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Tentamos autenticarnos coas credenciais proporcionadas
        return manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.get("email").textValue(),
                        credentials.get("contrasena").textValue()
                )
        );
    }

    // Método que se chama cando a autenticación do metodo anterior é satisfactoria
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        // Almacenamos o momento actual
        long now = System.currentTimeMillis();

        // Obtemos a lista de roles asignados ao usuario e concatenamolo nun string separado por comas
        String authorities = authResult.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Creamos o token JWT empregando o builder
        JwtBuilder tokenBuilder = Jwts.builder()
                // Establecemos como "propietario" do token ao usuario que fixo login
                .setSubject(((User)authResult.getPrincipal()).getUsername())
                // Establecemos a data de emisión do token
                .setIssuedAt(new Date(now))
                // Establecemos a data máxima de validez do token
                .setExpiration(new Date(now + TOKEN_DURATION))
                // Engadimos un atributo máis ao corpo do token cos roles do usuario
                .claim("roles", authorities)
                // Asinamos o token coa nosa clave secreta
                .signWith(key);

        // Devolvemos o token JWT acabado de crear
        response.addHeader("Content-Type", "application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(String.format("{\n\t\"access_token\": \"%s\",\n\t\"token_type\": \"Bearer\",\n\t\"expires_in\": %d\n}", tokenBuilder.compact(), TOKEN_DURATION));
            response.getWriter().flush();
        } catch (Exception ignored){
        }
    }

}
