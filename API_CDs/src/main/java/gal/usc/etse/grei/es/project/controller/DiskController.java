package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Disco;
import gal.usc.etse.grei.es.project.service.DiskService;
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
@RequestMapping("disks")
@CrossOrigin(origins="*")
@Tag(name = "Disk API", description = "Disk related operations")
public class DiskController {
    private final DiskService disks;
    private final LinkRelationProvider relationProvider;

    @Autowired
            public DiskController(DiskService diskservice, LinkRelationProvider lp) {
                this.disks = diskservice;
                this.relationProvider = lp;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getAllDisks",
            summary = "Obtener todos los discos, filtrando por nombre o artista.",
            description =   "Este método devuelve en formato json todos los discos de la base de datos.\n" +
                            "Puedes filtrar por \'name\' o por \'artist\'.\n" +
                            "Puedes usar parámetros de página: \'page\', \'size\' y \'sort\'."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Devuelve de manera correcta los discos en formato json."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request, parámetros incorrectos.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se han encontrado discos con los parámetros dados.",
                    content = @Content
            )
    })
        ResponseEntity<Page<Disco>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "name",   required = false) String name,
            @RequestParam(name = "artist", required = false) String artist
    ) {

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

        Optional<Page<Disco>> result = disks.get(page, size, Sort.by(criteria), name, artist);

        if(result.isPresent()) {
            Page<Disco> data = result.get();
            Pageable metadata = data.getPageable();

            Link self = linkTo(
                    methodOn(DiskController.class).get(page, size, sort, name, artist)
            ).withSelfRel();
            Link first = linkTo(
                    methodOn(DiskController.class).get(metadata.first().getPageNumber(), size, sort, name, artist)
            ).withRel(IanaLinkRelations.FIRST);
            Link last = linkTo(
                    methodOn(DiskController.class).get(data.getTotalPages() - 1, size, sort, name, artist)
            ).withRel(IanaLinkRelations.LAST);
            Link next = linkTo(
                    methodOn(DiskController.class).get(metadata.next().getPageNumber(), size, sort, name, artist)
            ).withRel(IanaLinkRelations.NEXT);
            Link previous = linkTo(
                    methodOn(DiskController.class).get(metadata.previousOrFirst().getPageNumber(), size, sort, name, artist)
            ).withRel(IanaLinkRelations.PREVIOUS);

            Link one = linkTo(
                    methodOn(DiskController.class).get(null)
            ).withRel(relationProvider.getItemResourceRelFor(Disco.class));

            // HATEOAS HEADERS
            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(result.get());
        }

        return ResponseEntity.notFound().build();

    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getOneDisk",
            summary = "Obtener un disco por su id.",
            description =   "Este método devuelve el disco, si existe, en formato json." +
                            ""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Devuelve de manera correcta los detalles del disco."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Disco no encontrado.",
                    content = @Content
            ),
    })
    public ResponseEntity<Disco> get(@PathVariable("id") String id) {

        Optional<Disco> result = disks.get(id);

            if(result.isPresent()) {

                Link self = linkTo(
                        methodOn(DiskController.class).get(id)
                ).withSelfRel();
                Link all = linkTo(
                        methodOn(DiskController.class).get(0, 20, null, "null", "null")
                ).withSelfRel();
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
        }
        return ResponseEntity.notFound().build();

    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "postDisk",
            summary = "Método para crear un disco en la base de datos.",
            description =   "Crea un disco pasando los campos en el body de la petición en formato json." +
                            "Devolverá el disco con el id asignado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Devuelve el propio disco creado con el id asignado."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            )
    })
    ResponseEntity<Disco> post(@RequestBody @Valid Disco disco) {
                if(disco.getId() !=null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                Optional<Disco> result = disks.post(disco);
                if(result.equals(Optional.empty())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if(result.isPresent()) {
            Link self = linkTo(
                    methodOn(DiskController.class).get(disco.getId())
            ).withSelfRel();
            Link all = linkTo(
                    methodOn(DiskController.class).get(0, 20, null, "null", "null")
            ).withSelfRel();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(result.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "deleteDisk",
            summary = "Borrar un disco por completo.",
            description = "Borrará el disco seleccionado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Borrado correcto sin devolver contenido."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Disco no encontrado.",
                    content = @Content
            )
    })
    ResponseEntity delete(@PathVariable("id") String id) {
        HttpStatus a = disks.delete(id);

            Link all = linkTo(
                    methodOn(DiskController.class).get(0, 20, null, "null", "null")
            ).withSelfRel();
            return ResponseEntity.status(a)
                    .header(HttpHeaders.LINK, all.toString())
                    .body(null);
    }

    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
        )
    @Operation(
            operationId = "patchDisk",
            summary = "Modifica un disco con el formato jsonPatch.",
            description = "Se modificará el disco de la id asignada. " +
                        "Utiliza el formato jsonpatch, podrás modificar todo menos el id."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modificación correcta del disco, se devuelve en el body los nuevos datos."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request, comprueba el jsonpatch.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Disco no encontrado.",
                    content = @Content
            )
    })
    ResponseEntity<Disco> patch(
            @PathVariable("id") String id,
            @RequestBody List<Map<String, Object>> updates
    ){
        try {
            for (Map m : updates){
                if(m.get("path").equals("/id")){
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
            }
            Optional<Disco> result = disks.patch(id, updates);

            if(result.isPresent()) {

                Link self = linkTo(
                        methodOn(DiskController.class).get(result.get().getId())
                ).withSelfRel();
                Link all = linkTo(
                        methodOn(DiskController.class).get(0, 20, null, "null", "null")
                ).withSelfRel();
                return ResponseEntity.status(HttpStatus.OK)
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(result.get());
            }
            return ResponseEntity.notFound().build();
        }catch(Exception e){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
