package com.controle.dao;

import com.controle.model.Categoria;
import com.controle.model.TipoCategoria;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;

public class CategoriaDAO extends AbstractDAO<Categoria, Integer> {

    public CategoriaDAO() {
    }

    public void save(Categoria categoria, Connection conn) {
        String sql = "INSERT INTO categorias (nome, tipo) OUTPUT INSERTED.id VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getTipo().name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    categoria.setId(rs.getInt(1));
                }
            }
            System.out.println("Categoria '" + categoria.getNome() + "' salva com sucesso. ID: " + categoria.getId());
        } catch (SQLException e) {
            System.err.println("Erro ao salvar categoria: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar categoria.", e);
        }
    }

    @Override
    public void save(Categoria categoria) {
        throw new UnsupportedOperationException("Use save(Categoria, Connection)");
    }

    public Categoria findById(Integer id, Connection conn) {
        String sql = "SELECT id, nome, tipo FROM categorias WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    return new Categoria(id, nome, tipo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categoria.", e);
        }
        return null;
    }

    @Override
    public Categoria findById(Integer id) {
        throw new UnsupportedOperationException("Use findById(Integer, Connection)");
    }

    public List<Categoria> findAll(Connection conn) {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nome, tipo FROM categorias";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                categorias.add(new Categoria(id, nome, tipo));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as categorias: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categorias.", e);
        }
        return categorias;
    }

    @Override
    public List<Categoria> findAll() {
        throw new UnsupportedOperationException("Use findAll(Connection)");
    }

    public void update(Categoria categoria, Connection conn) {
        String sql = "UPDATE categorias SET nome = ?, tipo = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getTipo().name());
            stmt.setInt(3, categoria.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Categoria '" + categoria.getNome() + "' atualizada com sucesso.");
            } else {
                System.out.println("Nenhuma categoria encontrada com ID: " + categoria.getId() + " para atualizar.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar categoria: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar categoria.", e);
        }
    }

    @Override
    public void update(Categoria categoria) {
        throw new UnsupportedOperationException("Use update(Categoria, Connection)");
    }

    public void delete(Integer id, Connection conn) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Categoria com ID " + id + " excluida com sucesso.");
            } else {
                System.out.println("Nenhuma categoria encontrada com ID: " + id + " para excluir.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir categoria: " + e.getMessage());
            throw new RuntimeException("Erro ao excluir categoria.", e);
        }
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Use delete(Integer, Connection)");
    }

    public Categoria findByNome(String nome, Connection conn) {
        String sql = "SELECT id, nome, tipo FROM categorias WHERE nome = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    return new Categoria(id, nome, tipo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categoria por nome: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categoria por nome.", e);
        }
        return null;
    }

    public List<Categoria> findAllByNomeLike(String termoBusca, Connection conn) {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nome, tipo FROM categorias WHERE nome LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termoBusca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    TipoCategoria tipo = TipoCategoria.valueOf(rs.getString("tipo"));
                    categorias.add(new Categoria(id, nome, tipo));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar categorias por termo: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar categorias por termo.", e);
        }
        return categorias;
    }
}