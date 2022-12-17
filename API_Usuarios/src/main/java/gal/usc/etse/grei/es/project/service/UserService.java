package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import gal.usc.etse.grei.es.project.exception.ErrorCodes;
import gal.usc.etse.grei.es.project.exception.ThrowHttpError;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {

    private final PasswordEncoder encoder;
    private final UserRepository users;
    private final PatchUtils patchUtils;
    private final ObjectMapper mapper;

    @Autowired
    public UserService(UserRepository users, PatchUtils pu, ObjectMapper mapper, PasswordEncoder encoder) {
        this.encoder = encoder;
        this.users = users;
        this.patchUtils = pu;
        this.mapper = mapper;
    }

    private Optional<User> findUser( String id ){
        return users.findById(id);
    }

    public Optional<Page<User>> get(int page, int size, Sort sort, String name, String email) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        User example = new User();

        if ( name != null && name.length() > 0 ) {
            example.setNombre(name);
        }
        if ( email != null && email.length() > 0 ) {
            example.setEmail(email);
        }

        Page<User> dbresult = users.findAll( Example.of(example, matcher), request);

        if (dbresult.isEmpty()) return Optional.empty();
        else{
            for ( User u : dbresult ){
                u.setContrasena(null);
            }
            return Optional.of(dbresult);
        }
    }


    public Optional<User> get(String id) {
        Optional<User> u = users.findById(id);
        if (u.isEmpty()) { return Optional.empty(); }
        u.get().setContrasena(null);
        return u;
    }


    public Optional<User> getbyEmail(String email) {
        User example = new User();
        example.setEmail(email);
        Optional<User> u = users.findOne( Example.of(example, ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.EXACT)) );
        if (u.isEmpty()){ return Optional.empty(); }
        u.get().setContrasena(null);
        return u;
    }


    public boolean hasAccessRights(String id, String email) {
        // Obtenemos usuario por ID
        Optional<User> u = users.findById(id);
        if (u.isEmpty()) return false;

        // Comprobamos si el email del usuario es el mismo que el de la petición
        return u.get().getEmail().equals(email);
    }


    public Optional<User> patch(String id, List<Map<String, Object>> operaciones){
        // Buscamos el usuario a editar
        User u;
        Optional<User> optU = findUser(id);
        if (optU.isEmpty()){ ThrowHttpError.throwHTTPCode(ErrorCodes.CONTENT_NOT_FOUND); }
        u = optU.get();

        List<Map<String, Object>> accionesAmigos = new ArrayList<>();
        List<Map<String, Object>> accionesEliminadas = new ArrayList<>(operaciones.size());

        // Comprobamos que el formato es correcto
        try { mapper.convertValue(operaciones, JsonPatch.class); }
        catch (IllegalArgumentException e) {
            ThrowHttpError.throwHTTPCode(ErrorCodes.JSONPATCH_FORMAT);
        }

        Map<String, Object> passwordChange = null;
        // Leemos las operaciones a realizar y descartamos las que actúan en campos bloqueados O estén mal formateados
        for ( Map<String, Object> operacion : operaciones ){

            if ( operacion.get("path").equals("/contrasena") && operacion.get("op").equals("replace") ){
                accionesEliminadas.add(operacion);
                passwordChange = operacion;
            }

            // Descartamos silenciosamente la operación si actúa en un campo no válido. El nodo raíz se considera NO válido.
            else if (!operacion.get("path").equals("/nombre")
                    && !operacion.get("path").equals("/email")
            ){
                accionesEliminadas.add(operacion);
            }
        }

        operaciones.removeAll(accionesEliminadas);

        // Aplicamos el cambio de contraseña, si es pertinente
        if ( passwordChange != null) {
            u.setContrasena( encoder.encode( (String) passwordChange.get("value")) );
        }

        // Aplicamos los cambios sobre el resto de campos, empleando JSON patch
        try {
            u = patchUtils.patch(u, operaciones);
            users.save(u);
        } catch (Exception e) {
            ThrowHttpError.throwHTTPCode(ErrorCodes.UNPROCESSABLE);
        }

        return Optional.of(u);
    }


    public Optional<User> insert(User user) {
        // Verificamos que existan todos los campos
        if ( user.getEmail() == null || user.getEmail().isEmpty()
                || user.getNombre() == null || user.getNombre().isEmpty()
                || user.getContrasena() == null || user.getContrasena().isEmpty()
                || user.getNacimiento() == null || user.getNacimiento().getAno() == null
                || user.getNacimiento().getDia() == null || user.getNacimiento().getMes() == null) {
            ThrowHttpError.throwHTTPCode(ErrorCodes.NOT_ENOUGH_FIELDS);
        }

        // Evitamos repetir ID y email
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
        inserted.setContrasena(null);

        return Optional.of(inserted);
    }


    public boolean delete(String id) {
        Optional<User> user = users.findById(id);

        if ( user.isPresent() ) {
            users.deleteById(id);
            return true;
        }
        return false;
    }

}
