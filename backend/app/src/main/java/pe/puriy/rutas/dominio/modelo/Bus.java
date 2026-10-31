package pe.puriy.rutas.dominio.modelo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Bus {

    private static final long VERSION_INICIAL = 0;

    @EqualsAndHashCode.Include
    @ToString.Include
    private final BusId id;

    @ToString.Include
    private DatosBus datos;

    private final long version;

    @Builder(builderMethodName = "reconstituir")
    private Bus(BusId id, DatosBus datos, long version) {
        this.id = id;
        this.datos = datos;
        this.version = version;
    }

    public static Bus registrar(DatosBus datos) {
        return new Bus(BusId.nuevo(), datos, VERSION_INICIAL);
    }

    public void actualizar(DatosBus nuevosDatos) {
        datos = nuevosDatos;
    }
}
