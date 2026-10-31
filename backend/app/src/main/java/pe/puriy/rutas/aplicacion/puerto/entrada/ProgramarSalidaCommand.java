package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.dominio.modelo.BusId;

public record ProgramarSalidaCommand(RutaId rutaId, BusId busId, TipoServicio tipoServicio, Instant parteEn) {}
