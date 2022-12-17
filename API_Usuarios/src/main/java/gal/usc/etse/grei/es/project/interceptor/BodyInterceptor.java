package gal.usc.etse.grei.es.project.interceptor;

import gal.usc.etse.grei.es.project.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

public class BodyInterceptor implements HandlerInterceptor {

    /**
     * Nos permite interceptar las peticiones HTTP que recibe el servidor y actuar ante ello.
     * En este caso lo empleamos para obligar a que las peticiones GET y DELETE no contengan datos en el cuerpo.
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String metodo = request.getMethod();

        try {
            if ((metodo.equalsIgnoreCase("GET") || metodo.equalsIgnoreCase("DELETE"))
                    && !request.getReader().lines().collect(Collectors.joining(System.lineSeparator())).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCodes.ILLEGAL_BODY_CONTENT.getErrorCode(), null);
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorCodes.ILLEGAL_BODY_CONTENT.getErrorCode(), null);
        }

        return true;
    }
}