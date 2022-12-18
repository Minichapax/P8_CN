package gal.usc.grei.cn.carrito.controller;

import gal.usc.grei.cn.carrito.model.Carrito;
import gal.usc.grei.cn.carrito.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("Carritos")
@CrossOrigin(origins="*")
@Tag(name = "Carrito API", description = "Operacions relacionadas co carrito")
public class CarritoController{
    private final CarritoService carritos;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public CarritoController(CarritoService carros, LinkRelationProvider pr ){
        this.carritos=carros;
        this.relationProvider=pr;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )

    @Operation(
            operationId = "getallCarritos",
            summary = "Obtén todos os carritos, filtrando opcionalmente por usuario",
            description =   "Este método devolve en formato json todos os carritos da base de datos.\n" +
                    "Pódese filtrar a busca por \'usuario\'.\n" +
                    "Póense usar os parámetros de páxina \'page\', \'size\' e \'sort\'."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Devolve de maneira correcta todos os carritos en formato json."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request, parámetros incorrectos.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Non se encntraron carritos cos parámetros dados.",
                    content = @Content
            )
    })

    ResponseEntity<Page<Disco>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10")  int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "usuario",   required = false)  String usuario){

        if( page < 0 ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal Page Index.", null);
        }
        if( size < 1 ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal Size.", null);
        }

        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Optional<Page<Carrito>> resultado = carritos.get(page, size,  Sort.by(criteria), usuario);

        if(resultado.isPresent()) {
            Page<Carrito> data = resultado.get();
            Pageable metadata = data.getPageable();

            Link self = linkTo(
                    methodOn(CarritoController.class).get(page, size, sort, usuario)
            ).withSelfRel();
            Link first = linkTo(
                    methodOn(CarritoController.class).get(metadata.first().getPageNumber(), size, sort, usuario)
            ).withRel(IanaLinkRelations.FIRST);
            Link last = linkTo(
                    methodOn(CarritoController.class).get(data.getTotalPages() - 1, size, sort, usuario)
            ).withRel(IanaLinkRelations.LAST);
            Link next = linkTo(
                    methodOn(CarritoController.class).get(metadata.next().getPageNumber(), size, sort, usuario)
            ).withRel(IanaLinkRelations.NEXT);
            Link previous = linkTo(
                    methodOn(CarritoController.class).get(metadata.previousOrFirst().getPageNumber(), size, sort, usuario)
            ).withRel(IanaLinkRelations.PREVIOUS);

            Link one = linkTo(
                    methodOn(CarritoController.class).get(null)
            ).withRel(relationProvider.getItemResourceRelFor(Disco.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(resultado.get());
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )

    @Operation(
            operationId = "getunCarrito",
            summary = "Obtén un carrito por id",
            description =   "Este método devolve en formato json o carrito da base de datos con esa id, se existe.\n" +
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Devolve de maneira correcta todos os carritos en formato json."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Non se encntrou ningún carrito coa id dada.",
                    content = @Content
            )
    })
    public ResponseEntity<Carrito> get(@PathVariable("id")  String id){
        Optional<Page<Carrito>> resultado = carritos.get(id);

        if(resultado.isPresent()) {

            Link self = linkTo(
                    methodOn(CarritoController.class).get(id)
            ).withSelfRel();
            Link all = linkTo(
                    methodOn(CarritoController.class).get(0, 20, null, "null")
            ).withSelfRel();

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(resultado.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )

    @Operation(
            operationId = "postCarrito",
            summary = "Crea un carrito na base de datos.",
            description =   "Crea un carrito en formato json na base de datos.\n" +
                    "Devolve o carrito creado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Devolve o carrito creado."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            )
    })
    public ResponseEntity<Carrito> post(@RequestBody @Valid Carrito carro){
        Optional<Carrito> resultado = this.carritos.post(carro);
        if(resultado.equals(Optional.empty())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else if(resultado.isPresent()){

            Link self = linkTo(
                    methodOn(CarritoController.class).get(carro.getId())
            ).withSelfRel();
            Link all = linkTo(
                    methodOn(CarritoController.class).get(0, 20, null, "null")
            ).withSelfRel();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(resultado.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )

    @Operation(
            operationId = "deleteCarrito",
            summary = "Elimina un carrito por id",
            description =   "Este método  elimina o carrito da base de datos con esa id, se existe.\n" +
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito borrado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Non se encntrou ningún carrito coa id dada.",
                    content = @Content
            )
    })

    public ResponseEntity delete(@PathVariable("id")  String id){
        HttpStatus resultado = this.carritos.delete(id);
        Link all = linkTo(
                methodOn(CarritoController.class).get(0, 20, null, "null")
        ).withSelfRel();
        return ResponseEntity.status(resultado)
                .header(HttpHeaders.LINK, all.toString())
                .body(null);
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )

    @Operation(
            operationId = "patchCarrito",
            summary = "Modifica un carrito na base de datos.",
            description =   "Modifica o carrito coa id dada en formato jsonPatch na base de datos.\n" +
                    "Pódese modificar todo menos a id"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Modificación correcta do carrito, os novos datos devólvense no body."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Non se encntrou ningún carrito coa id dada.",
                    content = @Content
            )
    })

    public ResponseEntity<Carrito> patch(
            @PathVariable("id") String id,
            @RequestBody List<Map<String, Object>> updates){
        try {
            for (Map m : updates){
                if(m.get("path").equals("/id")){
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
            }

            Optional<Page<Carrito>> resultado = this.carritos.patch(id, updates);

            if (resultado.isPresent()) {

                Link self = linkTo(
                        methodOn(CarritoController.class).get(id)
                ).withSelfRel();
                Link all = linkTo(
                        methodOn(CarritoController.class).get(0, 20, null, "null")
                ).withSelfRel();

                return ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(resultado.get());
            }
            return ResponseEntity.notFound().build();

        }catch(Exception e){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


}