package br.edu.uniesp.sistemadegestao.shared.exception;
public class ConflitoVersaoException extends RuntimeException {
    public ConflitoVersaoException() {
        super("O pedido foi alterado por outra operacao. Atualize os dados antes de tentar novamente.");
    }
}
