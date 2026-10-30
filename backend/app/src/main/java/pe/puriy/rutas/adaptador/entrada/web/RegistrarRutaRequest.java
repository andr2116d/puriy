package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import pe.puriy.rutas.dominio.modelo.DatosRuta;

record RegistrarRutaRequest(
        @NotBlank @Size(max = DatosRuta.MAXIMO_NOMBRE) String nombre,
        @NotNull List<@NotNull @Valid ParadaRequest> paradas) {}
