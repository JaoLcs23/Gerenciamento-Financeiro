package com.controle.dao;

import com.controle.model.Conta;
import com.controle.model.TipoConta;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO extends AbstractDAO<Conta, Integer> {

    public ContaDAO() {
        super();
    }

    @Override
    public void save(Conta conta) {
        String sql = "INSERT INTO contas (nome, saldo_inicial, tipo) OUTPUT INSERTED.id VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, conta.getNome());
            stmt.setDouble(2, conta.getSaldoInicial());
            stmt.setString(3, conta.getTipo().name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    conta.setId(rs.getInt(1));
                }
            }
            System.out.println("Conta '" + conta.getNome() + "' salva com sucesso. ID: " + conta.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao salvar conta: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar conta.", e);
        }
    }

    @Override
    public Conta findById(Integer id) {
        String sql = "SELECT * FROM contas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConta(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar conta por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar conta.", e);
        }
        return null;
    }

    @Override
    public List<Conta> findAll() {
        List<Conta> contas = new ArrayList<>();
        String sql = "SELECT * FROM contas";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                contas.add(mapResultSetToConta(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as contas: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar contas.", e);
        }
        return contas;
    }

    @Override
    public void update(Conta conta) {
        String sql = "UPDATE contas SET nome = ?, saldo_inicial = ?, tipo = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, conta.getNome());
            stmt.setDouble(2, conta.getSaldoInicial());
            stmt.setString(3, conta.getTipo().name());
            stmt.setInt(4, conta.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("Nenhuma conta encontrada com ID: " + conta.getId() + " para atualizar.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar conta: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar conta.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM contas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir conta: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir conta.", e);
        }
    }

    public Conta findByNome(String nome) {
        String sql = "SELECT * FROM contas WHERE nome = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConta(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar conta por nome: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar conta por nome.", e);
        }
        return null;
    }

    // Busca contas cujos nomes contêm o termo de busca
    public List<Conta> findAllByNomeLike(String termoBusca) {
        List<Conta> contas = new ArrayList<>();
        String sql = "SELECT * FROM contas WHERE nome LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + termoBusca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contas.add(mapResultSetToConta(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar contas por termo: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar contas por termo.", e);
        }
        return contas;
    }

    // Método auxiliar
    private Conta mapResultSetToConta(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        double saldoInicial = rs.getDouble("saldo_inicial");
        TipoConta tipo = TipoConta.valueOf(rs.getString("tipo"));
        return new Conta(id, nome, saldoInicial, tipo);
    }
}