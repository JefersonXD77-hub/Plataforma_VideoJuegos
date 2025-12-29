package models;

import db.ConexionMySQL;
import java.sql.*;

public class EmpresaUsuario_model {

    public Integer obtenerEmpresaDeUsuario(int idUsuario) {
        String sql = "SELECT id_empresa FROM empresa_usuario WHERE id_usuario = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            if (rs.next()) return rs.getInt("id_empresa");
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }

    public boolean vincularUsuario(int idEmpresa, int idUsuario, boolean esResponsable) {
        String sql = "INSERT INTO empresa_usuario(id_empresa, id_usuario, es_responsable) VALUES (?, ?, ?)";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idEmpresa);
            ps.setInt(2, idUsuario);
            ps.setBoolean(3, esResponsable);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }

    public boolean desvincularUsuario(int idEmpresa, int idUsuario) {
        String sql = "DELETE FROM empresa_usuario WHERE id_empresa=? AND id_usuario=?";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idEmpresa);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }
}
