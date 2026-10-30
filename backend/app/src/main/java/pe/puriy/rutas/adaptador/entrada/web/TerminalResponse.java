package pe.puriy.rutas.adaptador.entrada.web;

import java.util.UUID;

record TerminalResponse(
        UUID id, String nombre, String ciudad, String direccion, boolean vende, boolean embarca, long version) {}
