package com.controle.app;

import com.controle.model.Categoria;
import com.controle.model.Conta;
import com.controle.model.TipoCategoria;
import com.controle.model.TipoConta;
import com.controle.model.Transacao;
import com.controle.service.GastoPessoalService;
import com.controle.util.DatabaseConnection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MainApp {

    public static void main(String[] args) {
        System.out.println("Iniciando o Sistema de Controle de Gastos Pessoais...");

        try {
            DatabaseConnection.createTables();
            System.out.println("Tabelas verificadas/criadas no SQL Server.");
        } catch (Exception e) {
            System.err.println("Erro ao criar/verificar tabelas: " + e.getMessage());
            return;
        }

        GastoPessoalService service = new GastoPessoalService();
        System.out.println("\nServiço de Gastos Pessoais inicializado.");

        System.out.println("\n--- Adicionando Contas Default ---");
        Conta contaDefault = null;
        try {
            contaDefault = service.adicionarConta(new Conta("Dinheiro", 0.0, TipoConta.DINHEIRO));
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            contaDefault = service.listarContasPorTermo("Dinheiro").get(0);
        }
        final String CONTA_DEFAULT_NOME = contaDefault.getNome();

        System.out.println("\n--- Adicionando Categorias ---");
        try {
            service.adicionarCategoria("Salário", TipoCategoria.RECEITA);
            service.adicionarCategoria("Alimentação", TipoCategoria.DESPESA);
            service.adicionarCategoria("Transporte", TipoCategoria.DESPESA);
            service.adicionarCategoria("Lazer", TipoCategoria.DESPESA);
            service.adicionarCategoria("Moradia", TipoCategoria.DESPESA);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao adicionar categoria: " + e.getMessage());
        }

        System.out.println("\n--- Categorias Cadastradas ---");
        List<Categoria> categorias = service.listarTodasCategorias();
        categorias.forEach(System.out::println);

        System.out.println("\n--- Adicionando Transações ---");
        try {
            service.adicionarTransacao("Salário Mensal", 3000.00, LocalDate.of(2025, 5, 30), TipoCategoria.RECEITA, "Salário", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Almoço no restaurante", 45.50, LocalDate.of(2025, 6, 1), TipoCategoria.DESPESA, "Alimentação", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Gasolina do carro", 80.00, LocalDate.of(2025, 6, 2), TipoCategoria.DESPESA, "Transporte", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Cinema com a namorada", 55.00, LocalDate.of(2025, 6, 2), TipoCategoria.DESPESA, "Lazer", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Conta de luz", 120.00, LocalDate.of(2025, 6, 3), TipoCategoria.DESPESA, "Moradia", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Freelance Marketing", 700.00, LocalDate.of(2025, 6, 7), TipoCategoria.RECEITA, "Salário", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Mercado da semana", 150.00, LocalDate.of(2025, 6, 8), TipoCategoria.DESPESA, "Alimentação", CONTA_DEFAULT_NOME);
            service.adicionarTransacao("Uber para trabalho", 25.00, LocalDate.of(2025, 6, 8), TipoCategoria.DESPESA, "Transporte", CONTA_DEFAULT_NOME);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao adicionar transação: " + e.getMessage());
        }

        System.out.println("\n--- Todas as Transações ---");
        List<Transacao> todasTransacoes = service.listarTodasTransacoes();
        todasTransacoes.forEach(System.out::println);

        LocalDate inicioGeral = LocalDate.of(1900, 1, 1);
        LocalDate fimGeral = LocalDate.now();

        System.out.println("\n--- Balanço Total ---");
        double balanco = service.calcularBalancoTotal(inicioGeral, fimGeral);
        System.out.printf("Balanço Total: R$ %.2f%n", balanco);

        System.out.println("\n--- Despesas por Categoria (Maio-Junho 2025) ---");
        LocalDate inicioPeriodo = LocalDate.of(2025, 5, 1);
        LocalDate fimPeriodo = LocalDate.of(2025, 6, 30);
        Map<String, Double> despesasPorCategoria = service.calcularDespesasPorCategoria(inicioPeriodo, fimPeriodo);
        despesasPorCategoria.forEach((cat, total) -> System.out.printf("%s: R$ %.2f%n", cat, total));

        System.out.println("\n--- Atualizando uma Transação ---");
        Transacao transacaoParaAtualizar = service.buscarTransacaoPorId(2);
        if (transacaoParaAtualizar != null) {
            System.out.println("Transação antes da atualização: " + transacaoParaAtualizar);
            transacaoParaAtualizar.setDescricao("Almoço com cliente");
            transacaoParaAtualizar.setValor(60.00);
            try {
                service.atualizarTransacao(transacaoParaAtualizar, "Alimentação", CONTA_DEFAULT_NOME);
                System.out.println("Transação após atualização: " + service.buscarTransacaoPorId(2));
            } catch (IllegalArgumentException e) {
                System.err.println("Erro ao atualizar transação: " + e.getMessage());
            }
        } else {
            System.out.println("Transação com ID 2 não encontrada para atualizar.");
        }

        System.out.println("\n--- Excluindo uma Transacao ---");
        int idParaExcluir = 4;
        service.excluirTransacao(idParaExcluir);
        Transacao transacaoExcluida = service.buscarTransacaoPorId(idParaExcluir);
        if (transacaoExcluida == null) {
            System.out.println("Transacao com ID " + idParaExcluir + " foi realmente excluída.");
        } else {
            System.out.println("Erro: Transação com ID " + idParaExcluir + " ainda existe.");
        }

        System.out.println("\n--- Balanço Total Após Exclusão ---");
        balanco = service.calcularBalancoTotal(inicioGeral, fimGeral);
        System.out.printf("Novo Balanço Total: R$ %.2f%n", balanco);

        System.out.println("\nSistema de Controle de Gastos Pessoais finalizado.");
    }
}