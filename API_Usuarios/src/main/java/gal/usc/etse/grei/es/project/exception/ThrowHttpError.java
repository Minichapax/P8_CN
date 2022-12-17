package gal.usc.etse.grei.es.project.exception;

import org.springframework.web.server.ResponseStatusException;

public class ThrowHttpError {

    public static void throwHTTPCode(ErrorCodes code) throws ResponseStatusException {
        throw new ResponseStatusException(code.getHttpStatus(), code.getErrorCode(), null);
    }

}
