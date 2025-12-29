package models;

import db.ConexionMySQL;
import dtos.Parametro_sistema_dtos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Parametro_sistema_model {

    public Parametro_sistema_dtos buscarPorClave(String clave) {
        String sql = "SELECT clave, valor_texto, valor_numerico, fecha_actualizacion " +
                     "FROM parametro_sistema WHERE clave = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setString(1, clave);
            rs = ps.executeQuery();

            if (rs.next()) {
                Parametro_sistema_dtos p = new Parametro_sistema_dtos();
                p.setClave(rs.getString("clave"));
                p.setValorTexto(rs.getString("valor_texto"));
                p.setValorNumerico(rs.getBigDecimal("valor_numerico"));
                p.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                return p;
            }
            return null;

        } catch (SQLException e) {
            System.out.println("Error en Parametro_sistema_model.buscarPorClave(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    
    public boolean upsertValorNumerico(String clave, BigDecimal valor) {
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        try {
            conn = cx.conectar();
            if (conn == null) return false;
            return upsertValorNumerico(conn, clave, valor);
        } catch (SQLException e) {
            System.out.println("Error en Parametro_sistema_model.upsertValorNumerico(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            cx.desconectar(conn);
        }
    }

    public boolean upsertValorNumerico(Connection conn, String clave, BigDecimal valor) throws SQLException {
        String update = "UPDATE parametro_sistema " +
                        "SET valor_numerico=?, fecha_actualizacion=CURRENT_TIMESTAMP " +
                        "WHERE clave=?";
        String insert = "INSERT INTO parametro_sistema (clave, valor_numerico, fecha_actualizacion) " +
                        "VALUES (?, ?, CURRENT_TIMESTAMP)";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(update);
            ps.setBigDecimal(1, valor);
            ps.setString(2, clave);
            int rows = ps.executeUpdate();
            ps.close();
            ps = null;

            if (rows > 0) return true;

            ps = conn.prepareStatement(insert);
            ps.setString(1, clave);
            ps.setBigDecimal(2, valor);
            return ps.executeUpdate() > 0;

        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
        }
    }
}
