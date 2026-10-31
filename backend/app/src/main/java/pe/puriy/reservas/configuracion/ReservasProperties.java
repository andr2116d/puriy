package pe.puriy.reservas.configuracion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pe.puriy.reservas.dominio.modelo.Canal;

@Validated
@ConfigurationProperties("puriy.reservas")
public record ReservasProperties(
        @NotNull @Valid Liberacion liberacion, @NotEmpty Map<Canal, @NotNull @Valid Reglas> canales) {

    public record Liberacion(
            boolean habilitada,
            @NotNull Duration cada,
            @Positive int lote) {}

    public record Reglas(
            @NotNull Duration retencion,
            @Positive int maximoAsientos,
            @NotNull Duration cierreAntesDelPaso) {}
}
