package pe.puriy.rutas.dominio.modelo;

import java.time.Instant;
import pe.puriy.compartido.dominio.SalidaId;

public record CambioDeBus(
        SalidaId salidaId, BusId busAnterior, BusId busNuevo, MotivoCambioDeBus motivo, Instant cambiadoEn) {}
