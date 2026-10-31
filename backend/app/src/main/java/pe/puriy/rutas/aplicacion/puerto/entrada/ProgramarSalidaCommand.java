package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

public record ProgramarSalidaCommand(RutaId rutaId, BusId busId, TipoServicio tipoServicio, Instant parteEn) {}
