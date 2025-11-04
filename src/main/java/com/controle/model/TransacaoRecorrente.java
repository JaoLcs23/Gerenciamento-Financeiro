package com.controle.model;

import java.time.LocalDate;

public class TransacaoRecorrente {

    private int id;
    private String descricao;
    private double valor;
    private TipoCategoria tipo;
    private Categoria categoria;
    private Conta conta;
    private int diaDoMes;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataUltimoProcessamento;

    public TransacaoRecorrente(String descricao, double valor, TipoCategoria tipo, Categoria categoria, Conta conta, int diaDoMes, LocalDate dataInicio, LocalDate dataFim) {
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.categoria = categoria;
        this.conta = conta;
        this.diaDoMes = diaDoMes;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public TransacaoRecorrente(int id, String descricao, double valor, TipoCategoria tipo, Categoria categoria, Conta conta, int diaDoMes, LocalDate dataInicio, LocalDate dataFim, LocalDate dataUltimoProcessamento) {
        this(descricao, valor, tipo, categoria, conta, diaDoMes, dataInicio, dataFim);
        this.id = id;
        this.dataUltimoProcessamento = dataUltimoProcessamento;
    }

    public TransacaoRecorrente() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public TipoCategoria getTipo() { return tipo; }
    public void setTipo(TipoCategoria tipo) { this.tipo = tipo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Conta getConta() { return conta; } // <-- NOVO
    public void setConta(Conta conta) { this.conta = conta; } // <-- NOVO
    public int getDiaDoMes() { return diaDoMes; }
    public void setDiaDoMes(int diaDoMes) { this.diaDoMes = diaDoMes; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalDate getDataUltimoProcessamento() { return dataUltimoProcessamento; }
    public void setDataUltimoProcessamento(LocalDate dataUltimoProcessamento) { this.dataUltimoProcessamento = dataUltimoProcessamento; }
}