package com.controle.dao;

import com.controle.model.Categoria;
import com.controle.model.Conta;
import com.controle.model.TipoCategoria;
import com.controle.model.TransacaoRecorrente;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransacaoRecorrenteDAO extends AbstractDAO<TransacaoRecorrente, Integer> {

    private final CategoriaDAO categoriaDAO;
    private final ContaDAO contaDAO; // <-- NOVO

    public TransacaoRecorrenteDAO() {
        super();
        this.categoriaDAO = new CategoriaDAO();
        this.contaDAO = new ContaDAO(); // <-- NOVO
    }

    @Override
    public void save(TransacaoRecorrente tr) {
        // SQL ATUALIZADO
        String sql = "INSERT INTO transacoes_recorrentes (descricao, valor, tipo, categoria_id, conta_id, dia_do_mes, data_inicio, data_fim, data_ultimo_processamento) " +
                "OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tr.getDescricao());
            stmt.setDouble(2, tr.getValor());
            stmt.setString(3, tr.getTipo().name());
            stmt.setInt(4, tr.getCategoria().getId());
            stmt.setInt(5, tr.getConta().getId()); // <-- NOVO CAMPO
            stmt.setInt(6, tr.getDiaDoMes());
            stmt.setDate(7, Date.valueOf(tr.getDataInicio()));

            if (tr.getDataFim() != null) {
                stmt.setDate(8, Date.valueOf(tr.getDataFim()));
            } else {
                stmt.setNull(8, Types.DATE);
            }

            if (tr.getDataUltimoProcessamento() != null) {
                stmt.setDate(9, Date.valueOf(tr.getDataUltimoProcessamento()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    tr.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar transação recorrente: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar transação recorrente.", e);
        }
    }

    @Override
    public TransacaoRecorrente findById(Integer id) {
        String sql = "SELECT * FROM transacoes_recorrentes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransacaoRecorrente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transação recorrente por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transação recorrente.", e);
        }
        return null;
    }

    @Override
    public List<TransacaoRecorrente> findAll() {
        List<TransacaoRecorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes_recorrentes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToTransacaoRecorrente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as transações recorrentes: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações recorrentes.", e);
        }
        return lista;
    }

    @Override
    public void update(TransacaoRecorrente tr) {
        // SQL ATUALIZADO
        String sql = "UPDATE transacoes_recorrentes SET descricao = ?, valor = ?, tipo = ?, categoria_id = ?, conta_id = ?, " +
                "dia_do_mes = ?, data_inicio = ?, data_fim = ?, data_ultimo_processamento = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tr.getDescricao());
            stmt.setDouble(2, tr.getValor());
            stmt.setString(3, tr.getTipo().name());
            stmt.setInt(4, tr.getCategoria().getId());
            stmt.setInt(5, tr.getConta().getId()); // <-- NOVO CAMPO
            stmt.setInt(6, tr.getDiaDoMes());
            stmt.setDate(7, Date.valueOf(tr.getDataInicio()));

            if (tr.getDataFim() != null) {
                stmt.setDate(8, Date.valueOf(tr.getDataFim()));
            } else {
                stmt.setNull(8, Types.DATE);
            }

            if (tr.getDataUltimoProcessamento() != null) {
                stmt.setDate(9, Date.valueOf(tr.getDataUltimoProcessamento()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setInt(10, tr.getId()); // ID é o 10º parâmetro

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar transação recorrente: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar transação recorrente.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM transacoes_recorrentes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir transação recorrente: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir transação recorrente.", e);
        }
    }

    public List<TransacaoRecorrente> findAllAtivas(LocalDate dataReferencia) {
        List<TransacaoRecorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes_recorrentes " +
                "WHERE data_inicio <= ? AND (data_fim IS NULL OR data_fim >= ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataReferencia));
            stmt.setDate(2, Date.valueOf(dataReferencia));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTransacaoRecorrente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações recorrentes ativas: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações recorrentes ativas.", e);
        }
        return lista;
    }

    public List<TransacaoRecorrente> findAllByDescriptionLike(String termoBusca) {
        List<TransacaoRecorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes_recorrentes WHERE descricao LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + termoBusca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTransacaoRecorrente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações recorrentes por termo: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações recorrentes por termo.", e);
        }
        return lista;
    }

    private TransacaoRecorrente mapResultSetToTransacaoRecorrente(ResultSet rs) throws SQLException {
        // Usa o construtor vazio
        TransacaoRecorrente tr = new TransacaoRecorrente();
        tr.setId(rs.getInt("id"));
        tr.setDescricao(rs.getString("descricao"));
        tr.setValor(rs.getDouble("valor"));
        tr.setTipo(TipoCategoria.valueOf(rs.getString("tipo")));
        tr.setDiaDoMes(rs.getInt("dia_do_mes"));
        tr.setDataInicio(rs.getDate("data_inicio").toLocalDate());

        Date dataFimSQL = rs.getDate("data_fim");
        if (dataFimSQL != null) {
            tr.setDataFim(dataFimSQL.toLocalDate());
        } else {
            tr.setDataFim(null);
        }

        Date dataUltimoSQL = rs.getDate("data_ultimo_processamento");
        if (dataUltimoSQL != null) {
            tr.setDataUltimoProcessamento(dataUltimoSQL.toLocalDate());
        } else {
            tr.setDataUltimoProcessamento(null);
        }

        // Busca Categoria (não pode ser nula)
        int categoriaId = rs.getInt("categoria_id");
        Categoria cat = categoriaDAO.findById(categoriaId);
        tr.setCategoria(cat);

        // Busca Conta (não pode ser nula)
        int contaId = rs.getInt("conta_id");
        Conta conta = contaDAO.findById(contaId);
        tr.setConta(conta);

        return tr;
    }
}