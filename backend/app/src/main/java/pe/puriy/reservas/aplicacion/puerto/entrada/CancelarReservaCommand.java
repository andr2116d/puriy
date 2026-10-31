package pe.puriy.reservas.aplicacion.puerto.entrada;

import pe.puriy.reservas.dominio.modelo.ReservaId;

public record CancelarReservaCommand(ReservaId id) {}
