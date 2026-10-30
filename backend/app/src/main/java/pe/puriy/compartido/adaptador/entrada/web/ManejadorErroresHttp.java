package pe.puriy.compartido.adaptador.entrada.web;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;

@Slf4j
@RestControllerAdvice
class ManejadorErroresHttp extends ResponseEntityExceptionHandler {

    private static final String BASE_TIPOS = "https://puriy.pe/errores/";
    private static final String MENSAJE_ERROR_INTERNO = "Ocurrió un error inesperado. Intenta nuevamente.";

    @ExceptionHandler(ReglaDeNegocioException.class)
    ProblemDetail reglaDeNegocio(ReglaDeNegocioException excepcion) {
        log.atInfo().addKeyValue("codigo", excepcion.codigo()).log("regla de negocio rechazada");
        return problema(HttpStatus.CONFLICT, excepcion.codigo(), excepcion.mensaje());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail recursoNoEncontrado(RecursoNoEncontradoException excepcion) {
        return problema(HttpStatus.NOT_FOUND, "no-encontrado", excepcion.mensaje());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail errorInesperado(Exception excepcion) {
        log.atError().setCause(excepcion).log("error no controlado");
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "error-interno", MENSAJE_ERROR_INTERNO);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excepcion,
            HttpHeaders cabeceras,
            HttpStatusCode estado,
            WebRequest solicitud) {
        var problema = problema(HttpStatus.BAD_REQUEST, "solicitud-invalida", "La solicitud tiene campos inválidos.");
        problema.setProperty("campos", camposInvalidos(excepcion));
        return ResponseEntity.badRequest().body(problema);
    }

    private static List<CampoInvalido> camposInvalidos(MethodArgumentNotValidException excepcion) {
        return excepcion.getBindingResult().getFieldErrors().stream()
                .map(ManejadorErroresHttp::campoInvalido)
                .toList();
    }

    private static CampoInvalido campoInvalido(FieldError error) {
        return new CampoInvalido(
                error.getField(), Objects.requireNonNullElse(error.getDefaultMessage(), "valor inválido"));
    }

    private static ProblemDetail problema(HttpStatus estado, String codigo, String detalle) {
        var problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setType(URI.create(BASE_TIPOS + codigo));
        problema.setProperty("codigo", codigo);
        return problema;
    }

    record CampoInvalido(String campo, String mensaje) {}
}
