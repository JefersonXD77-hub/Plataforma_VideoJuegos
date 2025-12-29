package models;

import db.ConexionMySQL;
import dtos.Notificacion_dtos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificacionDerivada_model {

    public List<Notificacion_dtos> listarNotificaciones(int idUsuario, int limiteCompras, int limitePrestamos) {
        List<Notificacion_dtos> out = new ArrayList<>();
        out.addAll(comprasRecientes(idUsuario, limiteCompras));
        out.addAll(prestamosActivos(idUsuario, limitePrestamos));
        out.sort((a,b) -> {
            Date da = a.getFecha();
            Date db = b.getFecha();
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        return out;
    }

    private List<Notificacion_dtos> comprasRecientes(int idUsuario, int limit) {
        String sql = "SELECT c.id_compra, c.fecha, v.titulo " +
                     "FROM compra c " +
                     "JOIN compra_detalle cd ON cd.id_compra = c.id_compra " +
                     "JOIN videojuego v ON v.id_videojuego = cd.id_videojuego " +
                     "WHERE c.id_usuario = ? AND c.estado='PAGADA' " +
                     "ORDER BY c.fecha DESC LIMIT ?";

        List<Notificacion_dtos> list = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return list;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ps.setInt(2, Math.max(1, limit));
            rs = ps.executeQuery();

            while (rs.next()) {
                int idCompra = rs.getInt("id_compra");
                Date fecha = rs.getTimestamp("fecha");
                String tituloJuego = rs.getString("titulo");

                list.add(new Notificacion_dtos(
                        "COMPRA_CONFIRMADA",
                        "Compra confirmada",
                        "Has comprado: " + tituloJuego,
                        fecha,
                        "COMPRA",
                        idCompra
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }

        return list;
    }

    private List<Notificacion_dtos> prestamosActivos(int idUsuario, int limit) {
        String sql = "SELECT pg.id_prestamo, pg.fecha_inicio, v.titulo, gf.nombre AS grupo_nombre, " +
                     "u.nickname AS dueno_nick " +
                     "FROM prestamo_grupo pg " +
                     "JOIN videojuego v ON v.id_videojuego = pg.id_videojuego " +
                     "JOIN grupo_familiar gf ON gf.id_grupo = pg.id_grupo " +
                     "JOIN usuario u ON u.id_usuario = pg.id_usuario_dueno " +
                     "WHERE pg.id_usuario_receptor = ? AND pg.estado='ACTIVO' " +
                     "ORDER BY pg.fecha_inicio DESC LIMIT ?";

        List<Notificacion_dtos> list = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return list;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ps.setInt(2, Math.max(1, limit));
            rs = ps.executeQuery();

            while (rs.next()) {
                int idPrestamo = rs.getInt("id_prestamo");
                Date fecha = rs.getTimestamp("fecha_inicio");
                String juego = rs.getString("titulo");
                String grupo = rs.getString("grupo_nombre");
                String dueno = rs.getString("dueno_nick");

                list.add(new Notificacion_dtos(
                        "PRESTAMO_RECIBIDO",
                        "Préstamo activo",
                        "Tienes prestado '" + juego + "' del grupo '" + grupo + "' (dueño: " + dueno + ").",
                        fecha,
                        "PRESTAMO",
                        idPrestamo
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }

        return list;
    }
}