package gal.usc.etse.grei.es.project.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class CustomErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public final ResponseEntity<ExceptionResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {

        ExceptionResponse er = new ExceptionResponse(
                ex.getStatus().toString(),
                ex.getReason(),
                ErrorCodes.getErrorByCode(ex.getReason())
        );

        return new ResponseEntity<>(er, ex.getStatus() );
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public final ResponseEntity<ExceptionResponse> handleConflict(AccessDeniedException ex, HttpServletResponse response) throws IOException {
        Authentication authUser = SecurityContextHolder.getContext().getAuthentication();
        ErrorCodes error;

        boolean isAuthenticated = !(
                authUser.getAuthorities().size() == 1
                        && authUser.getAuthorities().contains(AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ANONYMOUS").get(0))
        );

        // El error depende únicamente de si está o no identificado el usuario.
        // Si NO lo está, siempre estará UNAUTHORIZED
        // Si está identificado, significa que no tiene permisos en la ruta (FORBIDDEN)
        if ( isAuthenticated ){
            error = ErrorCodes.FORBIDDEN;
        } else {
            error = ErrorCodes.UNAUTHORIZED;
        }

        return new ResponseEntity<>(new ExceptionResponse(error.getHttpStatus().toString(), error.getErrorCode(), error.getErrorDesc()), error.getHttpStatus());
    }

    @Override
    public final ResponseEntity<Object> handleNoHandlerFoundException( NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request){
        ErrorCodes error = ErrorCodes.URL_NOT_FOUND;
        ExceptionResponse er = new ExceptionResponse( "404 NOT_FOUND", error.getErrorCode(), error.getErrorDesc() );
        return new ResponseEntity<>(er, error.getHttpStatus() );
    }

    @Override
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported( HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorCodes error = ErrorCodes.METHOD_NOT_ALLOWED;
        ExceptionResponse er = new ExceptionResponse( "405 METHOD_NOT_ALLOWED", error.getErrorCode(), error.getErrorDesc() );
        return new ResponseEntity<>(er, error.getHttpStatus() );
    }

    @Override
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorCodes error = ErrorCodes.UNSUPPORTED_MEDIA_TYPE;
        ExceptionResponse er = new ExceptionResponse( "415 UNSUPPORTED_MEDIA_TYPE", error.getErrorCode(), error.getErrorDesc() );
        return new ResponseEntity<>(er, error.getHttpStatus() );
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable( HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorCodes error = ErrorCodes.BODY_NOT_READABLE;
        ExceptionResponse er = new ExceptionResponse( "400 BAD_REQUEST", error.getErrorCode(), error.getErrorDesc() );
        return new ResponseEntity<>(er, error.getHttpStatus() );
    }


    public ResponseEntity<Object> handleMethodArgumentNotValid( MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorCodes error = ErrorCodes.INVALID_RESOURCE_FORMAT;
        ExceptionResponse er = new ExceptionResponse( "400 BAD_REQUEST", error.getErrorCode(), error.getErrorDesc() );
        return new ResponseEntity<>(er, error.getHttpStatus() );
    }

    @Override
    public final ResponseEntity<Object> handleExceptionInternal( Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request ) {
        System.out.println("ERROR -> " + status.toString() + " type: " + ex.getClass() );

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body, headers, status);
    }


}