package pe.puriy.reservas.adaptador.entrada.web;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import pe.puriy.reservas.dominio.modelo.Titular;

record TitularRequest(
        @Nullable UUID clienteId,
        @Nullable @Size(max = Titular.MAXIMO_CORREO) String correo,
        @Nullable UUID navegadorId) {}
