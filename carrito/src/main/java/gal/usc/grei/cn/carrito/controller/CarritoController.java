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

    ResponseEntity<Page<Disco>> get(int page, int size, List<String> sort, String usuario){
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

    public ResponseEntity<Carrito> get(String id){
        Optional<Page<Carrito>> resultado = carritos.get(id);

        if(resultado.isPresent()) {

            Link self = linkTo(
                    methodOn(CarritoController.class).get(id)
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

    public ResponseEntity<Carrito> post(Carrito carro){
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

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(resultado.get());
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity delete(String id){
        HttpStatus resultado = this.carritos.delete(id);
        Link all = linkTo(
                methodOn(CarritoController.class).get(0, 20, null, "null")
        ).withSelfRel();
        return ResponseEntity.status(resultado)
                .header(HttpHeaders.LINK, all.toString())
                .body(null);
    }

    public ResponseEntity<Carrito> patch(String id, List<Map<String, Object>> updates){
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

                return ResponseEntity.status(HttpStatus.CREATED)
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