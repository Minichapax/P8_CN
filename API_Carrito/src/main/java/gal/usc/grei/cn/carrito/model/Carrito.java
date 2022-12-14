package gal.usc.grei.cn.carrito.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carritos")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Carrito {

    @Id
    private String id;

    private HashMap<String, Integer> discos_cantidades;

    private Integer cookie_user;

    public Carrito(String identificador, String usuario){
        this.id=identificador;
        this.discos_cantidades= new HashMap<>();
        this.cookie_user= usuario.hashCode();
    }

    public Carrito(String identificador){
        this.id=identificador;
        this.discos_cantidades= new HashMap<>();
        //this.cookie_user= usuario.hashCode();
    }

    public Carrito(){}

    public String getId(){
        return this.id;
    }

    public HashMap<String, Integer> getdiscos_cantidades(){
        return this.discos_cantidades;
    }

    public Integer getCookie_user() {
        return cookie_user;
    }

    public void setCookie_user(int cookie_user) {
        this.cookie_user = cookie_user;
    }

    public void setCookie_user(String usuario) {
        this.cookie_user= usuario.hashCode();
    }


    public void addDisco(String id_disco){
        if(this.discos_cantidades.containsKey(id_disco)==false){
            this.discos_cantidades.put(id_disco, 1);
        }
        else{
            this.discos_cantidades.put(id_disco,  this.discos_cantidades.get(id_disco)+1);
        }
    }

    public void addDisco(String id_disco, int cantidade){
        this.discos_cantidades.put(id_disco, cantidade);
    }

    public void diminuirDisco(String id_disco){
        if(this.discos_cantidades.containsKey(id_disco)==true && this.discos_cantidades.get(id_disco)>0){
            this.discos_cantidades.put(id_disco,  this.discos_cantidades.get(id_disco)-1);
            this.discos_cantidades.remove(id_disco, 0);
        }
    }

    public void rmDisco(String id_disco){
        if(this.discos_cantidades.containsKey(id_disco)){
            this.discos_cantidades.remove(id_disco);
        }
    }

    @Override
    public String toString() {

        String resultado= "Carrito{" +
                "id='" + id + '\'' +
                ", discos='";

        for(String k : this.discos_cantidades.keySet()){
            resultado= resultado  + '\'' +  k+ '\'' + this.discos_cantidades.get(k).toString();
        }

        resultado= resultado + '\'' +
                ", cookie='" + this.cookie_user + '\'' +
                '}';

        return resultado;
    }

}