package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.Recurrencia;

public record ProgramarSalidasRecurrentesCommand(
        RutaId rutaId, BusId busId, TipoServicio tipoServicio, Recurrencia recurrencia) {}
