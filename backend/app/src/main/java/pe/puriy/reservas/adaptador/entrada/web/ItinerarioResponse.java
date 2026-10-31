package pe.puriy.reservas.adaptador.entrada.web;

import java.util.UUID;

record ItinerarioResponse(UUID id, ReservaResponse primerTramo, ReservaResponse segundoTramo, long totalCentimos) {}
