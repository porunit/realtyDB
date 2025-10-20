// porunit.w8.realtydb.advice.ApiExceptionHandler.java
package porunit.w8.realtydb.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public record ApiError(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, Object> details
    ) {}

    // ⬇️ ВАЖНО: вместо @ExceptionHandler(HttpMessageNotWritableException)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        var body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                "Internal Server Error",
                "Failed to serialize response",
                path(request),
                Map.of("hint", "Avoid serializing LOB fields (e.g., images) directly in JSON")
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> notFound(EntityNotFoundException ex, ServletWebRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badReq(IllegalArgumentException ex, ServletWebRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException ex, ServletWebRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Invalid parameter: " + ex.getName(),
                req, Map.of("param", ex.getName(), "value", String.valueOf(ex.getValue())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(ConstraintViolationException ex, ServletWebRequest req) {
        var fields = new LinkedHashMap<String, String>();
        ex.getConstraintViolations().forEach(v -> fields.put(v.getPropertyPath().toString(), v.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", req, Map.of("fields", fields));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> rse(ResponseStatusException ex, ServletWebRequest req) {
        return build(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), req, null);
    }

    // Bean Validation на @RequestBody
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        var fields = new LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        var body = new ApiError(OffsetDateTime.now(),
                status.value(), "Bad Request", "Validation failed", path(request), Map.of("fields", fields));
        return ResponseEntity.status(status).body(body);
    }

    // Fallback на всё прочее
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> other(Exception ex, ServletWebRequest req) {
        Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(ex);
        var details = new LinkedHashMap<String,Object>();
        details.put("exception", ex.getClass().getSimpleName());
        if (root != null && root != ex) {
            details.put("cause", root.getClass().getSimpleName());
            details.put("causeMessage", root.getMessage());
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, details);
    }


    private ResponseEntity<ApiError> build(HttpStatus status, String message, ServletWebRequest req, Map<String, Object> details) {
        var body = new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
                message, req.getRequest().getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest swr) return swr.getRequest().getRequestURI();
        return "";
    }
}
