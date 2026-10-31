package pe.puriy.rutas.dominio.excepcion;

import java.time.Instant;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class SalidaEnElPasadoException extends ReglaDeNegocioException {

    public SalidaEnElPasadoException(Instant parteEn) {
        super(
                "salida-en-el-pasado",
                "No se puede programar una salida para el %s: esa hora ya pasó"
                        .formatted(FormatoDeFecha.enPeru(parteEn)));
    }
}
