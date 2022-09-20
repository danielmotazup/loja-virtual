package br.com.zup.edu.nossalojavirtual.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class HandlerException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodHandle(MethodArgumentNotValidException ex) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        MensagemDeErro mensagemDeErro = new MensagemDeErro();
        fieldErrors.forEach(mensagemDeErro::adicionar);

        return ResponseEntity.badRequest().body(mensagemDeErro);

    }
}
