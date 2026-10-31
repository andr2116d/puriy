package pe.puriy.rutas.adaptador.entrada.web;

import java.util.List;
import java.util.UUID;

record BusResponse(
        UUID id, String placa, long version, int capacidad, int cantidadPisos, List<AsientoResponse> asientos) {}
