package com.controle.service;

import com.controle.dao.CategoriaDAO;
import com.controle.dao.TransacaoDAO;
import com.controle.model.Categoria;
import com.controle.model.Transacao;
import com.controle.model.TipoCategoria;

// Imports da funcionalidade anterior
import com.controle.model.TransacaoRecorrente;
import com.controle.dao.TransacaoRecorrenteDAO;

// --- CORREÇÃO: Imports que faltavam ---
import com.controle.model.Orcamento;
import com.controle.dao.OrcamentoDAO;
// -------------------------------------

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Camada de serviço para gerenciar a lógica de negócio do Sistema de Controle de Gastos Pessoais
 */
public class GastoPessoalService {

    private CategoriaDAO categoriaDAO;
    private TransacaoDAO transacaoDAO;
    private TransacaoRecorrenteDAO transacaoRecorrenteDAO;
    private OrcamentoDAO orcamentoDAO; // NOVO

    public GastoPessoalService() {
        this.categoriaDAO = new CategoriaDAO();
        this.transacaoDAO = new TransacaoDAO();
        this.transacaoRecorrenteDAO = new TransacaoRecorrenteDAO();
        this.orcamentoDAO = new OrcamentoDAO(); // NOVO
    }

    // Métodos para Categoria
    public Categoria adicionarCategoria(String nome, TipoCategoria tipo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria não pode ser vazio.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da categoria não pode ser nulo.");
        }
        if (categoriaDAO.findByNome(nome.trim()) != null) {
            throw new IllegalArgumentException("Categoria com o nome '" + nome + "' já existe.");
        }
        Categoria novaCategoria = new Categoria(nome.trim(), tipo);
        categoriaDAO.save(novaCategoria);
        return novaCategoria;
    }
    public Categoria buscarCategoriaPorId(int id) {
        return categoriaDAO.findById(id);
    }
    public Categoria buscarCategoriaPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return categoriaDAO.findByNome(nome.trim());
    }
    public List<Categoria> listarTodasCategorias() {
        return categoriaDAO.findAll();
    }
    public List<Categoria> listarCategoriasPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return categoriaDAO.findAll();
        }
        return categoriaDAO.findAllByNomeLike(termoBusca.trim());
    }
    public void atualizarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() <= 0) {
            throw new IllegalArgumentException("Categoria inválida para atualização.");
        }
        Categoria existente = categoriaDAO.findByNome(categoria.getNome());
        if (existente != null && existente.getId() != categoria.getId()) {
            throw new IllegalArgumentException("Outra categoria já existe com o nome '" + categoria.getNome() + "'.");
        }
        categoriaDAO.update(categoria);
    }
    public void excluirCategoria(int id) {
        categoriaDAO.delete(id);
    }

    // --- Métodos para Transação (Original) ---
    public Transacao adicionarTransacao(String descricao, double valor, LocalDate data, TipoCategoria tipo, String categoriaNome) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descricao da transação não pode ser vazia.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser positivo.");
        }
        if (data == null || data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data da transação não pode ser nula ou futura.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da transação não pode ser nulo.");
        }

        Categoria categoria = null;
        if (categoriaNome != null && !categoriaNome.trim().isEmpty()) {
            categoria = categoriaDAO.findByNome(categoriaNome.trim());
            if (categoria == null) {
                throw new IllegalArgumentException("Categoria '" + categoriaNome + "' não encontrada. Crie-a primeiro.");
            }
            if (categoria.getTipo() != tipo) {
                throw new IllegalArgumentException("O tipo da transação (" + tipo + ") não corresponde ao tipo da categoria '" + categoriaNome + "' (" + categoria.getTipo() + ").");
            }
        }

        Transacao novaTransacao = new Transacao(descricao.trim(), valor, data, tipo, categoria);
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
    public List<Transacao> listarTransacoesPorTipo(TipoCategoria tipo) {
        return transacaoDAO.findAll().stream()
                .filter(t -> t.getTipo() == tipo)
                .collect(Collectors.toList());
    }
    public void atualizarTransacao(Transacao transacao, String novaCategoriaNome) {
        if (transacao == null || transacao.getId() <= 0) {
            throw new IllegalArgumentException("Transação inválida para atualização.");
        }
        // ... (validações) ...
        Categoria categoriaAssociada = null;
        if (novaCategoriaNome != null && !novaCategoriaNome.trim().isEmpty()) {
            categoriaAssociada = categoriaDAO.findByNome(novaCategoriaNome.trim());
            if (categoriaAssociada == null) {
                throw new IllegalArgumentException("Categoria '" + novaCategoriaNome + "' não encontrada para a transação.");
            }
            if (categoriaAssociada.getTipo() != transacao.getTipo()) {
                throw new IllegalArgumentException("O tipo da transação (" + transacao.getTipo() + ") não corresponde ao tipo da categoria '" + categoriaAssociada.getNome() + "' (" + categoriaAssociada.getTipo() + ").");
            }
        }
        transacao.setCategoria(categoriaAssociada);
        transacaoDAO.update(transacao);
    }
    public void excluirTransacao(int id) {
        transacaoDAO.delete(id);
    }

    // Métodos para Relatórios
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


    // Métodos (Transação Recorrente)
    public void processarTransacoesRecorrentes() {
        LocalDate hoje = LocalDate.now();
        List<TransacaoRecorrente> recorrentesAtivas = transacaoRecorrenteDAO.findAllAtivas(hoje);
        System.out.println("PROCESSANDO TRANSAÇÕES RECORRENTES: " + recorrentesAtivas.size() + " ativas encontradas.");

        for (TransacaoRecorrente tr : recorrentesAtivas) {
            LocalDate dataLancamento;
            try {
                dataLancamento = LocalDate.of(hoje.getYear(), hoje.getMonth(), tr.getDiaDoMes());
            } catch (Exception e) {
                dataLancamento = hoje.withDayOfMonth(hoje.lengthOfMonth());
            }

            boolean ehHoraDeLancar = !hoje.isBefore(dataLancamento);
            LocalDate ultimoProcessamento = tr.getDataUltimoProcessamento();
            boolean jaProcessadoEsteMes = ultimoProcessamento != null &&
                    ultimoProcessamento.getYear() == hoje.getYear() &&
                    ultimoProcessamento.getMonth() == hoje.getMonth();
            boolean antesDoInicio = dataLancamento.isBefore(tr.getDataInicio());

            if (ehHoraDeLancar && !jaProcessadoEsteMes && !antesDoInicio) {
                try {
                    System.out.println("LANÇANDO: " + tr.getDescricao());
                    adicionarTransacao(tr.getDescricao(), tr.getValor(), dataLancamento, tr.getTipo(), tr.getCategoria().getNome());
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
        transacaoRecorrenteDAO.save(tr);
        return tr;
    }
    public TransacaoRecorrente buscarTransacaoRecorrentePorId(int id) {
        return transacaoRecorrenteDAO.findById(id);
    }
    public List<TransacaoRecorrente> listarTodasTransacoesRecorrentes() {
        return transacaoRecorrenteDAO.findAll();
    }
    public List<TransacaoRecorrente> listarTransacoesRecorrentesPorTermo(String termoBusca) {
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            return transacaoRecorrenteDAO.findAll();
        }
        return transacaoRecorrenteDAO.findAllByDescriptionLike(termoBusca.trim());
    }
    public void atualizarTransacaoRecorrente(TransacaoRecorrente tr) {
        if (tr == null || tr.getId() <= 0) {
            throw new IllegalArgumentException("Transação recorrente inválida para atualização.");
        }
        transacaoRecorrenteDAO.update(tr);
    }
    public void excluirTransacaoRecorrente(int id) {
        transacaoRecorrenteDAO.delete(id);
    }

    // MÉTODOS PARA ORÇAMENTO

    public Orcamento adicionarOrcamento(Orcamento orcamento) {
        if (orcamento == null) throw new IllegalArgumentException("Orçamento não pode ser nulo.");
        // O DAO já vai tratar a exceção de duplicata (UQ_Categoria_Mes_Ano)
        orcamentoDAO.save(orcamento);
        return orcamento;
    }

    public void atualizarOrcamento(Orcamento orcamento) {
        if (orcamento == null || orcamento.getId() <= 0) {
            throw new IllegalArgumentException("Orçamento inválido para atualização.");
        }
        orcamentoDAO.update(orcamento);
    }

    public void excluirOrcamento(int id) {
        orcamentoDAO.delete(id);
    }

    public List<Orcamento> listarOrcamentosPorPeriodo(Integer mes, Integer ano) {
        return orcamentoDAO.findByMesAno(mes, ano);
    }

    public Orcamento buscarOrcamentoPorCategoriaMesAno(int categoriaId, int mes, int ano) {
        return orcamentoDAO.findByCategoriaMesAno(categoriaId, mes, ano);
    }

    /**
     * Busca o orçamento de uma categoria específica em um mês/ano.
     * Retorna null se não houver orçamento definido.
     */
    public Orcamento getOrcamentoCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            return null;
        }
        return orcamentoDAO.findByCategoriaMesAno(categoria.getId(), mes, ano);
    }

    /**
     * Calcula o total já gasto em uma categoria específica em um mês/ano.
     */
    public double getGastoAtualCategoria(Categoria categoria, int mes, int ano) {
        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            return 0.0;
        }

        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        Map<String, Double> despesas = calcularDespesasPorCategoria(inicioMes, fimMes);

        // Retorna o valor do mapa para a categoria, ou 0.0 se não houver gastos
        return despesas.getOrDefault(categoria.getNome(), 0.0);
    }
}