package gal.usc.etse.grei.es.project.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public class ExceptionResponse {

    @Schema(description = "Código de estado HTTP y descripción", example = "400 BAD_REQUEST")
    private String status;

    @Schema(description = "Código de error del sistema. Se relaciona con un error identificable dentro del sistema", example = "2001")
    private String code;

    @Schema(description = "Descripción del error del sistema. Descripción del código de error interno devuelto", example = "El índice de página debe ser mayor o igual que cero")
    private String desc;

    public ExceptionResponse(String status, String code, String desc) {
        super();
        this.status = status;
        this.code = code;
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

