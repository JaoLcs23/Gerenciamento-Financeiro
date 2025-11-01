package com.controle.model;

public class Orcamento {

    private int id;
    private Categoria categoria; // A categoria de DESPESA que está sendo orçada
    private double valorLimite;
    private int mes;
    private int ano;

    // Construtor para novos orçamentos
    public Orcamento(Categoria categoria, double valorLimite, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            throw new IllegalArgumentException("Orçamentos só podem ser definidos para categorias de DESPESA.");
        }
        this.categoria = categoria;
        this.valorLimite = valorLimite;
        this.mes = mes;
        this.ano = ano;
    }

    // Construtor completo (para buscar do DB)
    public Orcamento(int id, Categoria categoria, double valorLimite, int mes, int ano) {
        this(categoria, valorLimite, mes, ano);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public double getValorLimite() { return valorLimite; }
    public void setValorLimite(double valorLimite) { this.valorLimite = valorLimite; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    @Override
    public String toString() {
        return String.format("Orcamento[ID=%d, Cat=%s, Limite=%.2f, Mes/Ano=%d/%d]",
                id, categoria.getNome(), valorLimite, mes, ano);
    }
}