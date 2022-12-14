package gal.usc.etse.grei.es.project.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "API REST red social de películas",
                description = "API do proxecto de Enxeñaría de Servizos",
                version = "1.0.0",
                contact = @Contact(
                        name = "Alberto Pérez",
                        url = "https://twitter.com/Mixwell",
                        email = "alberto.perez.hernandez@rai.usc.es"
                ),
                license = @License(
                        name = "MIT Licence",
                        url = "https://opensource.org/licenses/MIT")),
        servers = {
                @Server(url = "/", description = "General use server"),
        }
)
public class OpenAPIConfiguration {
}
