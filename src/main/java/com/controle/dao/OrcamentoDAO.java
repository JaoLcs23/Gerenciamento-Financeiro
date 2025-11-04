package com.controle.dao;

import com.controle.model.Categoria;
import com.controle.model.Orcamento;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;

public class OrcamentoDAO extends AbstractDAO<Orcamento, Integer> {

    private CategoriaDAO categoriaDAO;

    public OrcamentoDAO(CategoriaDAO categoriaDAO) {
        super();
        this.categoriaDAO = categoriaDAO;
    }

    public void save(Orcamento orcamento, Connection conn) {
        String sql = "INSERT INTO orcamentos (categoria_id, valor_limite, mes, ano) " +
                "OUTPUT INSERTED.id VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orcamento.getCategoria().getId());
            stmt.setDouble(2, orcamento.getValorLimite());
            stmt.setInt(3, orcamento.getMes());
            stmt.setInt(4, orcamento.getAno());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    orcamento.setId(rs.getInt(1));
                }
            }
            System.out.println("Orçamento para '" + orcamento.getCategoria().getNome() + "' salvo com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar orçamento: " + e.getMessage());
            if (e.getMessage().contains("UQ_Categoria_Mes_Ano")) {
                throw new RuntimeException("Já existe um orçamento para esta categoria neste mês/ano.", e);
            }
            throw new RuntimeException("Erro ao salvar orçamento.", e);
        }
    }

    @Override
    public void save(Orcamento orcamento) {
        throw new UnsupportedOperationException("Use save(Orcamento, Connection)");
    }

    public void update(Orcamento orcamento, Connection conn) {
        String sql = "UPDATE orcamentos SET categoria_id = ?, valor_limite = ?, mes = ?, ano = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orcamento.getCategoria().getId());
            stmt.setDouble(2, orcamento.getValorLimite());
            stmt.setInt(3, orcamento.getMes());
            stmt.setInt(4, orcamento.getAno());
            stmt.setInt(5, orcamento.getId());
            stmt.executeUpdate();
            System.out.println("Orçamento para '" + orcamento.getCategoria().getNome() + "' atualizado.");
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar orçamento: " + e.getMessage());
            if (e.getMessage().contains("UQ_Categoria_Mes_Ano")) {
                throw new RuntimeException("Já existe um orçamento para esta categoria neste mês/ano.", e);
            }
            throw new RuntimeException("Erro ao atualizar orçamento.", e);
        }
    }

    @Override
    public void update(Orcamento orcamento) {
        throw new UnsupportedOperationException("Use update(Orcamento, Connection)");
    }

    public void delete(Integer id, Connection conn) {
        String sql = "DELETE FROM orcamentos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Orçamento ID " + id + " excluído.");
        } catch (SQLException e) {
            System.err.println("Erro ao excluir orçamento: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir orçamento.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Use delete(Integer, Connection)");
    }

    public Orcamento findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM orcamentos WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrcamento(rs, conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar orçamento por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar orçamento.", e);
        }
        return null;
    }

    @Override
    public Orcamento findById(Integer id) {
        throw new UnsupportedOperationException("Use findById(Integer, Connection)");
    }

    public List<Orcamento> findAll(Connection conn) {
        List<Orcamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM orcamentos ORDER BY ano, mes, categoria_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Passa a conexão para o método helper
                lista.add(mapResultSetToOrcamento(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os orçamentos: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar orçamentos.", e);
        }
        return lista;
    }

    @Override
    public List<Orcamento> findAll() {
        throw new UnsupportedOperationException("Use findAll(Connection)");
    }

    public Orcamento findByCategoriaMesAno(int categoriaId, int mes, int ano, Connection conn) {
        String sql = "SELECT * FROM orcamentos WHERE categoria_id = ? AND mes = ? AND ano = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);
            stmt.setInt(2, mes);
            stmt.setInt(3, ano);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrcamento(rs, conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar orçamento por Categoria/Mês/Ano: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar orçamento.", e);
        }
        return null;
    }

    private Orcamento mapResultSetToOrcamento(ResultSet rs, Connection conn) throws SQLException {
        int id = rs.getInt("id");
        int categoriaId = rs.getInt("categoria_id");
        double valorLimite = rs.getDouble("valor_limite");
        int mes = rs.getInt("mes");
        int ano = rs.getInt("ano");

        Categoria categoria = categoriaDAO.findById(categoriaId, conn);

        return new Orcamento(id, categoria, valorLimite, mes, ano);
    }

    public List<Orcamento> findByMesAno(Integer mes, Integer ano, Connection conn) {
        List<Orcamento> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM orcamentos");
        boolean hasMes = (mes != null && mes > 0);
        boolean hasAno = (ano != null && ano > 0);

        if (hasMes || hasAno) {
            sql.append(" WHERE ");
        }
        if (hasMes) {
            sql.append("mes = ?");
        }
        if (hasAno) {
            if (hasMes) sql.append(" AND ");
            sql.append("ano = ?");
        }
        sql.append(" ORDER BY ano, mes, categoria_id");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasMes) {
                stmt.setInt(paramIndex++, mes);
            }
            if (hasAno) {
                stmt.setInt(paramIndex, ano);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToOrcamento(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar orçamentos por Mês/Ano: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar orçamentos.", e);
        }
        return lista;
    }
}