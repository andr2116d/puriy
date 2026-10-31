package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

record ProgramarSalidaRequest(
        @NotNull UUID rutaId,
        @NotNull UUID busId,
        @NotNull TipoServicio tipoServicio,
        @NotNull OffsetDateTime parteEn) {}
