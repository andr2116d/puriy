package pe.puriy.tarifas.adaptador.entrada.web;

import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

record TarifaResponse(
        UUID id,
        UUID rutaId,
        UUID origenId,
        UUID destinoId,
        String tipoServicio,
        int piso,
        String tipoVigencia,
        LocalDate vigenteDesde,
        @Nullable LocalDate vigenteHasta,
        long precioCentimos,
        long version) {}
