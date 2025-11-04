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
import java.sql.Connection;

public class TransacaoRecorrenteDAO extends AbstractDAO<TransacaoRecorrente, Integer> {

    private final CategoriaDAO categoriaDAO;
    private final ContaDAO contaDAO;

    public TransacaoRecorrenteDAO(CategoriaDAO categoriaDAO, ContaDAO contaDAO) {
        super();
        this.categoriaDAO = categoriaDAO;
        this.contaDAO = contaDAO;
    }

    public void save(TransacaoRecorrente tr, Connection conn) {
        String sql = "INSERT INTO transacoes_recorrentes (descricao, valor, tipo, categoria_id, conta_id, dia_do_mes, data_inicio, data_fim, data_ultimo_processamento) " +
                "OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tr.getDescricao());
            stmt.setDouble(2, tr.getValor());
            stmt.setString(3, tr.getTipo().name());
            stmt.setInt(4, tr.getCategoria().getId());
            stmt.setInt(5, tr.getConta().getId());
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
    public void save(TransacaoRecorrente tr) {
        throw new UnsupportedOperationException("Use save(TransacaoRecorrente, Connection)");
    }

    public TransacaoRecorrente findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM transacoes_recorrentes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransacaoRecorrente(rs, conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transação recorrente por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transação recorrente.", e);
        }
        return null;
    }

    @Override
    public TransacaoRecorrente findById(Integer id) {
        throw new UnsupportedOperationException("Use findById(Integer, Connection)");
    }

    public List<TransacaoRecorrente> findAll(Connection conn) {
        List<TransacaoRecorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes_recorrentes";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSetToTransacaoRecorrente(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as transações recorrentes: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações recorrentes.", e);
        }
        return lista;
    }

    @Override
    public List<TransacaoRecorrente> findAll() {
        throw new UnsupportedOperationException("Use findAll(Connection)");
    }

    public void update(TransacaoRecorrente tr, Connection conn) {
        String sql = "UPDATE transacoes_recorrentes SET descricao = ?, valor = ?, tipo = ?, categoria_id = ?, conta_id = ?, " +
                "dia_do_mes = ?, data_inicio = ?, data_fim = ?, data_ultimo_processamento = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tr.getDescricao());
            stmt.setDouble(2, tr.getValor());
            stmt.setString(3, tr.getTipo().name());
            stmt.setInt(4, tr.getCategoria().getId());
            stmt.setInt(5, tr.getConta().getId());
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

            stmt.setInt(10, tr.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar transação recorrente: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar transação recorrente.", e);
        }
    }

    @Override
    public void update(TransacaoRecorrente tr) {
        throw new UnsupportedOperationException("Use update(TransacaoRecorrente, Connection)");
    }

    public void delete(Integer id, Connection conn) {
        String sql = "DELETE FROM transacoes_recorrentes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir transação recorrente: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir transação recorrente.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Use delete(Integer, Connection)");
    }

    public List<TransacaoRecorrente> findAllAtivas(LocalDate dataReferencia, Connection conn) {
        List<TransacaoRecorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes_recorrentes " +
                "WHERE data_inicio <= ? AND (data_fim IS NULL OR data_fim >= ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(dataReferencia));
            stmt.setDate(2, Date.valueOf(dataReferencia));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTransacaoRecorrente(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações recorrentes ativas: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações recorrentes ativas.", e);
        }
        return lista;
    }

    public List<TransacaoRecorrente> findAllByDescriptionLike(String termoBusca, Connection conn) {
        List<TransacaoRecorrente> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes_recorrentes WHERE descricao LIKE ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termoBusca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToTransacaoRecorrente(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações recorrentes por termo: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações recorrentes por termo.", e);
        }
        return lista;
    }

    private TransacaoRecorrente mapResultSetToTransacaoRecorrente(ResultSet rs, Connection conn) throws SQLException {
        TransacaoRecorrente tr = new TransacaoRecorrente();
        tr.setId(rs.getInt("id"));
        tr.setDescricao(rs.getString("descricao"));
        tr.setValor(rs.getDouble("valor"));
        tr.setTipo(TipoCategoria.valueOf(rs.getString("tipo")));
        tr.setDiaDoMes(rs.getInt("dia_do_mes"));
        tr.setDataInicio(rs.getDate("data_inicio").toLocalDate());

        Date dataFimSQL = rs.getDate("data_fim");
        tr.setDataFim(dataFimSQL != null ? dataFimSQL.toLocalDate() : null);

        Date dataUltimoSQL = rs.getDate("data_ultimo_processamento");
        tr.setDataUltimoProcessamento(dataUltimoSQL != null ? dataUltimoSQL.toLocalDate() : null);

        int categoriaId = rs.getInt("categoria_id");
        Categoria cat = categoriaDAO.findById(categoriaId, conn);
        tr.setCategoria(cat);

        int contaId = rs.getInt("conta_id");
        Conta conta = contaDAO.findById(contaId, conn);
        tr.setConta(conta);

        return tr;
    }
}