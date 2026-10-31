package pe.puriy.tarifas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import pe.puriy.tarifas.dominio.modelo.Vigencia;

public record DetalleTarifa(TarifaId id, CriterioDeTarifa criterio, Vigencia vigencia, Monto precio, long version) {

    public static DetalleTarifa desde(Tarifa tarifa) {
        return new DetalleTarifa(tarifa.id(), tarifa.criterio(), tarifa.vigencia(), tarifa.precio(), tarifa.version());
    }
}
