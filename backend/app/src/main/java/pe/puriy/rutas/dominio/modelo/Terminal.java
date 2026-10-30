package pe.puriy.rutas.dominio.modelo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Terminal {

    private static final long VERSION_INICIAL = 0;

    @EqualsAndHashCode.Include
    @ToString.Include
    private final TerminalId id;

    @ToString.Include
    private DatosTerminal datos;

    private final long version;

    @Builder(builderMethodName = "reconstituir")
    private Terminal(TerminalId id, DatosTerminal datos, long version) {
        this.id = id;
        this.datos = datos;
        this.version = version;
    }

    public static Terminal registrar(DatosTerminal datos) {
        return new Terminal(TerminalId.nuevo(), datos, VERSION_INICIAL);
    }

    public void actualizar(DatosTerminal nuevosDatos) {
        datos = nuevosDatos;
    }
}
