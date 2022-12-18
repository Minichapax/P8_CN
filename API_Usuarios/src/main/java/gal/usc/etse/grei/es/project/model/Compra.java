package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Document(collection = "compras")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Compra {

    @Id
    private String id;

    private Fecha fechacompra;

    private Usuario usuario;

    private Map<String ,Integer> productos;

    private String tarjeta;

    public Compra() {
    }

    public Compra(String id, Fecha fechacompra, Usuario usuario, Map<String ,Integer> productos, String tarjeta) {
        this.id = id;
        this.fechacompra = fechacompra;
        this.usuario = usuario;
        this.productos = productos;
        this.tarjeta = tarjeta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Fecha getFechacompra() {
        return fechacompra;
    }

    public void setFechacompra(Fecha fechacompra) {
        this.fechacompra = fechacompra;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Map<String ,Integer> getProductos() {
        return productos;
    }

    public void setProductos(Map<String ,Integer> productos) {
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
        if (!(o instanceof Compra)) return false;
        Compra compra = (Compra) o;
        return id.equals(compra.id) && fechacompra.equals(compra.fechacompra) && usuario.equals(compra.usuario) && productos.equals(compra.productos) && tarjeta.equals(compra.tarjeta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fechacompra, usuario, productos, tarjeta);
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id='" + id + '\'' +
                ", fechacompra=" + fechacompra +
                ", usuario=" + usuario +
                ", productos=" + productos +
                ", tarjeta='" + tarjeta + '\'' +
                '}';
    }
}
