package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;


@Document(collection = "usuarios")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usuario {

    @Id
    @NotBlank(message = "La ID no puede estar vacía")
    @Schema(required = true, example = "e7c88484-a596-4df2-bc42-710a67c5c48b")
    private String id;

    @NotBlank(message = "El correo no puede estar vacío")
    @Email
    @Schema(required = true, example = "test@test.com")
    private String email;

    @Schema(example = "Ibai Llanos")
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private Fecha nacimiento;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String contrasena;

    private List<String> roles;

    public Usuario() {
    }

    public Usuario(String id, String email, String nombre, Fecha nacimiento, String contrasena, List<String> roles) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.nacimiento = nacimiento;
        this.contrasena = contrasena;
        this.roles = roles;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Fecha getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(Fecha cumpleanos) {
        this.nacimiento = cumpleanos;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id) && email.equals(usuario.email) && nombre.equals(usuario.nombre) && nacimiento.equals(usuario.nacimiento) && contrasena.equals(usuario.contrasena) && roles.equals(usuario.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, nombre, nacimiento, contrasena, roles);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                ", nacimiento=" + nacimiento +
                ", contrasena='" + contrasena + '\'' +
                ", roles=" + roles +
                '}';
    }
}
