
package models;

import db.ConexionMySQL;
import dtos.Cartera_dtos;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Cartera_model {

    // Metodo para obtener la cartera de un usuario
    public Cartera_dtos buscarPorIdUsuario(int idUsuario) {
        String sql = "SELECT id_usuario, saldo, fecha_actualizacion FROM cartera WHERE id_usuario = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Cartera_model.buscarPorIdUsuario");
                return null;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            if (rs.next()) {
                Cartera_dtos c = new Cartera_dtos();
                c.setIdUsuario(rs.getInt("id_usuario"));
                c.setSaldo(rs.getBigDecimal("saldo"));
                c.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                return c;
            }

        } catch (SQLException e) {
            System.out.println("Error en Cartera_model.buscarPorIdUsuario(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }

        return null;
    }

    // Metodo para recargar saldo de cartera y registrar movimiento de tipo RECARGA transacción
    public boolean recargarSaldo(int idUsuario, BigDecimal monto, String descripcion) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String sqlUpdateCartera = "UPDATE cartera SET saldo = saldo + ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_usuario = ?";

        String sqlInsertMovimiento = "INSERT INTO movimiento_cartera (id_usuario, id_compra, tipo, monto, descripcion) VALUES (?, NULL, 'RECARGA', ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectar = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psMov = null;

        try {
            conectar = conexionMySQL.conectar();
            if (conectar == null) {
                System.out.println("No se pudo obtener conexión en Cartera_model.recargarSaldo");
                return false;
            }

            conectar.setAutoCommit(false);

            // Actualizar saldo 
            psUpdate = conectar.prepareStatement(sqlUpdateCartera);
            psUpdate.setBigDecimal(1, monto);
            psUpdate.setInt(2, idUsuario);

            int filasCartera = psUpdate.executeUpdate();
            if (filasCartera == 0) {
                System.out.println("No se actualizó la cartera; ¿existe la cartera para ese usuario?");
                conectar.rollback();
                conectar.setAutoCommit(true);
                return false;
            }

            // Insertar movimiento de recarga 
            psMov = conectar.prepareStatement(sqlInsertMovimiento);
            psMov.setInt(1, idUsuario);
            psMov.setBigDecimal(2, monto);
            psMov.setString(3, descripcion);
            psMov.executeUpdate();

            conectar.commit();
            conectar.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.out.println("Error en Cartera_model.recargarSaldo(): " + e.getMessage());
            e.printStackTrace();
            try {
                if (conectar != null) conectar.rollback();
            } catch (Exception ex) {}
            return false;
        } finally {
            try { if (psMov != null) psMov.close(); } catch (Exception ex) {}
            try { if (psUpdate != null) psUpdate.close(); } catch (Exception ex) {}
            try { if (conectar != null) conectar.setAutoCommit(true); } catch (Exception ex) {}
            conexionMySQL.desconectar(conectar);
        }
    }
}

