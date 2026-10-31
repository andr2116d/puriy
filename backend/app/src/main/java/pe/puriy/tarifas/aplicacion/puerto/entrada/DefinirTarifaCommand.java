package pe.puriy.tarifas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.Vigencia;

public record DefinirTarifaCommand(CriterioDeTarifa criterio, Vigencia vigencia, Monto precio) {}
