package models;

import db.ConexionMySQL;
import dtos.ReporteGanancias_dtos;
import dtos.ReporteRankingUsuario_dtos;
import dtos.ReporteTopVentas_dtos;
import dtos.ReporteTopCalidad_dtos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReporteExtra_model {

    public ReporteGanancias_dtos ganancias(Date ini, Date fin) {
        String sql =
            "SELECT " +
            "  COALESCE(SUM(c.total_bruto),0) AS total_bruto, " +
            "  COALESCE(SUM(c.total_comision_plataforma),0) AS total_comision, " +
            "  COALESCE(SUM(c.total_neto_empresas),0) AS total_neto " +
            "FROM compra c " +
            "WHERE c.estado='PAGADA' " +
            "  AND (? IS NULL OR c.fecha >= ?) " +
            "  AND (? IS NULL OR c.fecha <= ?)";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, ini);
                ps.setDate(2, ini);
                ps.setDate(3, fin);
                ps.setDate(4, fin);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    ReporteGanancias_dtos r = new ReporteGanancias_dtos();
                    r.setTotalBruto(rs.getDouble("total_bruto"));
                    r.setTotalComisionPlataforma(rs.getDouble("total_comision"));
                    r.setTotalNetoEmpresas(rs.getDouble("total_neto"));
                    return r;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            cx.desconectar(conn);
        }
    }

    public List<ReporteRankingUsuario_dtos> rankingUsuariosCompras(Date ini, Date fin, int limit) {
        String sql =
            "SELECT u.id_usuario, u.nickname, COUNT(cd.id_detalle) AS valor " +
            "FROM usuario u " +
            "JOIN compra c ON c.id_usuario = u.id_usuario AND c.estado='PAGADA' " +
            "JOIN compra_detalle cd ON cd.id_compra = c.id_compra " +
            "WHERE (? IS NULL OR c.fecha >= ?) " +
            "  AND (? IS NULL OR c.fecha <= ?) " +
            "GROUP BY u.id_usuario, u.nickname " +
            "ORDER BY valor DESC " +
            "LIMIT ?";

        List<ReporteRankingUsuario_dtos> out = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return out;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, ini);
                ps.setDate(2, ini);
                ps.setDate(3, fin);
                ps.setDate(4, fin);
                ps.setInt(5, Math.max(1, limit));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ReporteRankingUsuario_dtos r = new ReporteRankingUsuario_dtos();
                        r.setIdUsuario(rs.getInt("id_usuario"));
                        r.setNickname(rs.getString("nickname"));
                        r.setValor(rs.getInt("valor"));
                        out.add(r);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cx.desconectar(conn);
        }
        return out;
    }

    public List<ReporteRankingUsuario_dtos> rankingUsuariosResenas(Date ini, Date fin, int limit) {
        String sql =
            "SELECT u.id_usuario, u.nickname, COUNT(cv.id_comentario) AS valor " +
            "FROM usuario u " +
            "JOIN comentario_videojuego cv ON cv.id_usuario = u.id_usuario " +
            "WHERE (? IS NULL OR cv.fecha >= ?) " +
            "  AND (? IS NULL OR cv.fecha <= ?) " +
            "GROUP BY u.id_usuario, u.nickname " +
            "ORDER BY valor DESC " +
            "LIMIT ?";

        List<ReporteRankingUsuario_dtos> out = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return out;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, ini);
                ps.setDate(2, ini);
                ps.setDate(3, fin);
                ps.setDate(4, fin);
                ps.setInt(5, Math.max(1, limit));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ReporteRankingUsuario_dtos r = new ReporteRankingUsuario_dtos();
                        r.setIdUsuario(rs.getInt("id_usuario"));
                        r.setNickname(rs.getString("nickname"));
                        r.setValor(rs.getInt("valor"));
                        out.add(r);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cx.desconectar(conn);
        }
        return out;
    }

    public List<ReporteTopVentas_dtos> topVentas(Integer idCategoria, Integer idClasificacion, Date ini, Date fin, int limit) {
        String sql =
            "SELECT v.id_videojuego, v.titulo, e.nombre AS empresa, COUNT(cd.id_detalle) AS ventas " +
            "FROM videojuego v " +
            "JOIN empresa e ON e.id_empresa = v.id_empresa " +
            "JOIN compra_detalle cd ON cd.id_videojuego = v.id_videojuego " +
            "JOIN compra c ON c.id_compra = cd.id_compra AND c.estado='PAGADA' " +
            "LEFT JOIN videojuego_categoria vc ON vc.id_videojuego = v.id_videojuego " +
            "WHERE (? IS NULL OR v.id_clasificacion = ?) " +
            "  AND (? IS NULL OR vc.id_categoria = ?) " +
            "  AND (? IS NULL OR c.fecha >= ?) " +
            "  AND (? IS NULL OR c.fecha <= ?) " +
            "GROUP BY v.id_videojuego, v.titulo, e.nombre " +
            "ORDER BY ventas DESC " +
            "LIMIT ?";

        List<ReporteTopVentas_dtos> out = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return out;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, idClasificacion);
                ps.setObject(2, idClasificacion);
                ps.setObject(3, idCategoria);
                ps.setObject(4, idCategoria);
                ps.setDate(5, ini);
                ps.setDate(6, ini);
                ps.setDate(7, fin);
                ps.setDate(8, fin);
                ps.setInt(9, Math.max(1, limit));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ReporteTopVentas_dtos r = new ReporteTopVentas_dtos();
                        r.setIdVideojuego(rs.getInt("id_videojuego"));
                        r.setTitulo(rs.getString("titulo"));
                        r.setEmpresa(rs.getString("empresa"));
                        r.setVentas(rs.getInt("ventas"));
                        out.add(r);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cx.desconectar(conn);
        }
        return out;
    }

    public List<ReporteTopCalidad_dtos> topCalidad(Integer idCategoria, Integer idClasificacion, Date ini, Date fin, int limit) {
      
        String sql =
            "SELECT v.id_videojuego, v.titulo, e.nombre AS empresa, " +
            "       AVG(cal.puntuacion) AS promedio, COUNT(*) AS votos " +
            "FROM videojuego v " +
            "JOIN empresa e ON e.id_empresa = v.id_empresa " +
            "JOIN calificacion_videojuego cal ON cal.id_videojuego = v.id_videojuego " +
            "LEFT JOIN videojuego_categoria vc ON vc.id_videojuego = v.id_videojuego " +
            "WHERE (? IS NULL OR v.id_clasificacion = ?) " +
            "  AND (? IS NULL OR vc.id_categoria = ?) " +
            "  AND (? IS NULL OR cal.fecha_ultima_modificacion >= ?) " +
            "  AND (? IS NULL OR cal.fecha_ultima_modificacion <= ?) " +
            "GROUP BY v.id_videojuego, v.titulo, e.nombre " +
            "HAVING COUNT(*) > 0 " +
            "ORDER BY promedio DESC, votos DESC " +
            "LIMIT ?";

        List<ReporteTopCalidad_dtos> out = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return out;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, idClasificacion);
                ps.setObject(2, idClasificacion);
                ps.setObject(3, idCategoria);
                ps.setObject(4, idCategoria);
                ps.setDate(5, ini);
                ps.setDate(6, ini);
                ps.setDate(7, fin);
                ps.setDate(8, fin);
                ps.setInt(9, Math.max(1, limit));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ReporteTopCalidad_dtos r = new ReporteTopCalidad_dtos();
                        r.setIdVideojuego(rs.getInt("id_videojuego"));
                        r.setTitulo(rs.getString("titulo"));
                        r.setEmpresa(rs.getString("empresa"));
                        r.setPromedio(rs.getDouble("promedio"));
                        r.setVotos(rs.getInt("votos"));
                        out.add(r);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cx.desconectar(conn);
        }
        return out;
    }
}
