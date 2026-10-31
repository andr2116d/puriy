package pe.puriy.tarifas.dominio.modelo;

import java.util.List;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.tarifas.dominio.excepcion.TramoFueraDeRutaException;

public record ParadasDeRuta(RutaId rutaId, List<TerminalId> terminales) {

    public ParadasDeRuta {
        terminales = List.copyOf(terminales);
    }

    public void exigirTramo(ParDeParadas par) {
        if (!par.rutaId().equals(rutaId)) {
            throw new IllegalArgumentException("el par %s no es de la ruta %s".formatted(par, rutaId));
        }
        var origen = terminales.indexOf(par.origen());
        var destino = terminales.indexOf(par.destino());
        if (origen < 0 || destino <= origen) {
            throw new TramoFueraDeRutaException(par);
        }
    }
}
