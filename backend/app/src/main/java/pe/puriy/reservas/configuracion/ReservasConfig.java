package pe.puriy.reservas.configuracion;

import java.util.EnumMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import pe.puriy.reservas.adaptador.entrada.programado.LiberarReservasVencidasJob;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasUseCase;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.PoliticaDeConexion;
import pe.puriy.reservas.dominio.modelo.PoliticaReserva;
import pe.puriy.reservas.dominio.modelo.ReglasDeCanal;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(ReservasProperties.class)
class ReservasConfig {

    @Bean
    PoliticaReserva politicaReserva(ReservasProperties propiedades) {
        var reglas = new EnumMap<Canal, ReglasDeCanal>(Canal.class);
        propiedades
                .canales()
                .forEach((canal, deCanal) -> reglas.put(
                        canal,
                        new ReglasDeCanal(
                                deCanal.retencion(), deCanal.maximoAsientos(), deCanal.cierreAntesDelPaso())));
        return new PoliticaReserva(reglas);
    }

    @Bean
    PoliticaDeConexion politicaDeConexion(ReservasProperties propiedades) {
        var conexiones = propiedades.conexiones();
        return new PoliticaDeConexion(conexiones.escalaMinima(), conexiones.escalaMaxima());
    }

    @Bean
    @ConditionalOnProperty(name = "puriy.reservas.liberacion.habilitada", havingValue = "true")
    LiberarReservasVencidasJob liberarReservasVencidasJob(
            LiberarReservasVencidasUseCase liberarReservasVencidas, ReservasProperties propiedades) {
        return new LiberarReservasVencidasJob(
                liberarReservasVencidas,
                new LiberarReservasVencidasCommand(propiedades.liberacion().lote()));
    }
}
