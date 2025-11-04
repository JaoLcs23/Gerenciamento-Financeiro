package com.controle.model;

import java.time.LocalDate;

public class Transacao {
    private int id;
    private String descricao;
    private double valor;
    private LocalDate data;
    private TipoCategoria tipo;
    private Categoria categoria;
    private Conta conta;

    public Transacao(int id, String descricao, double valor, LocalDate data, TipoCategoria tipo, Categoria categoria, Conta conta) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.categoria = categoria;
        this.conta = conta;
    }

    public Transacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, Categoria categoria, Conta conta) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.categoria = categoria;
        this.conta = conta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public TipoCategoria getTipo() { return tipo; }
    public void setTipo(TipoCategoria tipo) { this.tipo = tipo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Conta getConta() { return conta; }
    public void setConta(Conta conta) { this.conta = conta; }

    @Override
    public String toString() {
        return "Transacao{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", data=" + data +
                ", tipo=" + tipo +
                ", categoria=" + (categoria != null ? categoria.getNome() : "N/A") +
                ", conta=" + (conta != null ? conta.getNome() : "N/A") + // <-- NOVO
                '}';
    }
}