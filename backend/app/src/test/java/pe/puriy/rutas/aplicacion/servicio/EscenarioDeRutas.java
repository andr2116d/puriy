package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

class EscenarioDeRutas {

    final TerminalRepositoryEnMemoria terminales = new TerminalRepositoryEnMemoria();
    final RutaRepositoryEnMemoria rutas = new RutaRepositoryEnMemoria();
    final CargadorDeTerminales cargadorDeTerminales = new CargadorDeTerminales(terminales);

    final Terminal lima = registrar(TerminalesDeEjemplo.plazaNorte());
    final Terminal chimbote = registrar(TerminalesDeEjemplo.paraderoChimbote());
    final Terminal trujillo = registrar(TerminalesDeEjemplo.trujillo());
    final Terminal oficinaDeVenta =
            registrar(new DatosTerminal("Oficina Miraflores", "Lima", "Av. Larco 500", true, false));

    DatosRuta limaTrujillo() {
        return new DatosRuta(
                "Lima – Trujillo",
                List.of(
                        Parada.origen(lima.id()),
                        Parada.aMinutos(chimbote.id(), 390),
                        Parada.aMinutos(trujillo.id(), 120)));
    }

    DatosRuta limaTrujilloDirecto() {
        return new DatosRuta(
                "Lima – Trujillo directo", List.of(Parada.origen(lima.id()), Parada.aMinutos(trujillo.id(), 480)));
    }

    private Terminal registrar(DatosTerminal datos) {
        var terminal = Terminal.registrar(datos);
        terminales.agregar(terminal);
        return terminal;
    }
}
