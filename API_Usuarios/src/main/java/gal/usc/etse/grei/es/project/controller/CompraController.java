package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.exception.ErrorCodes;
import gal.usc.etse.grei.es.project.exception.ExceptionResponse;
import gal.usc.etse.grei.es.project.exception.ThrowHttpError;
import gal.usc.etse.grei.es.project.model.Compra;
import gal.usc.etse.grei.es.project.model.CompraInput;
import gal.usc.etse.grei.es.project.model.Usuario;
import gal.usc.etse.grei.es.project.service.CompraService;
import gal.usc.etse.grei.es.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.Email;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("compras")
@Tag(name = "User API", description = "Operaciones de compras")
@SecurityRequirement(name = "JWT")
@CrossOrigin(origins = "*")
public class CompraController {

    private final CompraService compras;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public CompraController(CompraService compras, LinkRelationProvider relationProvider) {
        this.compras = compras;
        this.relationProvider = relationProvider;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getAllCompras",
            summary = "Obtiene la lista de compras de los usuarios, paginada",
            description = "Se puede filtrar por email. Accesible únicamente para administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de compras solicitada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninguna compra encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No dispones de suficientes permisos para acceder al contenido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticación necesaria",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
    })
    ResponseEntity<Page<Compra>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name= "email", required = false ) @Email String email
            ){

        if( page < 0 ){
            ThrowHttpError.throwHTTPCode(ErrorCodes.ILLEGAL_PAGE_INDEX);
        }

        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) { return Sort.Order.asc(string.substring(1)); }
                    else if (string.startsWith("-")) { return Sort.Order.desc(string.substring(1)); }
                    else return null;
                })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

        Optional<Page<Compra>> listado = compras.get(page, size, Sort.by(criteria), email);

        if ( !listado.isPresent() ){
            throw new ResponseStatusException( ErrorCodes.SEARCH_NO_RESULT.getHttpStatus(), ErrorCodes.SEARCH_NO_RESULT.getErrorCode(), null);
        }

        Link self = linkTo( methodOn(CompraController.class).get(page, size, sort, email) ).withSelfRel();
        Link first = linkTo( methodOn(CompraController.class).get(listado.get().getPageable().first().getPageNumber(), size, sort, email) ).withRel(IanaLinkRelations.FIRST);
        Link last = linkTo( methodOn(CompraController.class).get(listado.get().getTotalPages() - 1, size, sort, email)).withRel(IanaLinkRelations.LAST);
        Link next = linkTo( methodOn(CompraController.class).get(listado.get().getPageable().next().getPageNumber(), size, sort, email) ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo( methodOn(CompraController.class).get(listado.get().getPageable().previousOrFirst().getPageNumber(), size, sort, email) ).withRel(IanaLinkRelations.PREVIOUS);
        Link one = linkTo( methodOn(CompraController.class).get(null) ).withRel(relationProvider.getItemResourceRelFor(Compra.class));

        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, first.toString())
                .header(HttpHeaders.LINK, last.toString())
                .header(HttpHeaders.LINK, next.toString())
                .header(HttpHeaders.LINK, previous.toString())
                .header(HttpHeaders.LINK, one.toString())
                .body(listado.get());
    }


    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@compraService.hasAccessRights(#id, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "getOneCompra",
            summary = "Obtener una compra por ID",
            description = "Necesita especificar ID de compra. Necesita estar identificado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra solicitada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra no encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No dispones de permisos suficientes para acceder al recurso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticación necesaria",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
    })
    ResponseEntity<Compra> get(@PathVariable("id") String id) {
        Optional<Compra> usuario = compras.get(id);
        if ( !usuario.isPresent() ) throw new ResponseStatusException( ErrorCodes.CONTENT_NOT_FOUND.getHttpStatus(), ErrorCodes.CONTENT_NOT_FOUND.getErrorCode(), null);

        Link self = linkTo(methodOn(CompraController.class).get(id)).withSelfRel();
        Link all = linkTo(CompraController.class).withRel(relationProvider.getCollectionResourceRelFor(Compra.class));

        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(usuario.get());
    }



    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "createCompra",
            summary = "Realiza una compra de un disco dentro del sistema.",
            description = "Permite que un usuario realice la compra de los objetos que tiene en el carrito."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Compra realizada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La información facilitada carece de los campos necesarios para completar la operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            )
    })
    ResponseEntity<Compra> insert(@RequestBody CompraInput compra) {
        Optional<Compra> result = compras.insert(compra);
        if(result.isEmpty()) {
            throw new ResponseStatusException(ErrorCodes.CREATION_CONFLICT.getHttpStatus(), ErrorCodes.CREATION_CONFLICT.getErrorCode(), null);
        } else {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(result.get().getId().toString()).build().toUri();

            Link self = linkTo(methodOn(CompraController.class).get(result.get().getId())).withSelfRel();
            Link all = linkTo(CompraController.class).withRel(relationProvider.getCollectionResourceRelFor(Compra.class));

            return ResponseEntity.created(uri)
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(result.get());
        }
    }


    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "deleteCompra",
            summary = "Elimina una compra del sistema.",
            description = "Elimina una compra que haya realizado un usuario. Necesitas ser administrador para eliminar compras."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Compra eliminado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Petición no autenticada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tienes permiso para acceder al recurso solicitado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compra no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            )
    })
    ResponseEntity<Void> delete(@PathVariable("id") String id) {
        if( !compras.delete(id) ){ throw new ResponseStatusException( ErrorCodes.CONTENT_NOT_FOUND.getHttpStatus(), ErrorCodes.CONTENT_NOT_FOUND.getErrorCode(), null); }

        Link all = linkTo(CompraController.class).withRel(relationProvider.getCollectionResourceRelFor(Compra.class));
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

}
