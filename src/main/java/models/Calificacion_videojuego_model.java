package models;

import db.ConexionMySQL;
import dtos.Calificacion_videojuego_dtos;

import java.sql.*;

public class Calificacion_videojuego_model {

    public boolean upsert(int idUsuario, int idVideojuego, int puntuacion) {
        String sql =
            "INSERT INTO calificacion_videojuego (id_usuario, id_videojuego, puntuacion) " +
            "VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE puntuacion=VALUES(puntuacion), fecha_ultima_modificacion=CURRENT_TIMESTAMP";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.setInt(3, puntuacion);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public Calificacion_videojuego_dtos buscar(int idUsuario, int idVideojuego) {
        String sql =
            "SELECT id_usuario, id_videojuego, puntuacion, fecha_ultima_modificacion " +
            "FROM calificacion_videojuego WHERE id_usuario=? AND id_videojuego=?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            rs = ps.executeQuery();

            if (!rs.next()) return null;

            Calificacion_videojuego_dtos c = new Calificacion_videojuego_dtos();
            c.setIdUsuario(rs.getInt("id_usuario"));
            c.setIdVideojuego(rs.getInt("id_videojuego"));
            c.setPuntuacion(rs.getInt("puntuacion"));
            c.setFechaUltimaModificacion(rs.getTimestamp("fecha_ultima_modificacion"));
            return c;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public Double promedioPorVideojuego(int idVideojuego) {
        String sql = "SELECT AVG(puntuacion) AS prom FROM calificacion_videojuego WHERE id_videojuego=?";

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

            if (rs.next()) {
                double v = rs.getDouble("prom");
                if (rs.wasNull()) return 0.0;
                return v;
            }
            return 0.0;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }
}
