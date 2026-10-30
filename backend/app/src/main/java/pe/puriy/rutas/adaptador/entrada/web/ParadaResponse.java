package pe.puriy.rutas.adaptador.entrada.web;

import java.util.UUID;

record ParadaResponse(
        int orden,
        UUID terminalId,
        String terminal,
        String ciudad,
        long minutosDesdeAnterior,
        long minutosDesdeOrigen) {}
