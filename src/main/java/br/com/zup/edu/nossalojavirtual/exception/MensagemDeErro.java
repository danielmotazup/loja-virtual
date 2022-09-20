package br.com.zup.edu.nossalojavirtual.exception;

import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

public class MensagemDeErro {


    private final List<String> mensagens = new ArrayList<>();

    public void adicionar(FieldError fieldError) {
        String mensagem = String.format("O campo %s %s", fieldError.getField(), fieldError.getDefaultMessage());
        mensagens.add(mensagem);
    }

    public void adicionar(String campo, String mensagem) {
        String erro = String.format("O campo %s %s", campo, mensagem);
        mensagens.add(erro);
    }

    public List<String> getMensagens() {
        return mensagens;
    }
}
