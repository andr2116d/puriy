package pe.puriy.rutas.dominio.modelo;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Ruta {

    private static final long VERSION_INICIAL = 0;

    @EqualsAndHashCode.Include
    @ToString.Include
    private final RutaId id;

    @ToString.Include
    private DatosRuta datos;

    private final long version;

    @Builder(builderMethodName = "reconstituir")
    private Ruta(RutaId id, DatosRuta datos, long version) {
        this.id = id;
        this.datos = datos;
        this.version = version;
    }

    public static Ruta trazar(DatosRuta datos) {
        return new Ruta(RutaId.nuevo(), datos, VERSION_INICIAL);
    }

    public void actualizar(DatosRuta nuevosDatos) {
        datos = nuevosDatos;
    }

    public List<Parada> paradas() {
        return datos.paradas();
    }

    public Duration desfaseHasta(int orden) {
        exigirOrdenExistente(orden);
        return paradas().subList(0, orden + 1).stream()
                .map(Parada::desdeAnterior)
                .reduce(Duration.ZERO, Duration::plus);
    }

    public Instant horaDePaso(Instant partida, int orden) {
        return partida.plus(desfaseHasta(orden));
    }

    public Duration duracionTotal() {
        return desfaseHasta(paradas().size() - 1);
    }

    private void exigirOrdenExistente(int orden) {
        if (orden < 0 || orden >= paradas().size()) {
            throw new IllegalArgumentException(
                    "la ruta %s no tiene una parada con orden %d".formatted(datos.nombre(), orden));
        }
    }
}
