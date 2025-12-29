package models;

import db.ConexionMySQL;
import dtos.Comentario_videojuego_dtos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Comentario_videojuego_model {

    public boolean existeVideojuego(int idVideojuego) {
        String sql = "SELECT 1 FROM videojuego WHERE id_videojuego=? LIMIT 1";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

// Regla  solo puede comentar/calificar si compró 
public boolean usuarioPoseeJuegoPropio(int idUsuario, int idVideojuego) {
    String sql = "SELECT 1 FROM biblioteca_usuario WHERE id_usuario=? AND id_videojuego=? AND origen='PROPIO' LIMIT 1";
    ConexionMySQL cx = new ConexionMySQL();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = cx.conectar();
        if (conn == null) return false;
        ps = conn.prepareStatement(sql);
        ps.setInt(1, idUsuario);
        ps.setInt(2, idVideojuego);
        rs = ps.executeQuery();
        return rs.next();
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    } finally {
        cerrar(rs, ps, conn, cx);
    }
}


    public Integer obtenerEmpresaDeVideojuego(int idVideojuego) {
        String sql = "SELECT id_empresa FROM videojuego WHERE id_videojuego=?";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_empresa");
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public Comentario_videojuego_dtos buscarPorId(int idComentario) {
        String sql =
            "SELECT c.id_comentario, c.id_videojuego, c.id_usuario, c.id_comentario_padre, c.texto, c.fecha, " +
            "c.visible_empresa, c.visible_admin, u.nickname " +
            "FROM comentario_videojuego c " +
            "JOIN usuario u ON u.id_usuario = c.id_usuario " +
            "WHERE c.id_comentario=?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idComentario);
            rs = ps.executeQuery();
            if (rs.next()) return map(rs);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    
    public List<Comentario_videojuego_dtos> listarPublicoPorVideojuego(int idVideojuego) {
        String sql =
            "SELECT c.id_comentario, c.id_videojuego, c.id_usuario, c.id_comentario_padre, " +
            "CASE WHEN c.visible_empresa=1 THEN c.texto ELSE NULL END AS texto, " +
            "c.fecha, c.visible_empresa, c.visible_admin, u.nickname " +
            "FROM comentario_videojuego c " +
            "JOIN usuario u ON u.id_usuario = c.id_usuario " +
            "WHERE c.id_videojuego=? AND c.visible_admin=1 " +
            "ORDER BY c.fecha ASC";

        return listar(sql, idVideojuego);
    }


   
    public List<Comentario_videojuego_dtos> listarAdminPorVideojuego(int idVideojuego) {
        String sql =
            "SELECT c.id_comentario, c.id_videojuego, c.id_usuario, c.id_comentario_padre, c.texto, c.fecha, " +
            "c.visible_empresa, c.visible_admin, u.nickname " +
            "FROM comentario_videojuego c " +
            "JOIN usuario u ON u.id_usuario = c.id_usuario " +
            "WHERE c.id_videojuego=? " +
            "ORDER BY c.fecha ASC";

        return listar(sql, idVideojuego);
    }

    private List<Comentario_videojuego_dtos> listar(String sql, int idVideojuego) {
        List<Comentario_videojuego_dtos> lista = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();
            while (rs.next()) lista.add(map(rs));
            return lista;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public int crearComentario(Comentario_videojuego_dtos dto) {
        String sql =
            "INSERT INTO comentario_videojuego (id_videojuego, id_usuario, id_comentario_padre, texto) " +
            "VALUES (?, ?, ?, ?)";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return -1;

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, dto.getIdVideojuego());
            ps.setInt(2, dto.getIdUsuario());
            if (dto.getIdComentarioPadre() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, dto.getIdComentarioPadre());
            ps.setString(4, dto.getTexto());

            int filas = ps.executeUpdate();
            if (filas <= 0) return -1;

            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public boolean setVisibleEmpresa(int idComentario, boolean visible) {
        String sql = "UPDATE comentario_videojuego SET visible_empresa=? WHERE id_comentario=?";
        return ejecutarUpdate(sql, visible, idComentario);
    }

    public boolean setVisibleAdmin(int idComentario, boolean visible) {
        String sql = "UPDATE comentario_videojuego SET visible_admin=? WHERE id_comentario=?";
        return ejecutarUpdate(sql, visible, idComentario);
    }

   
    public boolean eliminar(int idComentario) {
        String sql = "DELETE FROM comentario_videojuego WHERE id_comentario=?";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idComentario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            cerrar(null, ps, conn, cx);
        }
    }

    private boolean ejecutarUpdate(String sql, boolean visible, int idComentario) {
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, visible);
            ps.setInt(2, idComentario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            cerrar(null, ps, conn, cx);
        }
    }

    private Comentario_videojuego_dtos map(ResultSet rs) throws SQLException {
        Comentario_videojuego_dtos c = new Comentario_videojuego_dtos();
        c.setIdComentario(rs.getInt("id_comentario"));
        c.setIdVideojuego(rs.getInt("id_videojuego"));
        c.setIdUsuario(rs.getInt("id_usuario"));

        int padre = rs.getInt("id_comentario_padre");
        if (rs.wasNull()) c.setIdComentarioPadre(null);
        else c.setIdComentarioPadre(padre);

        c.setTexto(rs.getString("texto"));
        c.setFecha(rs.getTimestamp("fecha"));
        c.setVisibleEmpresa(rs.getBoolean("visible_empresa"));
        c.setVisibleAdmin(rs.getBoolean("visible_admin"));
        c.setNicknameUsuario(rs.getString("nickname"));
        return c;
    }

    private void cerrar(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL cx) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        cx.desconectar(conn);
    }
}
