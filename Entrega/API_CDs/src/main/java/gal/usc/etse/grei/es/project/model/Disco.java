package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "discos")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Disco {

    @Id
    private String id;

    @Schema(example = "Pepe Perez")
    private String artista;

    @Schema(example = "Las largas")
    private String nombre;


    private Date fechasalida;

    @Schema(example = "Pop")
    private String genero;

    private Float precio;

    private Integer existencias;

    private Integer ventas;

    private Integer numerocanciones;

    public Disco() {}

    public Disco(String id, String artista, String nombre, Date fechasalida, String genero, Float precio, Integer existencias, Integer ventas, Integer numerocanciones) {
        this.id = id;
        this.artista = artista;
        this.nombre = nombre;
        this.fechasalida = fechasalida;
        this.genero = genero;
        this.precio = precio;
        this.existencias = existencias;
        this.ventas = ventas;
        this.numerocanciones = numerocanciones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Disco)) return false;
        Disco disco = (Disco) o;
        return id.equals(disco.id) && artista.equals(disco.artista) && nombre.equals(disco.nombre) && Objects.equals(fechasalida, disco.fechasalida) && Objects.equals(genero, disco.genero) && Objects.equals(precio, disco.precio) && Objects.equals(existencias, disco.existencias) && Objects.equals(ventas, disco.ventas) && Objects.equals(numerocanciones, disco.numerocanciones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, artista, nombre, fechasalida, genero, precio, existencias, ventas, numerocanciones);
    }

    public String getId() {
        return id;
    }

    public String getArtista() {
        return artista;
    }

    public Disco setArtista(String artista) {
        this.artista = artista;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public Disco setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public Date getFechasalida() {
        return fechasalida;
    }

    public Disco setFechasalida(Date fechasalida) {
        this.fechasalida = fechasalida;
        return this;
    }

    public String getGenero() {
        return genero;
    }

    public Disco setGenero(String genero) {
        this.genero = genero;
        return this;
    }

    public Float getPrecio() {
        return precio;
    }

    public Disco setPrecio(Float precio) {
        this.precio = precio;
        return this;
    }

    public Integer getExistencias() {
        return existencias;
    }

    public Disco setExistencias(Integer existencias) {
        this.existencias = existencias;
        return this;
    }

    public Integer getVentas() {
        return ventas;
    }

    public Disco setVentas(Integer ventas) {
        this.ventas = ventas;
        return this;
    }

    public Integer getNumerocanciones() {
        return numerocanciones;
    }

    public Disco setNumerocanciones(Integer numerocanciones) {
        this.numerocanciones = numerocanciones;
        return this;
    }

    @Override
    public String toString() {
        return "Disco{" +
                "id='" + id + '\'' +
                ", artista='" + artista + '\'' +
                ", nombre='" + nombre + '\'' +
                ", fechasalida=" + fechasalida +
                ", genero='" + genero + '\'' +
                ", precio=" + precio +
                ", existencias=" + existencias +
                ", ventas=" + ventas +
                ", numerocanciones=" + numerocanciones +
                '}';
    }
}
