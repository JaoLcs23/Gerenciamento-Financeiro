package com.controle.dao;

import com.controle.model.Categoria;
import com.controle.model.Conta;
import com.controle.model.Transacao;
import com.controle.model.TipoCategoria;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;

public class TransacaoDAO extends AbstractDAO<Transacao, Integer> {

    private final CategoriaDAO categoriaDAO;
    private final ContaDAO contaDAO;

    public TransacaoDAO(CategoriaDAO categoriaDAO, ContaDAO contaDAO) {
        super();
        this.categoriaDAO = categoriaDAO;
        this.contaDAO = contaDAO;
    }

    public void save(Transacao transacao, Connection conn) {
        String sql = "INSERT INTO transacoes (descricao, valor, data, tipo, categoria_id, conta_id) OUTPUT INSERTED.id VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transacao.getDescricao());
            stmt.setDouble(2, transacao.getValor());
            stmt.setDate(3, Date.valueOf(transacao.getData()));
            stmt.setString(4, transacao.getTipo().name());

            if (transacao.getCategoria() != null) {
                stmt.setInt(5, transacao.getCategoria().getId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            if (transacao.getConta() != null) {
                stmt.setInt(6, transacao.getConta().getId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    transacao.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar transação: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar transação.", e);
        }
    }

    public Transacao findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM transacoes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransacao(rs, conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transação por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transação.", e);
        }
        return null;
    }

    public List<Transacao> findAll(Connection conn) {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT * FROM transacoes ORDER BY data DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transacoes.add(mapResultSetToTransacao(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as transações: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações.", e);
        }
        return transacoes;
    }

    public void update(Transacao transacao, Connection conn) {
        String sql = "UPDATE transacoes SET descricao = ?, valor = ?, data = ?, tipo = ?, categoria_id = ?, conta_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, transacao.getDescricao());
            stmt.setDouble(2, transacao.getValor());
            stmt.setDate(3, Date.valueOf(transacao.getData()));
            stmt.setString(4, transacao.getTipo().name());

            if (transacao.getCategoria() != null) {
                stmt.setInt(5, transacao.getCategoria().getId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            if (transacao.getConta() != null) {
                stmt.setInt(6, transacao.getConta().getId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            stmt.setInt(7, transacao.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar transação: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar transação.", e);
        }
    }

    public void delete(Integer id, Connection conn) {
        String sql = "DELETE FROM transacoes WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir transação: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir transação.", e);
        }
    }

    public List<Transacao> findAllByDescriptionLike(String termoBusca, Connection conn) {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT * FROM transacoes WHERE descricao LIKE ? ORDER BY data DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termoBusca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(mapResultSetToTransacao(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações por termo na descrição: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações por termo.", e);
        }
        return transacoes;
    }

    public List<Transacao> findByContaId(int contaId, Connection conn) {
        List<Transacao> transacoes = new ArrayList<>();
        String sql = "SELECT * FROM transacoes WHERE conta_id = ? ORDER BY data DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(mapResultSetToTransacao(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar transações por conta ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar transações por conta.", e);
        }
        return transacoes;
    }

    private Transacao mapResultSetToTransacao(ResultSet rs, Connection conn) throws SQLException {
        int id = rs.getInt("id");
        String descricao = rs.getString("descricao");
        double valor = rs.getDouble("valor");
        LocalDate data = rs.getDate("data").toLocalDate();
        TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));

        Integer categoriaId = rs.getObject("categoria_id", Integer.class);
        Categoria categoria = null;
        if (categoriaId != null) {
            categoria = categoriaDAO.findById(categoriaId, conn);
        }

        Integer contaId = rs.getObject("conta_id", Integer.class);
        Conta conta = null;
        if (contaId != null) {
            conta = contaDAO.findById(contaId, conn);
        }

        return new Transacao(id, descricao, valor, data, tipo, categoria, conta);
    }
}