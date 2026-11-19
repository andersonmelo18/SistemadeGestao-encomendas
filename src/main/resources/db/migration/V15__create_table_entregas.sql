
CREATE TABLE entregas (
                          id BIGSERIAL PRIMARY KEY,
                          versao BIGINT NOT NULL DEFAULT 0,

                          pedido_id BIGINT NOT NULL,
                          entregador_id BIGINT NOT NULL,

                          status VARCHAR(20) NOT NULL DEFAULT 'AGUARDANDO',

                          endereco_entrega VARCHAR(300) NOT NULL,
                          observacoes VARCHAR(1000),

                          criado_em TIMESTAMP NOT NULL,
                          atualizado_em TIMESTAMP NOT NULL,
                          saiu_em TIMESTAMP,
                          entregue_em TIMESTAMP,

                          CONSTRAINT uk_entregas_pedido UNIQUE (pedido_id),

                          CONSTRAINT fk_entregas_pedido
                              FOREIGN KEY (pedido_id) REFERENCES pedidos(id),

                          CONSTRAINT fk_entregas_entregador
                              FOREIGN KEY (entregador_id) REFERENCES usuarios(id),

                          CONSTRAINT ck_entregas_status
                              CHECK (status IN ('AGUARDANDO', 'EM_TRANSITO', 'ENTREGUE'))
);

CREATE INDEX ix_entregas_entregador
    ON entregas(entregador_id, id);

CREATE INDEX ix_entregas_status
    ON entregas(status, id);
