package pe.puriy.reservas.dominio.modelo;

import pe.puriy.compartido.dominio.Monto;

public record PisoDisponible(int piso, int asientosLibres, Monto precio) {}
