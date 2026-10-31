package pe.puriy.reservas.aplicacion.servicio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.salida.TarifaPort;
import pe.puriy.reservas.dominio.excepcion.TramoSinTarifaException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Tramo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class AsientosConPrecio {

    static List<AsientoReservado> de(TarifaPort tarifas, Salida salida, Tramo tramo, List<Integer> numeros) {
        var precioPorPiso = new HashMap<Integer, Monto>();
        var asientos = new ArrayList<AsientoReservado>();
        for (var numero : numeros) {
            var piso = salida.pisoDe(numero);
            var precio = precioPorPiso.get(piso);
            if (precio == null) {
                precio = tarifas.buscarPrecio(salida.consultaDePrecio(tramo, piso))
                        .orElseThrow(() -> new TramoSinTarifaException(piso));
                precioPorPiso.put(piso, precio);
            }
            asientos.add(new AsientoReservado(numero, piso, precio));
        }
        return asientos;
    }
}
