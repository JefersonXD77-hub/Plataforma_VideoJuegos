package models;

import db.ConexionMySQL;
import dtos.Categoria_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Categoria_model {

    // Listar TODAS las categorías 
    public List<Categoria_dtos> listarTodas() {
        List<Categoria_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_categoria, nombre, descripcion, estado FROM categoria";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.listarTodas");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria_dtos c = convertirFilaACategoriaDTO(rs);
                lista.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.listarTodas(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    // Listar solo categorías ACTIVAS }
    public List<Categoria_dtos> listarActivas() {
        List<Categoria_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_categoria, nombre, descripcion, estado FROM categoria WHERE estado = 'ACTIVA'";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.listarActivas");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria_dtos c = convertirFilaACategoriaDTO(rs);
                lista.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.listarActivas(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    // Buscar una categoría por su ID
    public Categoria_dtos buscarPorId(int idCategoria) {
        String sql = "SELECT id_categoria, nombre, descripcion, estado FROM categoria WHERE id_categoria = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.buscarPorId");
                return null;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idCategoria);
            rs = ps.executeQuery();

            if (rs.next()) {
                return convertirFilaACategoriaDTO(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return null;
    }

    // Verificar si ya existe una categoría con ese nombre 
    public boolean existeNombre(String nombre) {
        String sql = "SELECT COUNT(*) AS total "
                   + "FROM categoria "
                   + "WHERE nombre = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.existeNombre");
                return true;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, nombre.trim());
            rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                return total > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.existeNombre(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return true;
    }

    // Insertar nueva categoría
    public Categoria_dtos insertar(Categoria_dtos categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion, estado) "
                   + "VALUES (?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.insertar");
                return null;
            }

            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            
            String estado = categoria.getEstado();
            if (estado == null || estado.isEmpty()) {
                estado = "ACTIVA";
            }
            ps.setString(3, estado);

            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No se insertó ninguna categoría");
                return null;
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                categoria.setIdCategoria(rs.getInt(1));
            }

            return categoria;

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.insertar(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // Metodo para actualizar nombre / descripción / estado de una categoría
    public boolean actualizar(Categoria_dtos categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ?, estado = ? WHERE id_categoria = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.actualizar");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setString(3, categoria.getEstado());
            ps.setInt(4, categoria.getIdCategoria());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // Cambiar sólo el estado de ACTIVA a INACTIVA
    public boolean cambiarEstado(int idCategoria, String nuevoEstado) {
        String sql = "UPDATE categoria SET estado = ? WHERE id_categoria = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Categoria_model.cambiarEstado");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCategoria);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Categoria_model.cambiarEstado(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    

    private Categoria_dtos convertirFilaACategoriaDTO(ResultSet rs) throws SQLException {
        Categoria_dtos c = new Categoria_dtos();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setEstado(rs.getString("estado"));
        return c;
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        conexionMySQL.desconectar(conn);
    }
}
