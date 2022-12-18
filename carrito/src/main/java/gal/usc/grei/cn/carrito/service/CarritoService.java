package gal.usc.grei.cn.carrito.service;

import gal.usc.grei.cn.carrito.model.Carrito;
import gal.usc.grei.cn.carrito.repository.CarritoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Optional;

@Service
public class CarritoService{
    private final CarritoRepository carrito;
    private final PatchUtils patchUtils;
    private final ObjectMapper mapper;

    @Autowired
    public CarritoService(CarritoRepository carro, PatchUtils patchUtils, ObjectMapper mapper) {
        this.carritos=carro;
        this.patchUtils=patchUtils;
        this.mapper=mapper;

    }

    public Optional<Page<Carrito>> get(int page, int size, Sort sort, String usuario){
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
            .withIgnoreCase()
            .withIgnoreNullValues()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Carrito carro= new Carrito();

        if(usuario!= null){
            carro.setCookie_user(usuario);
        }

        Page<Carrito> resultado = carritos.findAll( Example.of(carro, matcher), request);
        if(resultado.isEmpty()){
            return Optional.empty();
        }
        else{
            return  Optional.of(resultado);
        }
    }

    public Optional<Carrito> get(String id) {
            return carritos.findById(id);
    }

    public Optional<Carrito> post(Carrito carro){
        String id=carro.getId()
        if(carritos.findById(id).equals(Optional.empty())){
            return Optional.of(carritos.insert(carro));
        }
        else{
            carritos.deleteById(id);
            return Optional.of(carritos.insert(carro));
        }
    }

    public HttpStatus delete(String id)  {
        if(carritos.findById(id).equals(Optional.empty())){
            return HttpStatus.NOT_FOUND;
        }
        else{
            carritos.deleteById(id);
            return HttpStatus.NO_CONTENT;
        }
    }

    public Optional<Carrito> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException{
        return carritos.findById(id)
            .map(patchcarro -> {
                PatchUtils pu = new PatchUtils(new ObjectMapper());
                try {
                patchcarro = pu.patchUtil(patchcarro, updates);
                } catch (JsonPatchException e) {
                    throw new RuntimeException(e);
                }
                return disks.save(patchcarro);
            });
    }

}

