package pe.puriy.tarifas.aplicacion.puerto.entrada;

import pe.puriy.tarifas.dominio.modelo.TarifaId;

public record EliminarTarifaCommand(TarifaId id, long version) {}
