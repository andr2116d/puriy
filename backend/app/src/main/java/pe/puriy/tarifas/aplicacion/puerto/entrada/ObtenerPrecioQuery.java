package pe.puriy.tarifas.aplicacion.puerto.entrada;

import java.time.LocalDate;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;

public record ObtenerPrecioQuery(CriterioDeTarifa criterio, LocalDate fechaDeViaje) {}
