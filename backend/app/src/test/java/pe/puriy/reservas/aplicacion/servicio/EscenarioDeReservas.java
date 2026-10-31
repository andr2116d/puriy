package pe.puriy.reservas.aplicacion.servicio;

import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Titular;

class EscenarioDeReservas {

    final ReservaRepositoryEnMemoria reservas = new ReservaRepositoryEnMemoria();
    final SalidaPortEnMemoria salidas = new SalidaPortEnMemoria();
    final TarifaPortEnMemoria tarifas = new TarifaPortEnMemoria();
    final Salida salida = ReservasDeEjemplo.limaTumbes();

    EscenarioDeReservas() {
        salidas.agregar(salida);
        tarifas.definir(1, 12000);
        tarifas.definir(2, 9000);
    }

    RetenerAsientosService retencion(Instant ahora) {
        return new RetenerAsientosService(
                reservas, salidas, tarifas, ReservasDeEjemplo.POLITICA, Clock.fixed(ahora, ZoneOffset.UTC));
    }

    RetenerAsientosService retencion() {
        return retencion(AHORA);
    }

    RetenerAsientosCommand comando(TerminalId origen, TerminalId destino, Canal canal, Integer... asientos) {
        return RetenerAsientosCommand.builder()
                .claveIdempotencia(UUID.randomUUID())
                .salidaId(salida.id())
                .origen(origen)
                .destino(destino)
                .asientos(List.of(asientos))
                .canal(canal)
                .titular(new Titular(null, null, UUID.randomUUID()))
                .build();
    }

    static Clock relojEn(Instant instante) {
        return Clock.fixed(instante, ZoneOffset.UTC);
    }
}
