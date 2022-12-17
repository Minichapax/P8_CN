package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gal.usc.etse.grei.es.project.model.Compra;
import gal.usc.etse.grei.es.project.model.CompraInput;
import gal.usc.etse.grei.es.project.model.Usuario;
import gal.usc.etse.grei.es.project.repository.CompraRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompraService {

    private final PasswordEncoder encoder;
    private final CompraRepository compras;
    private final UserRepository users;
    private final PatchUtils patchUtils;
    private final ObjectMapper mapper;

    @Autowired
    public CompraService(CompraRepository compras, UserRepository users, PatchUtils pu, ObjectMapper mapper, PasswordEncoder encoder) {
        this.encoder = encoder;
        this.users = users;
        this.compras = compras;
        this.patchUtils = pu;
        this.mapper = mapper;
    }

    private Optional<Compra> findUser(String id ){
        return compras.findById(id);
    }

    public Optional<Page<Compra>> get(int page, int size, Sort sort, String email) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Compra example = new Compra();

        Page<Compra> dbresult = compras.findAll( Example.of(example, matcher), request);

        if (dbresult.isEmpty()) return Optional.empty();
        else{

            return Optional.of(dbresult);
        }
    }


    public Optional<Compra> get(String id) {
        Optional<Compra> c = compras.findById(id);
        if (c.isEmpty()) { return Optional.empty(); }
        return c;
    }


    public boolean hasAccessRights(String id, String email) {
        // Obtenemos usuario por ID
        Optional<Usuario> u = users.findById(id);
        if (u.isEmpty()) return false;

        // Obtenemos compra por ID
        Optional<Compra> c = compras.findById(id);
        if (c.isEmpty()) return false;

        // Comprobamos si el email del usuario es el mismo que el de la petición
        return c.get().getUsuario().getId().equals(u.get().getId());
    }


    public Optional<Compra> insert(CompraInput user) {
        // Verificamos que existan todos los campos
        // TODO: Insertar nuevas compras

        /* Evitamos repetir ID y email
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues();
        User example = new User();
        example.setEmail(user.getEmail());

        if ( users.exists(Example.of(example, matcher)) ) {
            return Optional.empty();
        }

        user.setId(UUID.randomUUID().toString());
        user.setContrasena(encoder.encode(user.getContrasena()));

        // Para este ejemplo de aplicación permitimos la creación de usuarios como ADMIN, aunque no tiene sentido
        if ( user.getRoles() == null || user.getRoles().isEmpty() ){
            ArrayList<String> roleList = new ArrayList<>();
            roleList.add("ROLE_USER");
            user.setRoles( roleList );
        }

        User inserted = users.insert(user);
        inserted.setContrasena(null);*/

        return Optional.empty();
    }


    public boolean delete(String id) {
        Optional<Compra> compra = compras.findById(id);

        if ( compra.isPresent() ) {
            compras.deleteById(id);
            return true;
        }
        return false;
    }

}
