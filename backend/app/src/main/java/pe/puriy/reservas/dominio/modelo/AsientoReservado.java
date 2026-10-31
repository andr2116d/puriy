package pe.puriy.reservas.dominio.modelo;

import pe.puriy.compartido.dominio.Monto;

public record AsientoReservado(int numero, int piso, Monto precio) {}
