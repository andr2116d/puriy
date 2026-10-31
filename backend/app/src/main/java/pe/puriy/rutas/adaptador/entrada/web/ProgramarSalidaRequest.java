package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;
import pe.puriy.compartido.dominio.TipoServicio;

record ProgramarSalidaRequest(
        @NotNull UUID rutaId,
        @NotNull UUID busId,
        @NotNull TipoServicio tipoServicio,
        @NotNull OffsetDateTime parteEn) {}
