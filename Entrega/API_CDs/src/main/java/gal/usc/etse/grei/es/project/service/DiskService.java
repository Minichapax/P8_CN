package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gal.usc.etse.grei.es.project.model.Disco;
import gal.usc.etse.grei.es.project.repository.DiskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Optional;

@Service
public class DiskService {

    private final DiskRepository disks;
    private final PatchUtils patchUtils;
    private final ObjectMapper mapper;

    @Autowired
    public DiskService(DiskRepository disk, PatchUtils patchUtils, ObjectMapper mapper) {
        this.disks = disk;
        this.patchUtils = patchUtils;
        this.mapper = mapper;
    }


    public Optional<Page<Disco>> get(int page, int size, Sort sort, String nombre, String artista) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        // Parámetros de búsqueda
        Disco disco = new Disco();

        if ( nombre != null && !nombre.isEmpty() ){
            disco.setNombre(nombre);
        }
        if ( artista != null && !artista.isEmpty() ){
            disco.setArtista(artista);
        }

        // Búsqueda y resultados
        Page<Disco> result = disks.findAll( Example.of(disco, matcher), request);
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    public Optional<Disco> get(String id) {
        // Búsqueda y resultado
        return disks.findById(id);
    }

    public Optional<Disco> post(Disco disk) {
        // Búsqueda y resultados
        return Optional.of(disks.insert(disk));
    }

    public HttpStatus delete(String id)  {
        if(disks.findById(id).equals(Optional.empty())){
            return HttpStatus.NOT_FOUND;
        }
        disks.deleteById(id);
        return HttpStatus.NO_CONTENT;
    }

    public Optional<Disco> patch(String id, List<Map<String, Object>> updates) throws JsonPatchException {

        return disks.findById(id)
                .map(patchdisk -> {
                    PatchUtils pu = new PatchUtils(new ObjectMapper());
                    try {
                        patchdisk = pu.patchUtil(patchdisk, updates);
                    } catch (JsonPatchException e) {
                        throw new RuntimeException(e);
                    }
                    return disks.save(patchdisk);
                });
    }

}
