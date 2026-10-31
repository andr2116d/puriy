package pe.puriy.reservas;

import static org.assertj.core.api.Assertions.assertThat;
import static puriy.basedatos.reservas.Tables.ASIGNACIONES_ASIENTO;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosUseCase;
import pe.puriy.reservas.dominio.excepcion.AsientoNoDisponibleException;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.Titular;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class RetencionConcurrenteIT {

    private static final int COMPRADORES = 500;
    private static final int ASIENTO = 3;

    private final MockMvcTester mvc;
    private final RetenerAsientosUseCase retenerAsientos;
    private final DSLContext dsl;
    private CatalogoDePrueba catalogo;

    RetencionConcurrenteIT(
            @Autowired MockMvcTester mvc,
            @Autowired RetenerAsientosUseCase retenerAsientos,
            @Autowired DSLContext dsl) {
        this.mvc = mvc;
        this.retenerAsientos = retenerAsientos;
        this.dsl = dsl;
    }

    @BeforeEach
    void armarCatalogo() {
        catalogo = new CatalogoDePrueba(mvc);
    }

    @Test
    void quinientos_compradores_por_el_mismo_asiento_solo_uno_lo_obtiene() throws Exception {
        var intentos = IntStream.range(0, COMPRADORES)
                .mapToObj(_ -> comando(catalogo.lima, catalogo.trujillo))
                .toList();

        var exitosos = retenerALaVez(intentos);

        assertThat(exitosos).isEqualTo(1);
        assertThat(asignacionesActivas()).isEqualTo(1);
    }

    @Test
    void en_tramos_consecutivos_el_mismo_asiento_se_vende_una_vez_en_cada_uno() throws Exception {
        var intentos = IntStream.range(0, COMPRADORES)
                .mapToObj(numero -> numero % 2 == 0
                        ? comando(catalogo.lima, catalogo.chimbote)
                        : comando(catalogo.chimbote, catalogo.trujillo))
                .toList();

        var exitosos = retenerALaVez(intentos);

        assertThat(exitosos).isEqualTo(2);
        assertThat(asignacionesActivas()).isEqualTo(2);
    }

    private int retenerALaVez(List<RetenerAsientosCommand> intentos) throws InterruptedException, ExecutionException {
        try (var hilos = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> resultados = intentos.stream()
                    .map(comando -> hilos.submit(() -> intentarRetener(comando)))
                    .toList();
            var exitosos = 0;
            for (var resultado : resultados) {
                if (resultado.get()) {
                    exitosos++;
                }
            }
            return exitosos;
        }
    }

    private boolean intentarRetener(RetenerAsientosCommand comando) {
        try {
            retenerAsientos.ejecutar(comando);
            return true;
        } catch (AsientoNoDisponibleException excepcion) {
            return false;
        }
    }

    private int asignacionesActivas() {
        return dsl.fetchCount(
                ASIGNACIONES_ASIENTO,
                ASIGNACIONES_ASIENTO
                        .SALIDA_ID
                        .eq(UUID.fromString(catalogo.salida))
                        .and(ASIGNACIONES_ASIENTO.NUMERO_ASIENTO.eq((short) ASIENTO))
                        .and(ASIGNACIONES_ASIENTO.ACTIVA));
    }

    private RetenerAsientosCommand comando(String origen, String destino) {
        return RetenerAsientosCommand.builder()
                .claveIdempotencia(UUID.randomUUID())
                .salidaId(new SalidaId(UUID.fromString(catalogo.salida)))
                .origen(new TerminalId(UUID.fromString(origen)))
                .destino(new TerminalId(UUID.fromString(destino)))
                .asientos(List.of(ASIENTO))
                .canal(Canal.INVITADO)
                .titular(new Titular(null, null, UUID.randomUUID()))
                .build();
    }
}
