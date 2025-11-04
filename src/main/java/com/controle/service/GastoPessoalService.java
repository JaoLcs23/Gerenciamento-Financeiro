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

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (categoriaDAO.findByNome(nome.trim(), conn) != null) {
                    throw new IllegalArgumentException("Categoria com o nome '" + nome + "' já existe.");
                }

                Categoria novaCategoria = new Categoria(nome.trim(), tipo);

                categoriaDAO.save(novaCategoria, conn);

                conn.commit();
                return novaCategoria;

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao adicionar categoria: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão com o banco de dados.", e);
        }
    }

    public Categoria buscarCategoriaPorId(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoriaDAO.findById(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar categoria.", e);
        }
    }

    public Categoria buscarCategoriaPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) { return null; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoriaDAO.findByNome(nome.trim(), conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar categoria.", e);
        }
    }

    public List<Categoria> listarTodasCategorias() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoriaDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao listar categorias.", e);
        }
    }

    public List<Categoria> listarCategoriasPorTermo(String termoBusca) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                return categoriaDAO.findAll(conn);
            }
            return categoriaDAO.findAllByNomeLike(termoBusca.trim(), conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar categorias.", e);
        }
    }

    public void atualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() <= 0) { throw new IllegalArgumentException("Categoria inválida para atualização."); }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Categoria existente = categoriaDAO.findByNome(categoria.getNome(), conn);
                if (existente != null && existente.getId() != categoria.getId()) {
                    throw new IllegalArgumentException("Outra categoria já existe com o nome '" + categoria.getNome() + "'.");
                }
                categoriaDAO.update(categoria, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao atualizar categoria: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao atualizar categoria.", e);
        }
    }

    public void excluirCategoria(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                categoriaDAO.delete(id, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao excluir categoria: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao excluir categoria.", e);
        }
    }

    public Transacao adicionarTransacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, String categoriaNome, String contaNome) {
        if (descricao == null || descricao.trim().isEmpty()) { throw new IllegalArgumentException("A descricao da transação não pode ser vazia."); }
        if (valor <= 0) { throw new IllegalArgumentException("O valor da transação deve ser positivo."); }
        if (data == null || data.isAfter(LocalDate.now())) { throw new IllegalArgumentException("A data da transação não pode ser nula ou futura."); }
        if (tipo == null) { throw new IllegalArgumentException("O tipo da transação não pode ser nulo."); }
        if (contaNome == null || contaNome.trim().isEmpty()) { throw new IllegalArgumentException("A conta é obrigatória para a transação."); }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
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

                conn.commit();
                return novaTransacao;

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao adicionar transação: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao adicionar transação.", e);
        }
    }

    public Transacao buscarTransacaoPorId(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transacaoDAO.findById(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar transação.", e);
        }
    }

    public List<Transacao> listarTodasTransacoes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transacaoDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao listar transações.", e);
        }
    }

    public List<Transacao> listarTransacoesPorTermo(String termoBusca) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                return transacaoDAO.findAll(conn);
            }
            return transacaoDAO.findAllByDescriptionLike(termoBusca.trim(), conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar transações.", e);
        }
    }

    public List<Transacao> listarTransacoesPorConta(int contaId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transacaoDAO.findByContaId(contaId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar transações por conta.", e);
        }
    }

    public void atualizarTransacao(Transacao transacao, String novaCategoriaNome, String novaContaNome) {
        if (transacao == null || transacao.getId() <= 0) { throw new IllegalArgumentException("Transação inválida para atualização."); }
        if (transacao.getValor() <= 0) { throw new IllegalArgumentException("O valor da transação deve ser positivo."); }
        if (novaContaNome == null || novaContaNome.trim().isEmpty()) { throw new IllegalArgumentException("A conta é obrigatória para a transação."); }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
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
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao atualizar transação: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao atualizar transação.", e);
        }
    }

    public void excluirTransacao(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transacaoDAO.delete(id, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao excluir transação: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao excluir transação.", e);
        }
    }

    private List<Transacao> getTransacoesPeriodo(LocalDate inicio, LocalDate fim, Connection conn) {
        return transacaoDAO.findAll(conn).stream()
                .filter(t -> !t.getData().isBefore(inicio) && !t.getData().isAfter(fim))
                .collect(Collectors.toList());
    }

    public double calcularBalancoTotal(LocalDate inicio, LocalDate fim) {
        try (Connection conn = DatabaseConnection.getConnection()) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao calcular balanço.", e);
        }
    }

    public double calcularTotalReceitas(LocalDate inicio, LocalDate fim) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);
            return transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.RECEITA && t.getCategoria() != null)
                    .mapToDouble(Transacao::getValor)
                    .sum();
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao calcular receitas.", e);
        }
    }

    public double calcularTotalDespesas(LocalDate inicio, LocalDate fim) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);
            return transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.DESPESA && t.getCategoria() != null)
                    .mapToDouble(Transacao::getValor)
                    .sum();
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao calcular despesas.", e);
        }
    }

    public Map<String, Double> calcularDespesasPorCategoria(LocalDate inicio, LocalDate fim) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Transacao> transacoesNoPeriodo = getTransacoesPeriodo(inicio, fim, conn);

            return transacoesNoPeriodo.stream()
                    .filter(t -> t.getTipo() == TipoCategoria.DESPESA && t.getCategoria() != null)
                    .collect(Collectors.groupingBy(
                            t -> t.getCategoria().getNome(),
                            Collectors.summingDouble(Transacao::getValor)
                    ));
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao calcular despesas por categoria.", e);
        }
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

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transacaoRecorrenteDAO.save(tr, conn);
                conn.commit();
                return tr;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao adicionar transação recorrente: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao adicionar transação recorrente.", e);
        }
    }

    public TransacaoRecorrente buscarTransacaoRecorrentePorId(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transacaoRecorrenteDAO.findById(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar transação recorrente.", e);
        }
    }

    public List<TransacaoRecorrente> listarTodasTransacoesRecorrentes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transacaoRecorrenteDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao listar transações recorrentes.", e);
        }
    }

    public List<TransacaoRecorrente> listarTransacoesRecorrentesPorTermo(String termoBusca) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (termoBusca == null || termoBusca.trim().isEmpty()) {
                return transacaoRecorrenteDAO.findAll(conn);
            }
            return transacaoRecorrenteDAO.findAllByDescriptionLike(termoBusca.trim(), conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar transações recorrentes.", e);
        }
    }

    public void atualizarTransacaoRecorrente(TransacaoRecorrente tr) {
        if (tr == null || tr.getId() <= 0) { throw new IllegalArgumentException("Transação recorrente inválida para atualização."); }
        if (tr.getCategoria() == null) { throw new IllegalArgumentException("Categoria é obrigatória."); }
        if (tr.getConta() == null) { throw new IllegalArgumentException("Conta é obrigatória."); }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transacaoRecorrenteDAO.update(tr, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao atualizar transação recorrente: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao atualizar transação recorrente.", e);
        }
    }

    public void excluirTransacaoRecorrente(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transacaoRecorrenteDAO.delete(id, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao excluir transação recorrente: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao excluir transação recorrente.", e);
        }
    }

    public Orcamento adicionarOrcamento(Orcamento orcamento) {
        if (orcamento == null) throw new IllegalArgumentException("Orçamento não pode ser nulo.");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Orcamento existente = orcamentoDAO.findByCategoriaMesAno(
                        orcamento.getCategoria().getId(), orcamento.getMes(), orcamento.getAno(), conn
                );
                if (existente != null) {
                    throw new RuntimeException("Já existe um orçamento para esta categoria neste mês/ano.");
                }

                orcamentoDAO.save(orcamento, conn);
                conn.commit();
                return orcamento;
            } catch (Exception e) {
                conn.rollback();
                if (e.getMessage().contains("UQ_Categoria_Mes_Ano")) {
                    throw new RuntimeException("Já existe um orçamento para esta categoria neste mês/ano.", e);
                }
                throw new RuntimeException("Erro ao adicionar orçamento: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao adicionar orçamento.", e);
        }
    }

    public void atualizarOrcamento(Orcamento orcamento) {
        if (orcamento == null || orcamento.getId() <= 0) { throw new IllegalArgumentException("Orçamento inválido para atualização."); }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Orcamento existente = orcamentoDAO.findByCategoriaMesAno(
                        orcamento.getCategoria().getId(), orcamento.getMes(), orcamento.getAno(), conn
                );
                if (existente != null && existente.getId() != orcamento.getId()) {
                    throw new RuntimeException("Já existe outro orçamento para esta categoria neste mês/ano.");
                }

                orcamentoDAO.update(orcamento, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e.getMessage().contains("UQ_Categoria_Mes_Ano")) {
                    throw new RuntimeException("Já existe um orçamento para esta categoria neste mês/ano.", e);
                }
                throw new RuntimeException("Erro ao atualizar orçamento: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao atualizar orçamento.", e);
        }
    }

    public void excluirOrcamento(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                orcamentoDAO.delete(id, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao excluir orçamento: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao excluir orçamento.", e);
        }
    }

    public List<Orcamento> listarOrcamentosPorPeriodo(Integer mes, Integer ano) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return orcamentoDAO.findByMesAno(mes, ano, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao listar orçamentos.", e);
        }
    }

    public Orcamento buscarOrcamentoPorCategoriaMesAno(int categoriaId, int mes, int ano) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return orcamentoDAO.findByCategoriaMesAno(categoriaId, mes, ano, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar orçamento.", e);
        }
    }

    public Orcamento getOrcamentoCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) { return null; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            return orcamentoDAO.findByCategoriaMesAno(categoria.getId(), mes, ano, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar orçamento.", e);
        }
    }

    public double getGastoAtualCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) { return 0.0; }
        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        Map<String, Double> despesas = calcularDespesasPorCategoria(inicioMes, fimMes);
        return despesas.getOrDefault(categoria.getNome(), 0.0);
    }

    public Conta adicionarConta(Conta conta) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (contaDAO.findByNome(conta.getNome(), conn) != null) {
                    throw new RuntimeException("Já existe uma conta com este nome.");
                }
                contaDAO.save(conta, conn);
                conn.commit();
                return conta;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao adicionar conta: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao adicionar conta.", e);
        }
    }

    public void atualizarConta(Conta conta) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Conta existente = contaDAO.findByNome(conta.getNome(), conn);
                if (existente != null && existente.getId() != conta.getId()) {
                    throw new RuntimeException("Já existe outra conta com este nome.");
                }
                contaDAO.update(conta, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao atualizar conta: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao atualizar conta.", e);
        }
    }

    public void excluirConta(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                contaDAO.delete(id, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erro ao excluir conta: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao excluir conta.", e);
        }
    }

    public List<Conta> listarTodasContas() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return contaDAO.findAll(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao listar contas.", e);
        }
    }

    public List<Conta> listarContasPorTermo(String termo) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (termo == null || termo.trim().isEmpty()) {
                return contaDAO.findAll(conn);
            }
            return contaDAO.findAllByNomeLike(termo.trim(), conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao buscar contas.", e);
        }
    }

    public void transferirFundos(Conta contaOrigem, Conta contaDestino, double valor, LocalDate data) {
        if (contaOrigem == null || contaDestino == null) {
            throw new IllegalArgumentException("As contas de origem e destino são obrigatórias.");
        }
        if (contaOrigem.getId() == contaDestino.getId()) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser a mesma.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da movimentação deve ser positivo.");
        }
        if (data == null || data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data não pode ser nula ou futura.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (contaOrigem.getTipo() != TipoConta.CARTAO_DE_CREDITO) {
                    double saldoAtualOrigem = calcularSaldoAtual(contaOrigem, conn);

                    if (saldoAtualOrigem < valor) {
                        throw new IllegalArgumentException(
                                String.format("Saldo insuficiente na conta '%s'. Saldo atual: R$ %.2f",
                                        contaOrigem.getNome(), saldoAtualOrigem)
                        );
                    }
                }

                Transacao despesa = new Transacao(
                        "Movimentação para " + contaDestino.getNome(),
                        valor,
                        data,
                        TipoCategoria.DESPESA,
                        null,
                        contaOrigem
                );
                transacaoDAO.save(despesa, conn);

                Transacao receita = new Transacao(
                        "Movimentação de " + contaOrigem.getNome(),
                        valor,
                        data,
                        TipoCategoria.RECEITA,
                        null,
                        contaDestino
                );
                transacaoDAO.save(receita, conn);

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                System.err.println("Erro na movimentação, transação revertida.");
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão com o banco de dados.", e);
        }
    }

    public double getSaldoAtual(int contaId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
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

        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao calcular saldo.", e);
        }
    }

    private double calcularSaldoAtual(Conta conta, Connection conn) {
        if (conta == null) {
            throw new IllegalArgumentException("Conta não pode ser nula.");
        }

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
        try (Connection conn = DatabaseConnection.getConnection()) {

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

        } catch (SQLException e) {
            throw new RuntimeException("Erro de conexão ao calcular evolução do patrimônio.", e);
        }
    }
}