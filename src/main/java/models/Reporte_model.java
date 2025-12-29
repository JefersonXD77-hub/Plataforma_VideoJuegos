package models;

import db.ConexionMySQL;
import dtos.ReporteResumenPlataforma_dtos;
import dtos.ReporteVentaEmpresa_dtos;
import dtos.ReporteTopJuego_dtos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Reporte_model {

    private static class Range {
        Date ini;
        Date fin;
    }

    private Range range(Date ini, Date fin) {
        Range r = new Range();
        r.ini = ini;
        r.fin = fin;
        return r;
    }

    public ReporteResumenPlataforma_dtos resumenPlataforma(Date ini, Date fin) {
        String sql = ""
            + "SELECT "
            + "  COUNT(*) AS totalCompras, "
            + "  COALESCE(SUM(total_bruto),0) AS totalBruto, "
            + "  COALESCE(SUM(total_comision_plataforma),0) AS totalComisionPlataforma, "
            + "  COALESCE(SUM(total_neto_empresas),0) AS totalNetoEmpresas "
            + "FROM compra "
            + "WHERE (? IS NULL OR fecha >= ?) AND (? IS NULL OR fecha <= ?)";

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

                    ReporteResumenPlataforma_dtos r = new ReporteResumenPlataforma_dtos();
                    r.setTotalCompras(rs.getInt("totalCompras"));
                    r.setTotalBruto(rs.getBigDecimal("totalBruto"));
                    r.setTotalComisionPlataforma(rs.getBigDecimal("totalComisionPlataforma"));
                    r.setTotalNetoEmpresas(rs.getBigDecimal("totalNetoEmpresas"));
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

    public List<ReporteVentaEmpresa_dtos> ventasPorEmpresa(Date ini, Date fin) {
        List<ReporteVentaEmpresa_dtos> out = new ArrayList<>();

        String sql = ""
            + "SELECT "
            + "  e.id_empresa, e.nombre AS nombreEmpresa, "
            + "  COUNT(d.id_detalle) AS cantidadVentas, "
            + "  COALESCE(SUM(d.precio_unitario),0) AS montoBruto, "
            + "  COALESCE(SUM(d.monto_neto_empresa),0) AS montoNeto "
            + "FROM compra c "
            + "JOIN compra_detalle d ON d.id_compra=c.id_compra "
            + "JOIN videojuego v ON v.id_videojuego=d.id_videojuego "
            + "JOIN empresa e ON e.id_empresa=v.id_empresa "
            + "WHERE (? IS NULL OR c.fecha >= ?) AND (? IS NULL OR c.fecha <= ?) "
            + "GROUP BY e.id_empresa, e.nombre "
            + "ORDER BY montoNeto DESC, cantidadVentas DESC";

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

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ReporteVentaEmpresa_dtos r = new ReporteVentaEmpresa_dtos();
                        r.setIdEmpresa(rs.getInt("id_empresa"));
                        r.setNombreEmpresa(rs.getString("nombreEmpresa"));
                        r.setCantidadVentas(rs.getInt("cantidadVentas"));
                        r.setMontoBruto(rs.getBigDecimal("montoBruto"));
                        r.setMontoNeto(rs.getBigDecimal("montoNeto"));
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

    public List<ReporteTopJuego_dtos> topVideojuegos(int limit, Date ini, Date fin) {
        if (limit <= 0) limit = 10;
        List<ReporteTopJuego_dtos> out = new ArrayList<>();

        String sql = ""
            + "SELECT "
            + "  v.id_videojuego, v.titulo, "
            + "  COUNT(d.id_detalle) AS ventas, "
            + "  COALESCE(SUM(d.precio_unitario),0) AS ingresoBruto "
            + "FROM compra c "
            + "JOIN compra_detalle d ON d.id_compra=c.id_compra "
            + "JOIN videojuego v ON v.id_videojuego=d.id_videojuego "
            + "WHERE (? IS NULL OR c.fecha >= ?) AND (? IS NULL OR c.fecha <= ?) "
            + "GROUP BY v.id_videojuego, v.titulo "
            + "ORDER BY ventas DESC, ingresoBruto DESC "
            + "LIMIT ?";

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
                ps.setInt(5, limit);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ReporteTopJuego_dtos r = new ReporteTopJuego_dtos();
                        r.setIdVideojuego(rs.getInt("id_videojuego"));
                        r.setTitulo(rs.getString("titulo"));
                        r.setVentas(rs.getInt("ventas"));
                        r.setIngresoBruto(rs.getBigDecimal("ingresoBruto"));
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

