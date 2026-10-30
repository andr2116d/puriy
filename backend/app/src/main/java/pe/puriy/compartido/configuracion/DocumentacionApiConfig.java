package pe.puriy.compartido.configuracion;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class DocumentacionApiConfig {

    @Bean
    OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Puriy API")
                        .version("v1")
                        .description("Venta y gestión de pasajes de transporte interprovincial en bus"));
    }
}
