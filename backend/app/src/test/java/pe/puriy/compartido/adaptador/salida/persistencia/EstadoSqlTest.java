package pe.puriy.compartido.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class EstadoSqlTest {

    @Test
    void reconoce_una_violacion_de_exclusion_envuelta_por_spring() {
        var excepcion = new DataIntegrityViolationException("conflicto", new SQLException("cruce", "23P01"));

        assertThat(EstadoSql.esViolacionDeExclusion(excepcion)).isTrue();
    }

    @Test
    void otra_violacion_de_integridad_no_es_de_exclusion() {
        var excepcion = new DataIntegrityViolationException("duplicado", new SQLException("repetido", "23505"));

        assertThat(EstadoSql.esViolacionDeExclusion(excepcion)).isFalse();
    }
}
