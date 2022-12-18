package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompraInput {
    private Map<String, Integer> productos;
    private String tarjeta;

    public CompraInput() {
    }

    public CompraInput(Map<String, Integer> productos, String tarjeta) {
        this.productos = productos;
        this.tarjeta = tarjeta;
    }

    public Map<String, Integer> getProductos() {
        return productos;
    }

    public void setProductos(Map<String, Integer> productos) {
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
        return productos.equals(compra.productos) && tarjeta.equals(compra.tarjeta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productos, tarjeta);
    }

    @Override
    public String toString() {
        return "CompraInput{" +
                "productos=" + productos +
                ", tarjeta='" + tarjeta + '\'' +
                '}';
    }
}
