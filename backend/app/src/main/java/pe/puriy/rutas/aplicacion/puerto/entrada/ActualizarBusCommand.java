package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.DatosBus;

public record ActualizarBusCommand(BusId id, long version, DatosBus datos) {}
