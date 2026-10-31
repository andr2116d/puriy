package pe.puriy.rutas.aplicacion.servicio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.RutasDeEjemplo;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

class EscenarioDeSalidas {

    static final Instant AHORA = Instant.parse("2026-11-01T15:00:00Z");
    static final Instant MANIANA_A_LAS_NUEVE = Instant.parse("2026-11-03T02:00:00Z");
    static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    final SalidaRepositoryEnMemoria salidas = new SalidaRepositoryEnMemoria();
    final RutaRepositoryEnMemoria rutas = new RutaRepositoryEnMemoria();
    final BusRepositoryEnMemoria buses = new BusRepositoryEnMemoria();

    final Ruta limaTrujillo = registrar(RutasDeEjemplo.limaTrujilloDeOchoHoras());
    final Bus bus = registrar(Bus.registrar(BusesDeEjemplo.dosPisos()));
    final Bus otroBus = registrar(Bus.registrar(BusesDeEjemplo.unPiso()));

    Salida programada(Bus asignado, Instant parteEn) {
        var salida = Salida.programar(limaTrujillo, asignado.id(), TipoServicio.CAMA, parteEn, AHORA);
        salidas.agregar(salida);
        return salida;
    }

    static Clock relojEn(Instant instante) {
        return Clock.fixed(instante, ZoneOffset.UTC);
    }

    static Instant despuesDeLlegar(Salida salida) {
        return salida.horario().llegaEn().plus(Duration.ofMinutes(1));
    }

    private Ruta registrar(Ruta ruta) {
        rutas.agregar(ruta);
        return ruta;
    }

    private Bus registrar(Bus nuevo) {
        buses.agregar(nuevo);
        return nuevo;
    }
}
