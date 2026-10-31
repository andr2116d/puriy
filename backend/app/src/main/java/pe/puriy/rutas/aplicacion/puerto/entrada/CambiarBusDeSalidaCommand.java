package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.MotivoCambioDeBus;

public record CambiarBusDeSalidaCommand(SalidaId salidaId, long version, BusId busNuevo, MotivoCambioDeBus motivo) {}
