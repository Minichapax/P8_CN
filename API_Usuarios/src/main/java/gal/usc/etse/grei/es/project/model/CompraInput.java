package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompraInput {
    private Usuario usuario;
    private List<Producto> productos;
    private String tarjeta;

    public CompraInput() {
    }

    public CompraInput(Usuario usuario, List<Producto> productos, String tarjeta) {
        this.usuario = usuario;
        this.productos = productos;
        this.tarjeta = tarjeta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompraInput)) return false;
        CompraInput compra = (CompraInput) o;
        return usuario.equals(compra.usuario) && productos.equals(compra.productos) && tarjeta.equals(compra.tarjeta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, productos, tarjeta);
    }

    @Override
    public String toString() {
        return "CompraInput{" +
                "usuario=" + usuario +
                ", productos=" + productos +
                ", tarjeta='" + tarjeta + '\'' +
                '}';
    }
}
