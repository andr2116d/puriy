package pe.puriy.rutas.dominio.modelo;

import java.time.Instant;

public record CambioDeBus(
        SalidaId salidaId, BusId busAnterior, BusId busNuevo, MotivoCambioDeBus motivo, Instant cambiadoEn) {}
