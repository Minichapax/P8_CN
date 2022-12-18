package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.exception.ErrorCodes;
import gal.usc.etse.grei.es.project.exception.ExceptionResponse;
import gal.usc.etse.grei.es.project.exception.ThrowHttpError;
import gal.usc.etse.grei.es.project.model.Usuario;
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
@RequestMapping("usuarios")
@Tag(name = "User API", description = "Operaciones de usuarios")
@SecurityRequirement(name = "JWT")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService users;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public UserController(UserService users, LinkRelationProvider relationProvider) {
        this.users = users;
        this.relationProvider = relationProvider;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getAllUsers",
            summary = "Obtiene la lista de usuarios, paginada",
            description = "Se puede filtrar por email y nombre. Accesible únicamente para administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de usuarios solicitada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ningún usuario encontrado",
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
    ResponseEntity<Page<Usuario>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name= "name", required = false ) String name,
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

        Optional<Page<Usuario>> listado = users.get(page, size, Sort.by(criteria), name, email);

        if (listado.isPresent()){
            throw new ResponseStatusException( ErrorCodes.SEARCH_NO_RESULT.getHttpStatus(), ErrorCodes.SEARCH_NO_RESULT.getErrorCode(), null);
        }

        Link self = linkTo( methodOn(UserController.class).get(page, size, sort, email, name) ).withSelfRel();
        Link first = linkTo( methodOn(UserController.class).get(listado.get().getPageable().first().getPageNumber(), size, sort, email, name) ).withRel(IanaLinkRelations.FIRST);
        Link last = linkTo( methodOn(UserController.class).get(listado.get().getTotalPages() - 1, size, sort, email, name)).withRel(IanaLinkRelations.LAST);
        Link next = linkTo( methodOn(UserController.class).get(listado.get().getPageable().next().getPageNumber(), size, sort, email, name) ).withRel(IanaLinkRelations.NEXT);
        Link previous = linkTo( methodOn(UserController.class).get(listado.get().getPageable().previousOrFirst().getPageNumber(), size, sort, email, name) ).withRel(IanaLinkRelations.PREVIOUS);
        Link one = linkTo( methodOn(UserController.class).get(null) ).withRel(relationProvider.getItemResourceRelFor(Usuario.class));

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
    @PreAuthorize("@userService.hasAccessRights(#id, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "getOneUser",
            summary = "Obtener un usuario por ID",
            description = "Necesita especificar ID de usuario. Necesita estar conectado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario solicitado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
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
    ResponseEntity<Usuario> get(@PathVariable("id") String id) {
        Optional<Usuario> usuario = users.get(id);
        if (usuario.isPresent()) throw new ResponseStatusException( ErrorCodes.CONTENT_NOT_FOUND.getHttpStatus(), ErrorCodes.CONTENT_NOT_FOUND.getErrorCode(), null);

        Link self = linkTo(methodOn(UserController.class).get(id)).withSelfRel();
        Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Usuario.class));

        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(usuario.get());
    }


    @GetMapping(
            path = "@me",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('USER')")
    @Operation(
            operationId = "getUserMe",
            summary = "Obtiene el usuario autenticado actualmente.",
            description = "Necesitas estar autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario autenticado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class)
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
    ResponseEntity<Usuario> getMe() {
        Optional<Usuario> usuario = users.getbyEmail( (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal() );

        Link self = linkTo(methodOn(UserController.class).getMe()).withSelfRel();
        Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Usuario.class));

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
            operationId = "createUser",
            summary = "Crea un nuevo usuario en el sistema.",
            description = "Permite la creación de un nuevo usuario en el sistema para poder interactuar con éste."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Creado el nuevo usuario",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La información facilitada carece de los campos necesarios para completar la operación.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La combinación de parámetros suministrada ya existe en el sistema",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
    })
    ResponseEntity<Usuario> insert(@RequestBody Usuario usuario) {
        Optional<Usuario> result = users.insert(usuario);
        if(result.isPresent()) {
            throw new ResponseStatusException(ErrorCodes.CREATION_CONFLICT.getHttpStatus(), ErrorCodes.CREATION_CONFLICT.getErrorCode(), null);
        } else {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(result.get().getId()).build().toUri();

            Link self = linkTo(methodOn(UserController.class).get(result.get().getEmail())).withSelfRel();
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Usuario.class));

            return ResponseEntity.created(uri)
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(result.get());
        }
    }


    @PatchMapping(
            path = "{id}",
            consumes = "application/json-patch+json",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@userService.hasAccessRights(#id, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "patchUser",
            summary = "Editar un usuario en el sistema.",
            description = "Permite la edición de un usuario. Sólo es posible editar el usuario actual. " +
                    "Sólo se pueden editar los campos email y nombre. Los demás serán ignorados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Usuario.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los comandos PATCH no disponen de un esquema adecuado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
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
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "El servidor es incapaz de procesar la solicitud definida en el esquema enviado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            ),
    })
    ResponseEntity<Usuario> patch(@PathVariable("id") String id, @RequestBody List<Map<String, Object>> operaciones) {
        Optional<Usuario> usuario = users.patch(id, operaciones);

        Link self = linkTo(methodOn(UserController.class).get(id)).withSelfRel();
        Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Usuario.class));

        return ResponseEntity.ok()
                .header(HttpHeaders.LINK, self.toString())
                .header(HttpHeaders.LINK, all.toString())
                .body(usuario.get());
    }


    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@userService.hasAccessRights(#id, principal) or hasRole('ADMIN')")
    @Operation(
            operationId = "deleteUser",
            summary = "Elimina un usuario del sistema.",
            description = "Elimina un usuario del sistema de datos. Sólo puede realizarlo el propio usuario o un administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario eliminado"
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
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExceptionResponse.class)
                    )
            )
    })
    ResponseEntity<Void> delete(@PathVariable("id") String id) {
        if( !users.delete(id) ){ throw new ResponseStatusException( ErrorCodes.CONTENT_NOT_FOUND.getHttpStatus(), ErrorCodes.CONTENT_NOT_FOUND.getErrorCode(), null); }

        Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Usuario.class));
        return ResponseEntity.noContent()
                .header(HttpHeaders.LINK, all.toString())
                .build();
    }

}
