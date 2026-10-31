package pe.puriy.tarifas.dominio.modelo;

import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.tarifas.dominio.excepcion.TarifaInvalidaException;

public record ParDeParadas(RutaId rutaId, TerminalId origen, TerminalId destino) {

    public ParDeParadas {
        if (origen.equals(destino)) {
            throw TarifaInvalidaException.origenIgualADestino(origen);
        }
    }
}
