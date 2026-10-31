package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import pe.puriy.rutas.dominio.modelo.Placa;

record RegistrarBusRequest(
        @NotBlank @Size(max = Placa.LONGITUD) String placa,
        @NotNull List<@NotNull @Valid AsientoRequest> asientos) {}
