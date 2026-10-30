package pe.puriy.rutas.adaptador.salida.persistencia;

import static puriy.basedatos.rutas.Tables.TERMINALES;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.TerminalDuplicadoException;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@Repository
@RequiredArgsConstructor
class TerminalJooqAdapter implements TerminalRepositoryPort {

    private final DSLContext dsl;

    @Override
    public void agregar(Terminal terminal) {
        try {
            dsl.insertInto(TERMINALES)
                    .set(TerminalPersistenceMapper.aRegistro(terminal))
                    .execute();
        } catch (DuplicateKeyException excepcion) {
            throw duplicado(terminal);
        }
    }

    @Override
    public Terminal actualizar(Terminal terminal) {
        var datos = terminal.datos();
        try {
            return dsl.update(TERMINALES)
                    .set(TERMINALES.NOMBRE, datos.nombre())
                    .set(TERMINALES.CIUDAD, datos.ciudad())
                    .set(TERMINALES.DIRECCION, datos.direccion())
                    .set(TERMINALES.VENDE, datos.vende())
                    .set(TERMINALES.EMBARCA, datos.embarca())
                    .set(TERMINALES.VERSION, TERMINALES.VERSION.plus(1))
                    .set(TERMINALES.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                    .where(TERMINALES.ID.eq(terminal.id().valor()))
                    .and(TERMINALES.VERSION.eq(terminal.version()))
                    .returning()
                    .fetchOptional()
                    .map(TerminalPersistenceMapper::aDominio)
                    .orElseThrow(EdicionConcurrenteException::new);
        } catch (DuplicateKeyException excepcion) {
            throw duplicado(terminal);
        }
    }

    @Override
    public Optional<Terminal> buscarPorId(TerminalId id) {
        return dsl.selectFrom(TERMINALES)
                .where(TERMINALES.ID.eq(id.valor()))
                .fetchOptional(TerminalPersistenceMapper::aDominio);
    }

    @Override
    public List<Terminal> listarTodos() {
        return dsl.selectFrom(TERMINALES)
                .orderBy(TERMINALES.CIUDAD, TERMINALES.NOMBRE)
                .fetch(TerminalPersistenceMapper::aDominio);
    }

    @Override
    public List<Terminal> listarPorCiudad(String ciudad) {
        return dsl.selectFrom(TERMINALES)
                .where(DSL.lower(TERMINALES.CIUDAD).eq(DSL.lower(DSL.val(ciudad))))
                .orderBy(TERMINALES.NOMBRE)
                .fetch(TerminalPersistenceMapper::aDominio);
    }

    private static TerminalDuplicadoException duplicado(Terminal terminal) {
        return new TerminalDuplicadoException(
                terminal.datos().ciudad(), terminal.datos().nombre());
    }
}
