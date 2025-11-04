package com.controle.service;

import com.controle.dao.*;
import com.controle.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GastoPessoalService {

    private final CategoriaDAO categoriaDAO;
    private final TransacaoDAO transacaoDAO;
    private final TransacaoRecorrenteDAO transacaoRecorrenteDAO;
    private final OrcamentoDAO orcamentoDAO;
    private final ContaDAO contaDAO;

    public GastoPessoalService() {
        this.categoriaDAO = new CategoriaDAO();
        this.transacaoDAO = new TransacaoDAO();
        this.transacaoRecorrenteDAO = new TransacaoRecorrenteDAO();
        this.orcamentoDAO = new OrcamentoDAO();
        this.contaDAO = new ContaDAO();
    }

    // Métodos de Categoria
    public Categoria adicionarCategoria(String nome, TipoCategoria tipo) {
        if (nome == null || nome.trim().isEmpty()) { throw new IllegalArgumentException("O nome da categoria não pode ser vazio."); }
        if (tipo == null) { throw new IllegalArgumentException("O tipo da categoria não pode ser nulo."); }
        if (categoriaDAO.findByNome(nome.trim()) != null) { throw new IllegalArgumentException("Categoria com o nome '" + nome + "' já existe."); }
        Categoria novaCategoria = new Categoria(nome.trim(), tipo);
        categoriaDAO.save(novaCategoria);
        return novaCategoria;
    }
    public Categoria buscarCategoriaPorId(int id) { return categoriaDAO.findById(id); }
    public Categoria buscarCategoriaPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) { return null; }
        return categoriaDAO.findByNome(nome.trim());
    }
    public List<Categoria> listarTodasCategorias() { return categoriaDAO.findAll(); }
    public List<Categoria> listarCategoriasPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) { return categoriaDAO.findAll(); }
        return categoriaDAO.findAllByNomeLike(termoBusca.trim());
    }
    public void atualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() <= 0) { throw new IllegalArgumentException("Categoria inválida para atualização."); }
        Categoria existente = categoriaDAO.findByNome(categoria.getNome());
        if (existente != null && existente.getId() != categoria.getId()) { throw new IllegalArgumentException("Outra categoria já existe com o nome '" + categoria.getNome() + "'."); }
        categoriaDAO.update(categoria);
    }
    public void excluirCategoria(int id) { categoriaDAO.delete(id); }

    // Métodos de Transação
    public Transacao adicionarTransacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, String categoriaNome, String contaNome) {
        if (descricao == null || descricao.trim().isEmpty()) { throw new IllegalArgumentException("A descricao da transação não pode ser vazia."); }
        if (valor <= 0) { throw new IllegalArgumentException("O valor da transação deve ser positivo."); }
        if (data == null || data.isAfter(LocalDate.now())) { throw new IllegalArgumentException("A data da transação não pode ser nula ou futura."); }
        if (tipo == null) { throw new IllegalArgumentException("O tipo da transação não pode ser nulo."); }

        // Validação da Categoria
        Categoria categoria = null;
        if (categoriaNome != null && !categoriaNome.trim().isEmpty()) {
            categoria = categoriaDAO.findByNome(categoriaNome.trim());
            if (categoria == null) { throw new IllegalArgumentException("Categoria '" + categoriaNome + "' não encontrada. Crie-a primeiro."); }
            if (categoria.getTipo() != tipo) { throw new IllegalArgumentException("O tipo da transação (" + tipo + ") não corresponde ao tipo da categoria '" + categoriaNome + "' (" + categoria.getTipo() + ")."); }
        }

        // Validação da Conta
        Conta conta = null;
        if (contaNome == null || contaNome.trim().isEmpty()) {
            throw new IllegalArgumentException("A conta é obrigatória para a transação.");
        }
        conta = contaDAO.findByNome(contaNome.trim());
        if (conta == null) {
            throw new IllegalArgumentException("Conta '" + contaNome + "' não encontrada.");
        }

        Transacao novaTransacao = new Transacao(descricao.trim(), valor, data, tipo, categoria, conta);
        transacaoDAO.save(novaTransacao);
        return novaTransacao;
    }

    public Transacao buscarTransacaoPorId(int id) {
        return transacaoDAO.findById(id);
    }
    public List<Transacao> listarTodasTransacoes() {
        return transacaoDAO.findAll();
    }
    public List<Transacao> listarTransacoesPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return transacaoDAO.findAll();
        }
        return transacaoDAO.findAllByDescriptionLike(termoBusca.trim());
    }

    public void atualizarTransacao(Transacao transacao, String novaCategoriaNome, String novaContaNome) {
        if (transacao == null || transacao.getId() <= 0) { throw new IllegalArgumentException("Transação inválida para atualização."); }
        if (transacao.getValor() <= 0) { throw new IllegalArgumentException("O valor da transação deve ser positivo."); }

        // Validação Categoria
        Categoria categoriaAssociada = null;
        if (novaCategoriaNome != null && !novaCategoriaNome.trim().isEmpty()) {
            categoriaAssociada = categoriaDAO.findByNome(novaCategoriaNome.trim());
            if (categoriaAssociada == null) { throw new IllegalArgumentException("Categoria '" + novaCategoriaNome + "' não encontrada."); }
            if (categoriaAssociada.getTipo() != transacao.getTipo()) { throw new IllegalArgumentException("O tipo da transação não corresponde ao tipo da categoria."); }
        }
        transacao.setCategoria(categoriaAssociada);

        // Validação Conta
        Conta contaAssociada = null;
        if (novaContaNome == null || novaContaNome.trim().isEmpty()) {
            throw new IllegalArgumentException("A conta é obrigatória para a transação.");
        }
        contaAssociada = contaDAO.findByNome(novaContaNome.trim());
        if (contaAssociada == null) {
            throw new IllegalArgumentException("Conta '" + novaContaNome + "' não encontrada.");
        }
        transacao.setConta(contaAssociada);

        transacaoDAO.update(transacao);
    }

    public void excluirTransacao(int id) {
        transacaoDAO.delete(id);
    }

    // Métodos de Relatório
    public double calcularBalancoTotal(LocalDate inicio, LocalDate fim) {
        List<Transacao> transacoesNoPeriodo = transacaoDAO.findAll().stream()
                .filter(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim))
                .collect(Collectors.toList());
        double totalReceitas = transacoesNoPeriodo.stream()
                .filter(t -> t.getTipo() == TipoCategoria.RECEITA)
                .mapToDouble(Transacao::getValor)
                .sum();
        double totalDespesas = transacoesNoPeriodo.stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA)
                .mapToDouble(Transacao::getValor)
                .sum();
        return totalReceitas - totalDespesas;
    }
    public double calcularTotalReceitas(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.RECEITA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .mapToDouble(Transacao::getValor)
                .sum();
    }
    public double calcularTotalDespesas(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .mapToDouble(Transacao::getValor)
                .sum();
    }
    public Map<String, Double> calcularDespesasPorCategoria(LocalDate inicio, LocalDate fim) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == TipoCategoria.DESPESA &&
                        !t.getData().isBefore(inicio) &&
                        !t.getData().isAfter(fim))
                .collect(Collectors.groupingBy(
                        t -> t.getCategoria() != null ? t.getCategoria().getNome() : "Sem Categoria",
                        Collectors.summingDouble(Transacao::getValor)
                ));
    }

    // Métodos de Transação Recorrente

    public void processarTransacoesRecorrentes() {
        LocalDate hoje = LocalDate.now();
        List<TransacaoRecorrente> recorrentesAtivas = transacaoRecorrenteDAO.findAllAtivas(hoje);
        System.out.println("PROCESSANDO TRANSAÇÕES RECORRENTES: " + recorrentesAtivas.size() + " ativas encontradas.");

        for (TransacaoRecorrente tr : recorrentesAtivas) {
            LocalDate dataLancamento;
            try { dataLancamento = LocalDate.of(hoje.getYear(), hoje.getMonth(), tr.getDiaDoMes()); }
            catch (Exception e) { dataLancamento = hoje.withDayOfMonth(hoje.lengthOfMonth()); }

            boolean ehHoraDeLancar = !hoje.isBefore(dataLancamento);
            LocalDate ultimoProcessamento = tr.getDataUltimoProcessamento();
            boolean jaProcessadoEsteMes = ultimoProcessamento != null &&
                    ultimoProcessamento.getYear() == hoje.getYear() &&
                    ultimoProcessamento.getMonth() == hoje.getMonth();
            boolean antesDoInicio = dataLancamento.isBefore(tr.getDataInicio());

            if (ehHoraDeLancar && !jaProcessadoEsteMes && !antesDoInicio) {
                try {
                    System.out.println("LANÇANDO: " + tr.getDescricao());

                    adicionarTransacao(
                            tr.getDescricao(),
                            tr.getValor(),
                            dataLancamento,
                            tr.getTipo(),
                            tr.getCategoria().getNome(),
                            tr.getConta().getNome() // <-- NOVO PARÂMETRO
                    );

                    tr.setDataUltimoProcessamento(hoje);
                    transacaoRecorrenteDAO.update(tr);

                } catch (IllegalArgumentException e) {
                    System.err.println("Erro ao processar transação recorrente ID " + tr.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    public TransacaoRecorrente adicionarTransacaoRecorrente(TransacaoRecorrente tr) {
        if (tr == null) throw new IllegalArgumentException("Transação recorrente não pode ser nula.");
        if (tr.getCategoria() == null) throw new IllegalArgumentException("Categoria é obrigatória.");
        if (tr.getConta() == null) throw new IllegalArgumentException("Conta é obrigatória.");
        transacaoRecorrenteDAO.save(tr);
        return tr;
    }
    public TransacaoRecorrente buscarTransacaoRecorrentePorId(int id) { return transacaoRecorrenteDAO.findById(id); }
    public List<TransacaoRecorrente> listarTodasTransacoesRecorrentes() { return transacaoRecorrenteDAO.findAll(); }
    public List<TransacaoRecorrente> listarTransacoesRecorrentesPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) { return transacaoRecorrenteDAO.findAll(); }
        return transacaoRecorrenteDAO.findAllByDescriptionLike(termoBusca.trim());
    }
    public void atualizarTransacaoRecorrente(TransacaoRecorrente tr) {
        if (tr == null || tr.getId() <= 0) { throw new IllegalArgumentException("Transação recorrente inválida para atualização."); }
        if (tr.getCategoria() == null) throw new IllegalArgumentException("Categoria é obrigatória.");
        if (tr.getConta() == null) throw new IllegalArgumentException("Conta é obrigatória.");
        transacaoRecorrenteDAO.update(tr);
    }
    public void excluirTransacaoRecorrente(int id) { transacaoRecorrenteDAO.delete(id); }

    // Métodos de Orçamento
    public Orcamento adicionarOrcamento(Orcamento orcamento) {
        if (orcamento == null) throw new IllegalArgumentException("Orçamento não pode ser nulo.");
        orcamentoDAO.save(orcamento);
        return orcamento;
    }
    public void atualizarOrcamento(Orcamento orcamento) {
        if (orcamento == null || orcamento.getId() <= 0) { throw new IllegalArgumentException("Orçamento inválido para atualização."); }
        orcamentoDAO.update(orcamento);
    }
    public void excluirOrcamento(int id) { orcamentoDAO.delete(id); }
    public List<Orcamento> listarOrcamentosPorPeriodo(Integer mes, Integer ano) { return orcamentoDAO.findByMesAno(mes, ano); }
    public Orcamento buscarOrcamentoPorCategoriaMesAno(int categoriaId, int mes, int ano) { return orcamentoDAO.findByCategoriaMesAno(categoriaId, mes, ano); }
    public Orcamento getOrcamentoCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) { return null; }
        return orcamentoDAO.findByCategoriaMesAno(categoria.getId(), mes, ano);
    }
    public double getGastoAtualCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) { return 0.0; }
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        Map<String, Double> despesas = calcularDespesasPorCategoria(inicioMes, fimMes);
        return despesas.getOrDefault(categoria.getNome(), 0.0);
    }

    public Conta adicionarConta(Conta conta) {
        if (contaDAO.findByNome(conta.getNome()) != null) { throw new RuntimeException("Já existe uma conta com este nome."); }
        contaDAO.save(conta);
        return conta;
    }
    public void atualizarConta(Conta conta) {
        Conta existente = contaDAO.findByNome(conta.getNome());
        if (existente != null && existente.getId() != conta.getId()) { throw new RuntimeException("Já existe outra conta com este nome."); }
        contaDAO.update(conta);
    }
    public void excluirConta(int id) { contaDAO.delete(id); }
    public List<Conta> listarTodasContas() { return contaDAO.findAll(); }
    public List<Conta> listarContasPorTermo(String termo) {
        if (termo == null || termo.trim().isEmpty()) { return contaDAO.findAll(); }
        return contaDAO.findAllByNomeLike(termo.trim());
    }
}