package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.Recurrencia;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

public record ProgramarSalidasRecurrentesCommand(
        RutaId rutaId, BusId busId, TipoServicio tipoServicio, Recurrencia recurrencia) {}
