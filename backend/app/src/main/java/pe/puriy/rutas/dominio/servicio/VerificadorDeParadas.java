package pe.puriy.rutas.dominio.servicio;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.excepcion.ParadaSinEmbarqueException;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Terminal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VerificadorDeParadas {

    public static void exigirTerminalesDeEmbarque(DatosRuta datos, Map<TerminalId, Terminal> terminales) {
        for (var terminalId : datos.terminales()) {
            var terminal = terminales.get(terminalId);
            if (terminal == null) {
                throw new TerminalNoEncontradoException(terminalId);
            }
            if (!terminal.datos().embarca()) {
                throw new ParadaSinEmbarqueException(terminal.datos().nombre());
            }
        }
    }
}
