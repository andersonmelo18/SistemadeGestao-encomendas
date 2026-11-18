CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    versao BIGINT NOT NULL DEFAULT 0,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    status VARCHAR(20) NOT NULL,
    observacoes VARCHAR(2000),
    valor_total NUMERIC(19,2) NOT NULL CHECK (valor_total > 0),
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL,
    CONSTRAINT ck_pedidos_status CHECK (status IN ('EM_ANALISE','APROVADO','FILA','EM_PRODUCAO','FINALIZADO'))
);
CREATE INDEX ix_pedidos_cliente ON pedidos(cliente_id, id);
CREATE INDEX ix_pedidos_status ON pedidos(status, id);

CREATE TABLE itens_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id),
    produto VARCHAR(150) NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade BETWEEN 1 AND 9999),
    valor_unitario NUMERIC(10,2) NOT NULL CHECK (valor_unitario > 0)
);
CREATE INDEX ix_itens_pedido ON itens_pedido(pedido_id);

CREATE TABLE historico_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id),
    status_anterior VARCHAR(20),
    novo_status VARCHAR(20) NOT NULL,
    responsavel_id BIGINT NOT NULL REFERENCES usuarios(id),
    data_alteracao TIMESTAMP NOT NULL
);
CREATE INDEX ix_historico_pedido ON historico_pedido(pedido_id);
