package pe.puriy.compartido.configuracion;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class CompartidoConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}
