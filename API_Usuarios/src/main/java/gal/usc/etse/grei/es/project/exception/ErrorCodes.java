package gal.usc.etse.grei.es.project.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCodes {
    FORBIDDEN("El cliente no tiene derechos de acceso al contenido", "403", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("La solicitud carece de credenciales de autenticación válidas", "401", HttpStatus.UNAUTHORIZED),

    ILLEGAL_BODY_CONTENT("El contenido del cuerpo de la solicitud es ilegal", "2000", HttpStatus.BAD_REQUEST),
    ILLEGAL_PAGE_INDEX("El índice de página debe ser mayor o igual que cero", "2001", HttpStatus.BAD_REQUEST),
    ILLEGAL_DATE_FORMAT("El formato de fecha suministrado no corresponde a un formato válido", "2002", HttpStatus.BAD_REQUEST),
    BODY_NOT_READABLE("El contenido del cuerpo suministrado no contiene una estructura válida", "2003", HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_FIELDS("La información suministrada carece de los campos necesarios para completar la operación", "2004", HttpStatus.BAD_REQUEST),
    INVALID_RESOURCE_FORMAT("La información suministrada no forma una estructura de recurso válida", "2004", HttpStatus.BAD_REQUEST),
    EXCLUDING_PARAMETERS("Los parámetros excluidos no pueden formar parte de la misma solicitud", "2005", HttpStatus.BAD_REQUEST),
    JSONPATCH_FORMAT("Al menos una operación solicitada no cumple el estándar RFC 6902", "2006", HttpStatus.BAD_REQUEST),
    JSONPATCH_SCHEMA("Al menos una operación solicitada no cumple con el esquema de recursos solicitado", "2007", HttpStatus.BAD_REQUEST),
    UNPROCESSABLE("El servidor es incapaz de procesar la solicitud definida en el esquema dado", "2008", HttpStatus.UNPROCESSABLE_ENTITY),
    PATCH_FORBIDDEN_OP("Al menos una de las rutas indicadas no acepta la operación suministrada", "2009", HttpStatus.BAD_REQUEST),
    FILM_NOT_FOUND("La película indicada no existe", "2010", HttpStatus.BAD_REQUEST),
    ASSESSMENT_NEED_FILTER("Debe especificar un parámetro de exclusión entre el usuario y la película", "2011", HttpStatus.BAD_REQUEST),

    URL_NOT_FOUND("La ruta a la que intenta acceder no existe", "3000", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("La ruta a la que intenta acceder no acepta el método HTTP solicitado", "3001", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE("El formato de la carga útil está en un método de formato no admitido", "3002", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    CONTENT_NOT_FOUND("El elemento buscado no existe", "4000", HttpStatus.NOT_FOUND),
    SEARCH_NO_RESULT("Su combinación de parámetros de búsqueda no ha devuelto ningún elemento", "4001", HttpStatus.NOT_FOUND),
    CREATION_CONFLICT("La combinación de parámetros proporcionada ya existe en el sistema", "5000", HttpStatus.CONFLICT);


    private final String error_desc;
    private final String error_code;

    private final HttpStatus httpStatus;

    private static final Map<String, ErrorCodes> lookup = new HashMap<>();
    static {
        for (ErrorCodes e : ErrorCodes.values()) {
            lookup.put(e.getErrorCode(), e);
        }
    }

    private ErrorCodes (String error_desc, String error_code, HttpStatus httpStatus){
        this.error_code = error_code;
        this.error_desc = error_desc;
        this.httpStatus = httpStatus;
    }

    public String getErrorDesc() {
        return error_desc;
    }

    public String getErrorCode() {
        return error_code;
    }

    public HttpStatus getHttpStatus(){
        return httpStatus;
    }
    public static String getErrorByCode(String code) {
        return lookup.get(code).error_desc;
    }
}
