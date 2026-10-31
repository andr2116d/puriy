package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.MotivoCambioDeBus;
import pe.puriy.rutas.dominio.modelo.SalidaId;

public record CambiarBusDeSalidaCommand(SalidaId salidaId, long version, BusId busNuevo, MotivoCambioDeBus motivo) {}
