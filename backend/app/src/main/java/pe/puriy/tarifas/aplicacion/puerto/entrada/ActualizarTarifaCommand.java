package pe.puriy.tarifas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import pe.puriy.tarifas.dominio.modelo.Vigencia;

public record ActualizarTarifaCommand(TarifaId id, long version, Vigencia vigencia, Monto precio) {}
