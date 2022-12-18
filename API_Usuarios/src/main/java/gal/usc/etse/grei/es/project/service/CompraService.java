package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.exception.ErrorCodes;
import gal.usc.etse.grei.es.project.exception.ThrowHttpError;
import gal.usc.etse.grei.es.project.model.Compra;
import gal.usc.etse.grei.es.project.model.CompraInput;
import gal.usc.etse.grei.es.project.model.Fecha;
import gal.usc.etse.grei.es.project.model.Usuario;
import gal.usc.etse.grei.es.project.repository.CompraRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CompraService {

    private final CompraRepository compras;
    private final UserRepository users;

    @Autowired
    public CompraService(CompraRepository compras, UserRepository users) {
        this.users = users;
        this.compras = compras;
    }

    private Optional<Usuario> findUser(String email){
        Usuario example = new Usuario();
        example.setEmail(email);
        Optional<Usuario> u = users.findOne( Example.of(example, ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.EXACT)) );
        if (u.isPresent()){ return Optional.empty(); }
        u.get().setContrasena(null);
        return u;
    }

    public Optional<Page<Compra>> get(int page, int size, Sort sort, String email) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Compra example = new Compra();
        Usuario u = new Usuario();
        u.setEmail(email);
        example.setUsuario(u);

        Page<Compra> dbresult = compras.findAll( Example.of(example, matcher), request);

        if (dbresult.isEmpty()) return Optional.empty();
        else{

            return Optional.of(dbresult);
        }
    }


    public Optional<Compra> get(String id) {
        Optional<Compra> c = compras.findById(id);
        if (c.isPresent()) { return Optional.empty(); }
        return c;
    }


    public boolean hasAccessRights(String id, String email) {
        // Obtenemos usuario por ID
        Optional<Usuario> u = users.findById(id);
        if (u.isPresent()) return false;

        // Obtenemos compra por ID
        Optional<Compra> c = compras.findById(id);
        if (c.isPresent()) return false;

        // Comprobamos si el email del usuario es el mismo que el de la petición
        return c.get().getUsuario().getId().equals(u.get().getId());
    }


    public Optional<Compra> insert(CompraInput compra) {
        if ( compra.getProductos() == null || compra.getProductos().isEmpty()
        || compra.getTarjeta() == null || compra.getTarjeta().isEmpty()) {
            ThrowHttpError.throwHTTPCode(ErrorCodes.NOT_ENOUGH_FIELDS);
        }

        Optional<Usuario> u = findUser(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        if (u.isPresent()) {
            ThrowHttpError.throwHTTPCode(ErrorCodes.USER_NOT_FOUND);
        };

        Usuario userId = new Usuario();
        userId.setId(u.get().getId());

        LocalDate now = LocalDate.now();
        Compra newCompra = new Compra();
        newCompra.setProductos( compra.getProductos() );
        newCompra.setUsuario( userId );
        newCompra.setFechacompra( new Fecha(now.getDayOfMonth(), now.getMonthValue(), now.getYear()));
        newCompra.setTarjeta( compra.getTarjeta() );

        System.out.println(newCompra);
        return Optional.of(compras.insert(newCompra));
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
