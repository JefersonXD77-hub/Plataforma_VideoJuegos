package models;

import db.ConexionMySQL;
import dtos.Movimiento_cartera_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Movimiento_cartera_model {

    // Listar todos los movimientos de un usuario
    public List<Movimiento_cartera_dtos> listarPorUsuario(int idUsuario) {
        List<Movimiento_cartera_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_movimiento, id_usuario, id_compra, fecha, tipo, monto, descripcion FROM movimiento_cartera WHERE id_usuario = ? ORDER BY fecha DESC, id_movimiento DESC";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en MovimientoCartera_model.listarPorUsuario");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            while (rs.next()) {
                Movimiento_cartera_dtos mov = crearMovimientoDesdeFila(rs);
                lista.add(mov);
            }

        } catch (SQLException e) {
            System.out.println("Error en MovimientoCartera_model.listarPorUsuario(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    // Listar movimientos filtrando por rango de fechas
    public List<Movimiento_cartera_dtos> listarPorUsuarioYRangoFechas(int idUsuario, Timestamp desde, Timestamp hasta) {
        List<Movimiento_cartera_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_movimiento, id_usuario, id_compra, fecha, tipo, monto, descripcion FROM movimiento_cartera WHERE id_usuario = ? AND fecha BETWEEN ? AND ? ORDER BY fecha DESC, id_movimiento DESC";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en MovimientoCartera_model.listarPorUsuarioYRangoFechas");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ps.setTimestamp(2, desde);
            ps.setTimestamp(3, hasta);
            rs = ps.executeQuery();

            while (rs.next()) {
                Movimiento_cartera_dtos mov = crearMovimientoDesdeFila(rs);
                lista.add(mov);
            }

        } catch (SQLException e) {
            System.out.println("Error en MovimientoCartera_model.listarPorUsuarioYRangoFechas(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    // Registrar movimiento
    public boolean registrarMovimiento(Movimiento_cartera_dtos mov) {
        String sql = "INSERT INTO movimiento_cartera (id_usuario, id_compra, tipo, monto, descripcion) VALUES (?, ?, ?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en MovimientoCartera_model.registrarMovimiento");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, mov.getIdUsuario());

            if (mov.getIdCompra() != null) {
                ps.setInt(2, mov.getIdCompra());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }

            ps.setString(3, mov.getTipo());
            ps.setBigDecimal(4, mov.getMonto());
            ps.setString(5, mov.getDescripcion());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en MovimientoCartera_model.registrarMovimiento(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

  

    private Movimiento_cartera_dtos crearMovimientoDesdeFila(ResultSet rs) throws SQLException {
        Movimiento_cartera_dtos mov = new Movimiento_cartera_dtos();
        mov.setIdMovimiento(rs.getInt("id_movimiento"));
        mov.setIdUsuario(rs.getInt("id_usuario"));

        int idCompra = rs.getInt("id_compra");
        if (rs.wasNull()) {
            mov.setIdCompra(null);
        } else {
            mov.setIdCompra(idCompra);
        }

        mov.setFecha(rs.getTimestamp("fecha"));
        mov.setTipo(rs.getString("tipo"));
        mov.setMonto(rs.getBigDecimal("monto"));
        mov.setDescripcion(rs.getString("descripcion"));

        return mov;
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        conexionMySQL.desconectar(conn);
    }
}

