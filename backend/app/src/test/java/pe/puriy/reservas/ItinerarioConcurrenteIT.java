package pe.puriy.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand.TramoDelItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioUseCase;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.Titular;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class ItinerarioConcurrenteIT {

    private static final int ASIENTOS = 40;

    private final MockMvcTester mvc;
    private final RetenerItinerarioUseCase retenerItinerario;

    ItinerarioConcurrenteIT(@Autowired MockMvcTester mvc, @Autowired RetenerItinerarioUseCase retenerItinerario) {
        this.mvc = mvc;
        this.retenerItinerario = retenerItinerario;
    }

    /*
     * Las dos salidas recorren los terminales en sentido cruzado: un itinerario usa primero la
     * salida 1 y otro primero la salida 2. Sus tramos no se cruzan, así que ambos caben, pero piden
     * los mismos asientos en orden inverso: sin un orden común, se traban hasta que PostgreSQL
     * aborta a uno.
     */
    @Test
    void dos_itinerarios_que_usan_las_mismas_salidas_en_orden_inverso_no_se_traban() throws Exception {
        var catalogo = new CatalogoDePrueba(mvc);
        var dia = catalogo.fechaDeSalida.plusDays(3);
        var a = catalogo.terminalEn(ciudad("A"));
        var b = catalogo.terminalEn(ciudad("B"));
        var r = catalogo.terminalEn(ciudad("R"));
        var s = catalogo.terminalEn(ciudad("S"));
        var q = catalogo.terminalEn(ciudad("Q"));
        var c = catalogo.terminalEn(ciudad("C"));
        var salida1 = catalogo.programar(hora(dia, "08:00"), List.of(a, b, r, s), List.of(0, 120, 240, 120));
        var salida2 = catalogo.programar(hora(dia, "09:00"), List.of(q, r, b, c), List.of(0, 90, 60, 120));

        var intentos = new ArrayList<RetenerItinerarioCommand>();
        for (var asiento : IntStream.rangeClosed(1, ASIENTOS).boxed().toList()) {
            intentos.add(comando(tramo(salida1, a, b, asiento), tramo(salida2, b, c, asiento)));
            intentos.add(comando(tramo(salida2, q, r, asiento), tramo(salida1, r, s, asiento)));
        }

        var retenidos = retenerALaVez(intentos);

        assertThat(retenidos).hasSize(ASIENTOS * 2);
    }

    private List<DetalleItinerario> retenerALaVez(List<RetenerItinerarioCommand> intentos) throws Exception {
        try (var hilos = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<DetalleItinerario>> resultados = intentos.stream()
                    .map(comando -> hilos.submit(() -> retenerItinerario.ejecutar(comando)))
                    .toList();
            var retenidos = new ArrayList<DetalleItinerario>();
            for (var resultado : resultados) {
                retenidos.add(resultado.get());
            }
            return retenidos;
        }
    }

    private static RetenerItinerarioCommand comando(TramoDelItinerario primero, TramoDelItinerario segundo) {
        return RetenerItinerarioCommand.builder()
                .claveIdempotencia(UUID.randomUUID())
                .primerTramo(primero)
                .segundoTramo(segundo)
                .canal(Canal.AGENTE)
                .titular(Titular.anonimo())
                .build();
    }

    private static TramoDelItinerario tramo(String salida, String origen, String destino, int asiento) {
        return new TramoDelItinerario(
                new SalidaId(UUID.fromString(salida)),
                new TerminalId(UUID.fromString(origen)),
                new TerminalId(UUID.fromString(destino)),
                List.of(asiento));
    }

    private static String ciudad(String letra) {
        return letra + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String hora(LocalDate dia, String hora) {
        return dia + "T" + hora + ":00-05:00";
    }
}
