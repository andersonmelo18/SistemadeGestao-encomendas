package br.edu.uniesp.sistemadegestao.internal.pedido.entity;

public enum StatusPedido {
    EM_ANALISE, APROVADO, FILA, EM_PRODUCAO, FINALIZADO;

    public boolean permiteEdicao() {
        return this == EM_ANALISE || this == APROVADO || this == FILA;
    }

    public boolean permiteTransicaoPara(StatusPedido destino) {
        return switch (this) {
            case EM_ANALISE -> destino == APROVADO;
            case APROVADO -> destino == FILA;
            case FILA -> destino == EM_PRODUCAO;
            case EM_PRODUCAO -> destino == FINALIZADO;
            case FINALIZADO -> false;
        };
    }
}
