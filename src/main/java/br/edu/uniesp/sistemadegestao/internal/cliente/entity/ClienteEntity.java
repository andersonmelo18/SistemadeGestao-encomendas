package br.edu.uniesp.sistemadegestao.internal.cliente.entity;

import br.edu.uniesp.sistemadegestao.internal.equipamento.entity.EquipamentoEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 11, unique = true)
    private String cpf;

    @Column(length = 150, unique = true)
    private String email;

    @Column(length = 11, unique = true)
    private String celular;

    @Column(nullable = false)
    private boolean ativo= true;

    @OneToMany(mappedBy = "cliente")
    private List<EquipamentoEntity> equipamentos = new java.util.ArrayList<>();

    public ClienteEntity(String nome, String cpf, String email, String celular) {
        this(nome, cpf, email);
        this.celular = celular;
    }

    public ClienteEntity(String nome, String cpf, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    public void adicionarEquipamento(EquipamentoEntity equipamento) {
        equipamentos.add(equipamento);
        equipamento.associarCliente(this);
    }

    public void alterarNome(String nome) {
        this.nome = nome;
    }

    public void alterarEmail(String email) {
        this.email = email;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
    }

}
