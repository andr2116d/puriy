package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.RutaId;

public record ActualizarRutaCommand(RutaId id, long version, DatosRuta datos) {}
