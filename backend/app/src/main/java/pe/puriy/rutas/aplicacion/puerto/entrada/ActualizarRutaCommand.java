package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.rutas.dominio.modelo.DatosRuta;

public record ActualizarRutaCommand(RutaId id, long version, DatosRuta datos) {}
