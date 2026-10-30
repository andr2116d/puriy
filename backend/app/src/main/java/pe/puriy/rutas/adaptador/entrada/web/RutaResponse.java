package pe.puriy.rutas.adaptador.entrada.web;

import java.util.List;
import java.util.UUID;

record RutaResponse(UUID id, String nombre, long version, long duracionTotalMinutos, List<ParadaResponse> paradas) {}
