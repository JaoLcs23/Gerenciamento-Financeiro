package com.controle.model;

import java.time.LocalDate;

public class TransacaoRecorrente {

    private int id;
    private String descricao;
    private double valor;
    private TipoCategoria tipo;
    private Categoria categoria;

    // Campos específicos da recorrência
    private int diaDoMes; // Ex: 5 (para todo dia 5)
    private LocalDate dataInicio;
    private LocalDate dataFim;

    // Controle para evitar duplicatas
    private LocalDate dataUltimoProcessamento; // Última vez que gerou uma Transacao

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
    public int getDiaDoMes() { return diaDoMes; }
    public void setDiaDoMes(int diaDoMes) { this.diaDoMes = diaDoMes; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalDate getDataUltimoProcessamento() { return dataUltimoProcessamento; }
    public void setDataUltimoProcessamento(LocalDate dataUltimoProcessamento) { this.dataUltimoProcessamento = dataUltimoProcessamento; }
}