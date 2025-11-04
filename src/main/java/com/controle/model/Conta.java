package com.controle.model;

public class Conta {
    private int id;
    private String nome;
    private double saldoInicial;
    private TipoConta tipo;

    public Conta(String nome, double saldoInicial, TipoConta tipo) {
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
    }

    public Conta(int id, String nome, double saldoInicial, TipoConta tipo) {
        this.id = id;
        this.nome = nome;
        this.saldoInicial = saldoInicial;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(double saldoInicial) { this.saldoInicial = saldoInicial; }
    public TipoConta getTipo() { return tipo; }
    public void setTipo(TipoConta tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return nome;
    }
}