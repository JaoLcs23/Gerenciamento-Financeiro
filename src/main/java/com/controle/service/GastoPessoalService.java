package com.controle.service;

import com.controle.dao.*;
import com.controle.model.*;
import com.controle.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
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
        this.contaDAO = new ContaDAO();
        this.orcamentoDAO = new OrcamentoDAO(this.categoriaDAO);
        this.transacaoDAO = new TransacaoDAO(this.categoriaDAO, this.contaDAO);
        this.transacaoRecorrenteDAO = new TransacaoRecorrenteDAO(this.categoriaDAO, this.contaDAO);
    }

    public Categoria adicionarCategoria(String nome, TipoCategoria tipo) {
        if (nome == null || nome.trim().isEmpty()) { throw new IllegalArgumentException("O nome da categoria não pode ser vazio."); }
        if (tipo == null) { throw new IllegalArgumentException("O tipo da categoria não pode ser nulo."); }

        return executeWrite(conn -> {
            if (categoriaDAO.findByNome(nome.trim(), conn) != null) {
                throw new IllegalArgumentException("Categoria com o nome '" + nome + "' já existe.");
            }
            Categoria novaCategoria = new Categoria(nome.trim(), tipo);
            categoriaDAO.save(novaCategoria, conn);
            return novaCategoria;
        });
    }

    public Categoria buscarCategoriaPorId(int id) {
        return executeRead(conn -> categoriaDAO.findById(id, conn));
    }

    public Categoria buscarCategoriaPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) { return null; }
        return executeRead(conn -> categoriaDAO.findByNome(nome.trim(), conn));
    }

    public List<Categoria> listarTodasCategorias() {
        return executeRead(categoriaDAO::findAll);
    }

    public List<Categoria> listarCategoriasPorTermo(String termoBusca) {
        return executeRead(conn -> {
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                return categoriaDAO.findAll(conn);
            }
            return categoriaDAO.findAllByNomeLike(termoBusca.trim(), conn);
        });
    }

    public void atualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() <= 0) { throw new IllegalArgumentException("Categoria inválida para atualização."); }

        executeWrite(conn -> {
            Categoria existente = categoriaDAO.findByNome(categoria.getNome(), conn);
            if (existente != null && existente.getId() != categoria.getId()) {
                throw new IllegalArgumentException("Outra categoria já existe com o nome '" + categoria.getNome() + "'.");
            }
            categoriaDAO.update(categoria, conn);
            return null;
        });
    }

    public void excluirCategoria(int id) {
        executeWrite(conn -> {
            categoriaDAO.delete(id, conn);
            return null;
        });
    }

    public Transacao adicionarTransacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, String categoriaNome, String contaNome) {
        if (descricao == null || descricao.trim().isEmpty()) { throw new IllegalArgumentException("A descricao da transação não pode ser vazia."); }
        if (valor <= 0) { throw new IllegalArgumentException("O valor da transação deve ser positivo."); }
        if (data == null || data.isAfter(LocalDate.now())) { throw new IllegalArgumentException("A data da transação não pode ser nula ou futura."); }
        if (tipo == null) { throw new IllegalArgumentException("O tipo da transação não pode ser nulo."); }
        if (contaNome == null || contaNome.trim().isEmpty()) { throw new IllegalArgumentException("A conta é obrigatória para a transação."); }

        return executeWrite(conn -> {
            Categoria categoria = null;
            if (categoriaNome != null && !categoriaNome.trim().isEmpty()) {
                categoria = categoriaDAO.findByNome(categoriaNome.trim(), conn);
                if (categoria == null) { throw new IllegalArgumentException("Categoria '" + categoriaNome + "' não encontrada. Crie-a primeiro."); }
                if (categoria.getTipo() != tipo) { throw new IllegalArgumentException("O tipo da transação (" + tipo + ") não corresponde ao tipo da categoria '" + categoriaNome + "' (" + categoria.getTipo() + ")."); }
            }

            Conta conta = contaDAO.findByNome(contaNome.trim(), conn);
            if (conta == null) {
                throw new IllegalArgumentException("Conta '" + contaNome + "' não encontrada.");
            }

            Transacao novaTransacao = new Transacao(descricao.trim(), valor, data, tipo, categoria, conta);
            transacaoDAO.save(novaTransacao, conn);
            return novaTransacao;
        });
    }

    public Transacao buscarTransacaoPorId(int id) {
        return executeRead(conn -> transacaoDAO.findById(id, conn));
    }

    public List<Transacao> listarTodasTransacoes() {
        return executeRead(transacaoDAO::findAll);
    }

    public List<Transacao> listarTransacoesPorTermo(String termoBusca) {
        return executeRead(conn -> {
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                return transacaoDAO.findAll(conn);
            }
            return transacaoDAO.findAllByDescriptionLike(termoBusca.trim(), conn);
        });
    }

    public List<Transacao> listarTransacoesPorConta(int contaId) {
        return executeRead(conn -> transacaoDAO.findByContaId(contaId, conn));
    }

    public void atualizarTransacao(Transacao transacao, String novaCategoriaNome, String novaContaNome) {
        if (transacao == null || transacao.getId() <= 0) { throw new IllegalArgumentException("Transação inválida para atualização."); }
        if (transacao.getValor() <= 0) { throw new IllegalArgumentException("O valor da transação deve ser positivo."); }
        if (novaContaNome == null || novaContaNome.trim().isEmpty()) { throw new IllegalArgumentException("A conta é obrigatória para a transação."); }

        executeWrite(conn -> {
            Categoria categoriaAssociada = null;
            if (novaCategoriaNome != null && !novaCategoriaNome.trim().isEmpty()) {
                categoriaAssociada = categoriaDAO.findByNome(novaCategoriaNome.trim(), conn);
                if (categoriaAssociada == null) { throw new IllegalArgumentException("Categoria '" + novaCategoriaNome + "' não encontrada."); }
                if (categoriaAssociada.getTipo() != transacao.getTipo()) { throw new IllegalArgumentException("O tipo da transação não corresponde ao tipo da categoria."); }
            }
            transacao.setCategoria(categoriaAssociada);

            Conta contaAssociada = contaDAO.findByNome(novaContaNome.trim(), conn);
            if (contaAssociada == null) {
                throw new IllegalArgumentException("Conta '" + novaContaNome + "' não encontrada.");
            }
            transacao.setConta(contaAssociada);

            transacaoDAO.update(transacao, conn);
            return null;
        });
    }

    public void excluirTransacao(int id) {
        executeWrite(conn -> {
            transacaoDAO.delete(id, conn);
            return null;
        });
    }

    private List<Transacao> getTransacoesPeriodo(LocalDate inicio, LocalDate fim, Connection conn) {
        return transacaoDAO.findAll(conn).stream()
                .filter(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim))
                .collect(Collectors.toList());
    }

    public double calcularBalancoTotal(LocalDate inicio, LocalDate fim) {
        return executeRead(conn -> {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);
            double totalReceitas = transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.RECEITA)
                    .mapToDouble(Transacao::getValor)
                    .sum();
            double totalDespesas = transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.DESPESA)
                    .mapToDouble(Transacao::getValor)
                    .sum();
            return totalReceitas - totalDespesas;
        });
    }

    public double calcularTotalReceitas(LocalDate inicio, LocalDate fim) {
        return executeRead(conn -> {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);
            return transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.RECEITA && t.getCategoria() != null)
                    .mapToDouble(Transacao::getValor)
                    .sum();
        });
    }

    public double calcularTotalDespesas(LocalDate inicio, LocalDate fim) {
        return executeRead(conn -> {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);
            return transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.DESPESA && t.getCategoria() != null)
                    .mapToDouble(Transacao::getValor)
                    .sum();
        });
    }

    public Map<String, Double> calcularDespesasPorCategoria(LocalDate inicio, LocalDate fim) {
        return executeRead(conn -> {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);
            return transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.DESPESA && t.getCategoria() != null)
                    .collect(Collectors.groupingBy(
                            t -> t.getCategoria().getNome(),
                            Collectors.summingDouble(Transacao::getValor)
                    ));
        });
    }

    public void processarTransacoesRecorrentes() {
        LocalDate hoje = LocalDate.now();

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<TransacaoRecorrente> recorrentesAtivas = transacaoRecorrenteDAO.findAllAtivas(hoje, conn);
            System.out.println("PROCESSANDO TRANSAÇÕES RECORRENTES: " + recorrentesAtivas.size() + " ativas encontradas.");

            for (TransacaoRecorrente tr : recorrentesAtivas) {
                conn.setAutoCommit(false);
                try {
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
                        System.out.println("LANÇANDO: " + tr.getDescricao());

                        Categoria categoria = tr.getCategoria();
                        Conta conta = tr.getConta();
                        if (categoria.getTipo() != tr.getTipo()) {
                            throw new IllegalArgumentException("Tipo da transação e da categoria não batem.");
                        }

                        Transacao novaTransacao = new Transacao(
                                tr.getDescricao(), tr.getValor(), dataLancamento,
                                tr.getTipo(), categoria, conta
                        );
                        transacaoDAO.save(novaTransacao, conn);
                        tr.setDataUltimoProcessamento(hoje);
                        transacaoRecorrenteDAO.update(tr, conn);
                        conn.commit();
                    } else {
                        conn.rollback();
                    }
                } catch (Exception e) {
                    conn.rollback();
                    System.err.println("Erro ao processar transação recorrente ID " + tr.getId() + ": " + e.getMessage());
                }
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao processar transações recorrentes.", e);
        }
    }

    public TransacaoRecorrente adicionarTransacaoRecorrente(TransacaoRecorrente tr) {
        if (tr == null) throw new IllegalArgumentException("Transação recorrente não pode ser nula.");
        if (tr.getCategoria() == null) throw new IllegalArgumentException("Categoria é obrigatória.");
        if (tr.getConta() == null) throw new IllegalArgumentException("Conta é obrigatória.");

        return executeWrite(conn -> {
            transacaoRecorrenteDAO.save(tr, conn);
            return tr;
        });
    }

    public TransacaoRecorrente buscarTransacaoRecorrentePorId(int id) {
        return executeRead(conn -> transacaoRecorrenteDAO.findById(id, conn));
    }

    public List<TransacaoRecorrente> listarTodasTransacoesRecorrentes() {
        return executeRead(transacaoRecorrenteDAO::findAll);
    }

    public List<TransacaoRecorrente> listarTransacoesRecorrentesPorTermo(String termoBusca) {
        return executeRead(conn -> {
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                return transacaoRecorrenteDAO.findAll(conn);
            }
            return transacaoRecorrenteDAO.findAllByDescriptionLike(termoBusca.trim(), conn);
        });
    }

    public void atualizarTransacaoRecorrente(TransacaoRecorrente tr) {
        if (tr == null || tr.getId() <= 0) { throw new IllegalArgumentException("Transação recorrente inválida para atualização."); }
        if (tr.getCategoria() == null) { throw new IllegalArgumentException("Categoria é obrigatória."); }
        if (tr.getConta() == null) { throw new IllegalArgumentException("Conta é obrigatória."); }

        executeWrite(conn -> {
            transacaoRecorrenteDAO.update(tr, conn);
            return null;
        });
    }

    public void excluirTransacaoRecorrente(int id) {
        executeWrite(conn -> {
            transacaoRecorrenteDAO.delete(id, conn);
            return null;
        });
    }

    public Orcamento adicionarOrcamento(Orcamento orcamento) {
        if (orcamento == null) throw new IllegalArgumentException("Orçamento não pode ser nulo.");

        return executeWrite(conn -> {
            Orcamento existente = orcamentoDAO.findByCategoriaMesAno(
                    orcamento.getCategoria().getId(), orcamento.getMes(), orcamento.getAno(), conn
            );
            if (existente != null) {
                throw new RuntimeException("Já existe um orçamento para esta categoria neste mês/ano.");
            }
            orcamentoDAO.save(orcamento, conn);
            return orcamento;
        });
    }

    public void atualizarOrcamento(Orcamento orcamento) {
        if (orcamento == null || orcamento.getId() <= 0) { throw new IllegalArgumentException("Orçamento inválido para atualização."); }

        executeWrite(conn -> {
            Orcamento existente = orcamentoDAO.findByCategoriaMesAno(
                    orcamento.getCategoria().getId(), orcamento.getMes(), orcamento.getAno(), conn
            );
            if (existente != null && existente.getId() != orcamento.getId()) {
                throw new RuntimeException("Já existe outro orçamento para esta categoria neste mês/ano.");
            }
            orcamentoDAO.update(orcamento, conn);
            return null;
        });
    }

    public void excluirOrcamento(int id) {
        executeWrite(conn -> {
            orcamentoDAO.delete(id, conn);
            return null;
        });
    }

    public List<Orcamento> listarOrcamentosPorPeriodo(Integer mes, Integer ano) {
        return executeRead(conn -> orcamentoDAO.findByMesAno(mes, ano, conn));
    }

    public Orcamento buscarOrcamentoPorCategoriaMesAno(int categoriaId, int mes, int ano) {
        return executeRead(conn -> orcamentoDAO.findByCategoriaMesAno(categoriaId, mes, ano, conn));
    }

    public Orcamento getOrcamentoCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) { return null; }
        return executeRead(conn -> orcamentoDAO.findByCategoriaMesAno(categoria.getId(), mes, ano, conn));
    }

    public double getGastoAtualCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) { return 0.0; }
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        Map<String, Double> despesas = calcularDespesasPorCategoria(inicioMes, fimMes);
        return despesas.getOrDefault(categoria.getNome(), 0.0);
    }

    public Conta adicionarConta(Conta conta) {
        return executeWrite(conn -> {
            if (contaDAO.findByNome(conta.getNome(), conn) != null) {
                throw new RuntimeException("Já existe uma conta com este nome.");
            }
            contaDAO.save(conta, conn);
            return conta;
        });
    }

    public void atualizarConta(Conta conta) {
        executeWrite(conn -> {
            Conta existente = contaDAO.findByNome(conta.getNome(), conn);
            if (existente != null && existente.getId() != conta.getId()) {
                throw new RuntimeException("Já existe outra conta com este nome.");
            }
            contaDAO.update(conta, conn);
            return null;
        });
    }

    public void excluirConta(int id) {
        executeWrite(conn -> {
            contaDAO.delete(id, conn);
            return null;
        });
    }

    public List<Conta> listarTodasContas() {
        return executeRead(contaDAO::findAll);
    }

    public List<Conta> listarContasPorTermo(String termo) {
        return executeRead(conn -> {
            if (termo == null || termo.trim().isEmpty()) {
                return contaDAO.findAll(conn);
            }
            return contaDAO.findAllByNomeLike(termo.trim(), conn);
        });
    }

    public void transferirFundos(Conta contaOrigem, Conta contaDestino, double valor, LocalDate data) {
        if (contaOrigem == null || contaDestino == null) { throw new IllegalArgumentException("As contas de origem e destino são obrigatórias."); }
        if (contaOrigem.getId() == contaDestino.getId()) { throw new IllegalArgumentException("A conta de origem e destino não podem ser a mesma."); }
        if (valor <= 0) { throw new IllegalArgumentException("O valor da movimentação deve ser positivo."); }
        if (data == null || data.isAfter(LocalDate.now())) { throw new IllegalArgumentException("A data não pode ser nula ou futura."); }

        executeWrite(conn -> {
            if (contaOrigem.getTipo() != TipoConta.CARTAO_DE_CREDITO) {
                double saldoAtualOrigem = calcularSaldoAtual(contaOrigem, conn);
                if (saldoAtualOrigem < valor) {
                    throw new IllegalArgumentException(
                            String.format("Saldo insuficiente na conta '%s'. Saldo atual: R$ %.2f",
                                    contaOrigem.getNome(), saldoAtualOrigem)
                    );
                }
            }

            Transacao despesa = new Transacao("Movimentação para " + contaDestino.getNome(), valor, data, TipoCategoria.DESPESA, null, contaOrigem);
            transacaoDAO.save(despesa, conn);

            Transacao receita = new Transacao("Movimentação de " + contaOrigem.getNome(), valor, data, TipoCategoria.RECEITA, null, contaDestino);
            transacaoDAO.save(receita, conn);

            return null;
        });
    }

    public double getSaldoAtual(int contaId) {
        return executeRead(conn -> {
            Conta conta = contaDAO.findById(contaId, conn);
            if (conta == null) {
                throw new IllegalArgumentException("Conta não encontrada.");
            }
            double saldoCalculado = calcularSaldoAtual(conta, conn);
            if (conta.getTipo() == TipoConta.CARTAO_DE_CREDITO) {
                return -saldoCalculado;
            } else {
                return saldoCalculado;
            }
        });
    }

    private double calcularSaldoAtual(Conta conta, Connection conn) {
        if (conta == null) { throw new IllegalArgumentException("Conta não pode ser nula."); }
        List<Transacao> transacoes = transacaoDAO.findByContaId(conta.getId(), conn);
        double balancoTransacoes = 0.0;
        if (conta.getTipo() == TipoConta.CARTAO_DE_CREDITO) {
            for (Transacao t : transacoes) {
                if (t.getTipo() == TipoCategoria.DESPESA) {
                    balancoTransacoes += t.getValor();
                } else if (t.getTipo() == TipoCategoria.RECEITA) {
                    balancoTransacoes -= t.getValor();
                }
            }
        } else {
            for (Transacao t : transacoes) {
                if (t.getTipo() == TipoCategoria.RECEITA) {
                    balancoTransacoes += t.getValor();
                } else if (t.getTipo() == TipoCategoria.DESPESA) {
                    balancoTransacoes -= t.getValor();
                }
            }
        }
        return conta.getSaldoInicial() + balancoTransacoes;
    }

    public Map<LocalDate, Double> getPatrimonioEvolucao(LocalDate inicio, LocalDate fim) {
        return executeRead(conn -> {
            List<Conta> contas = contaDAO.findAll(conn);
            double patrimonioBase = contas.stream()
                    .mapToDouble(Conta::getSaldoInicial)
                    .sum();

            List<Transacao> todasTransacoes = transacaoDAO.findAll(conn);

            double balancoAteInicio = todasTransacoes.stream()
                    .filter(t -> t.getData().isBefore(inicio))
                    .mapToDouble(t -> t.getTipo() == TipoCategoria.RECEITA ? t.getValor() : -t.getValor())
                    .sum();

            double patrimonioCorrente = patrimonioBase + balancoAteInicio;

            Map<LocalDate, Double> mudancasDiarias = todasTransacoes.stream()
                    .filter(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim))
                    .collect(Collectors.groupingBy(
                            Transacao::getData,
                            Collectors.summingDouble(t -> t.getTipo() == TipoCategoria.RECEITA ? t.getValor() : -t.getValor())
                    ));

            Map<LocalDate, Double> evolucao = new java.util.TreeMap<>();
            LocalDate dataAtual = inicio;

            while (!dataAtual.isAfter(fim)) {
                patrimonioCorrente += mudancasDiarias.getOrDefault(dataAtual, 0.0);
                evolucao.put(dataAtual, patrimonioCorrente);
                dataAtual = dataAtual.plusDays(1);
            }
            return evolucao;
        });
    }

    public double getTotalReceitasPorConta(int contaId) {
        return executeRead(conn ->
                listarTransacoesPorConta(contaId).stream()
                        .filter(t -> t.getTipo() == TipoCategoria.RECEITA)
                        .mapToDouble(Transacao::getValor)
                        .sum()
        );
    }

    public double getTotalDespesasPorConta(int contaId) {
        return executeRead(conn ->
                listarTransacoesPorConta(contaId).stream()
                        .filter(t -> t.getTipo() == TipoCategoria.DESPESA)
                        .mapToDouble(Transacao::getValor)
                        .sum()
        );
    }

    // =================================================================================
    // MÉTODOS TEMPLATE (HELPERS PRIVADOS)
    // =================================================================================

    @FunctionalInterface
    private interface DatabaseReadOperation<T> {
        T execute(Connection conn) throws Exception;
    }

    @FunctionalInterface
    private interface DatabaseWriteOperation<T> {
        T execute(Connection conn) throws Exception;
    }

    private <T> T executeRead(DatabaseReadOperation<T> operation) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return operation.execute(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private <T> T executeWrite(DatabaseWriteOperation<T> operation) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = operation.execute(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new RuntimeException("Erro na transação: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão com o banco de dados.", e);
        }
    }
}