package pe.puriy.tarifas.dominio.modelo;

import java.time.LocalDate;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.dominio.excepcion.TarifaInvalidaException;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Tarifa {

    private static final long VERSION_INICIAL = 0;

    @EqualsAndHashCode.Include
    @ToString.Include
    private final TarifaId id;

    @ToString.Include
    private final CriterioDeTarifa criterio;

    @ToString.Include
    private Vigencia vigencia;

    private Monto precio;

    private final long version;

    @Builder(builderMethodName = "reconstituir")
    private Tarifa(TarifaId id, CriterioDeTarifa criterio, Vigencia vigencia, Monto precio, long version) {
        this.id = id;
        this.criterio = criterio;
        this.vigencia = vigencia;
        this.precio = precio;
        this.version = version;
    }

    public static Tarifa definir(CriterioDeTarifa criterio, Vigencia vigencia, Monto precio) {
        exigirPrecio(precio);
        return new Tarifa(TarifaId.nuevo(), criterio, vigencia, precio, VERSION_INICIAL);
    }

    public void actualizar(Vigencia nuevaVigencia, Monto nuevoPrecio) {
        exigirPrecio(nuevoPrecio);
        vigencia = nuevaVigencia;
        precio = nuevoPrecio;
    }

    public boolean estaVigenteEn(LocalDate fechaDeViaje) {
        return vigencia.incluye(fechaDeViaje);
    }

    public boolean seCruzaCon(Tarifa otra) {
        return !id.equals(otra.id) && criterio.equals(otra.criterio) && vigencia.seCruzaCon(otra.vigencia);
    }

    private static void exigirPrecio(Monto precio) {
        if (!precio.moneda().equals(Monto.SOLES) || !precio.esMayorQue(Monto.cero())) {
            throw TarifaInvalidaException.precioInvalido();
        }
    }
}
