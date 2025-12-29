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
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
            }
            conexionMySQL.desconectar(conn);
        }

        return null;
    }

    // Metodo para recargar saldo de cartera y registrar movimiento de tipo RECARGA transacción
    public boolean recargarSaldo(int idUsuario, BigDecimal monto, String descripcion) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String sqlUpdateCartera
                = "UPDATE cartera SET saldo = saldo + ?, fecha_actualizacion = CURRENT_TIMESTAMP WHERE id_usuario = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement psUpdate = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);

            psUpdate = conn.prepareStatement(sqlUpdateCartera);
            psUpdate.setBigDecimal(1, monto);
            psUpdate.setInt(2, idUsuario);

            int filasCartera = psUpdate.executeUpdate();
            if (filasCartera == 0) {
                conn.rollback();
                return false;
            }

            // INSERT movimiento usando MISMA conexión
            Movimiento_cartera_model movModel = new Movimiento_cartera_model();
            dtos.Movimiento_cartera_dtos mov = new dtos.Movimiento_cartera_dtos();
            mov.setIdUsuario(idUsuario);
            mov.setIdCompra(null);
            mov.setTipo("RECARGA");
            mov.setMonto(monto);
            mov.setDescripcion(descripcion);

            boolean okMov = movModel.registrarMovimiento(conn, mov);
            if (!okMov) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ignored) {
            }
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (psUpdate != null) {
                    psUpdate.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (Exception ignored) {
            }
            conexionMySQL.desconectar(conn);
        }
    }

}
