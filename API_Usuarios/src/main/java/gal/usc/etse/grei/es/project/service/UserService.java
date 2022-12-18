package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import gal.usc.etse.grei.es.project.exception.ErrorCodes;
import gal.usc.etse.grei.es.project.exception.ThrowHttpError;
import gal.usc.etse.grei.es.project.model.Usuario;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private Optional<Usuario> findUser(String id ){
        return users.findById(id);
    }

    public Optional<Page<Usuario>> get(int page, int size, Sort sort, String name, String email) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Usuario example = new Usuario();

        if ( name != null && name.length() > 0 ) {
            example.setNombre(name);
        }
        if ( email != null && email.length() > 0 ) {
            example.setEmail(email);
        }

        Page<Usuario> dbresult = users.findAll( Example.of(example, matcher), request);

        if (dbresult.isEmpty()) return Optional.empty();
        else{
            for ( Usuario u : dbresult ){
                u.setContrasena(null);
            }
            return Optional.of(dbresult);
        }
    }


    public Optional<Usuario> get(String id) {
        Optional<Usuario> u = users.findById(id);
        if (!u.isPresent()) { return Optional.empty(); }
        u.get().setContrasena(null);
        return u;
    }


    public Optional<Usuario> getbyEmail(String email) {
        Usuario example = new Usuario();
        example.setEmail(email);
        Optional<Usuario> u = users.findOne( Example.of(example, ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.EXACT)) );
        if (!u.isPresent()){ return Optional.empty(); }
        u.get().setContrasena(null);
        return u;
    }


    public boolean hasAccessRights(String id, String email) {
        // Obtenemos usuario por ID
        Optional<Usuario> u = users.findById(id);
        if (!u.isPresent()) return false;

        // Comprobamos si el email del usuario es el mismo que el de la petición
        return u.get().getEmail().equals(email);
    }


    public Optional<Usuario> patch(String id, List<Map<String, Object>> operaciones){
        // Buscamos el usuario a editar
        Usuario u;
        Optional<Usuario> optU = findUser(id);
        if (!optU.isPresent()){ ThrowHttpError.throwHTTPCode(ErrorCodes.CONTENT_NOT_FOUND); }
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


    public Optional<Usuario> insert(Usuario usuario) {
        // Verificamos que existan todos los campos
        if ( usuario.getEmail() == null || usuario.getEmail().isEmpty()
                || usuario.getNombre() == null || usuario.getNombre().isEmpty()
                || usuario.getContrasena() == null || usuario.getContrasena().isEmpty()
                || usuario.getNacimiento() == null || usuario.getNacimiento().getAno() == null
                || usuario.getNacimiento().getDia() == null || usuario.getNacimiento().getMes() == null) {
            ThrowHttpError.throwHTTPCode(ErrorCodes.NOT_ENOUGH_FIELDS);
        }

        // Evitamos repetir ID y email
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues();
        Usuario example = new Usuario();
        example.setEmail(usuario.getEmail());

        if ( users.exists(Example.of(example, matcher)) ) {
            return Optional.empty();
        }

        usuario.setId(UUID.randomUUID().toString());
        usuario.setContrasena(encoder.encode(usuario.getContrasena()));

        // Para este ejemplo de aplicación permitimos la creación de usuarios como ADMIN, aunque no tiene sentido
        if ( usuario.getRoles() == null || usuario.getRoles().isEmpty() ){
            ArrayList<String> roleList = new ArrayList<>();
            roleList.add("ROLE_USER");
            usuario.setRoles( roleList );
        }

        Usuario inserted = users.insert(usuario);
        inserted.setContrasena(null);

        return Optional.of(inserted);
    }


    public boolean delete(String id) {
        Optional<Usuario> user = users.findById(id);

        if ( user.isPresent() ) {
            users.deleteById(id);
            return true;
        }
        return false;
    }

}
