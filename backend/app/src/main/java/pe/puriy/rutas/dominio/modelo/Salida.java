package pe.puriy.rutas.dominio.modelo;

import java.time.Instant;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pe.puriy.rutas.dominio.excepcion.CambioDeBusInvalidoException;
import pe.puriy.rutas.dominio.excepcion.SalidaEnElPasadoException;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Salida {

    private static final long VERSION_INICIAL = 0;

    @EqualsAndHashCode.Include
    @ToString.Include
    private final SalidaId id;

    private final RutaId rutaId;

    @ToString.Include
    private BusId busId;

    private final TipoServicio tipoServicio;

    @ToString.Include
    private final Horario horario;

    private final long version;

    @Builder(builderMethodName = "reconstituir")
    private Salida(SalidaId id, RutaId rutaId, BusId busId, TipoServicio tipoServicio, Horario horario, long version) {
        this.id = id;
        this.rutaId = rutaId;
        this.busId = busId;
        this.tipoServicio = tipoServicio;
        this.horario = horario;
        this.version = version;
    }

    public static Salida programar(Ruta ruta, BusId busId, TipoServicio tipoServicio, Instant parteEn, Instant ahora) {
        if (!parteEn.isAfter(ahora)) {
            throw new SalidaEnElPasadoException(parteEn);
        }
        var horario = Horario.desde(parteEn, ruta.duracionTotal());
        return new Salida(SalidaId.nuevo(), ruta.id(), busId, tipoServicio, horario, VERSION_INICIAL);
    }

    public CambioDeBus cambiarBus(BusId nuevoBus, MotivoCambioDeBus motivo, Instant ahora) {
        if (estaTerminada(ahora)) {
            throw CambioDeBusInvalidoException.salidaTerminada(id);
        }
        if (nuevoBus.equals(busId)) {
            throw CambioDeBusInvalidoException.mismoBus(busId);
        }
        var cambio = new CambioDeBus(id, busId, nuevoBus, motivo, ahora);
        busId = nuevoBus;
        return cambio;
    }

    public boolean estaTerminada(Instant ahora) {
        return horario.estaTerminado(ahora);
    }
}
