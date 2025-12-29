package models;

import dtos.Compra_detalle_dtos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Compra_detalle_model {

    public int insertar(Connection conn, Compra_detalle_dtos d) throws Exception {
        if (conn == null) throw new Exception("Connection null.");
        if (d == null) throw new Exception("Detalle null.");
        if (d.getIdCompra() <= 0 || d.getIdVideojuego() <= 0) throw new Exception("Detalle inválido (idCompra/idVideojuego).");

        String sql = "INSERT INTO compra_detalle " +
                "(id_compra, id_videojuego, precio_unitario, porcentaje_comision, monto_comision, monto_neto_empresa) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getIdCompra());
            ps.setInt(2, d.getIdVideojuego());
            ps.setBigDecimal(3, d.getPrecioUnitario());
            ps.setBigDecimal(4, d.getPorcentajeComision());
            ps.setBigDecimal(5, d.getMontoComision());
            ps.setBigDecimal(6, d.getMontoNetoEmpresa());

            int filas = ps.executeUpdate();
            if (filas == 0) throw new Exception("No se insertó compra_detalle.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new Exception("No se obtuvo id_detalle.");
                return rs.getInt(1);
            }
        }
    }

    public void insertarBatch(Connection conn, List<Compra_detalle_dtos> detalles) throws Exception {
        if (conn == null) throw new Exception("Connection null.");
        if (detalles == null || detalles.isEmpty()) return;

        String sql = "INSERT INTO compra_detalle " +
                "(id_compra, id_videojuego, precio_unitario, porcentaje_comision, monto_comision, monto_neto_empresa) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int count = 0;
            for (Compra_detalle_dtos d : detalles) {
                if (d == null) continue;
                if (d.getIdCompra() <= 0 || d.getIdVideojuego() <= 0) continue;

                ps.setInt(1, d.getIdCompra());
                ps.setInt(2, d.getIdVideojuego());
                ps.setBigDecimal(3, d.getPrecioUnitario());
                ps.setBigDecimal(4, d.getPorcentajeComision());
                ps.setBigDecimal(5, d.getMontoComision());
                ps.setBigDecimal(6, d.getMontoNetoEmpresa());
                ps.addBatch();
                count++;
            }
            if (count > 0) ps.executeBatch();
        }
    }

    public List<Compra_detalle_dtos> listarPorCompra(Connection conn, int idCompra) throws Exception {
        if (conn == null) throw new Exception("Connection null.");
        List<Compra_detalle_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_detalle, id_compra, id_videojuego, precio_unitario, porcentaje_comision, monto_comision, monto_neto_empresa " +
                     "FROM compra_detalle WHERE id_compra = ? ORDER BY id_detalle ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra_detalle_dtos d = new Compra_detalle_dtos();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setIdCompra(rs.getInt("id_compra"));
                    d.setIdVideojuego(rs.getInt("id_videojuego"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setPorcentajeComision(rs.getBigDecimal("porcentaje_comision"));
                    d.setMontoComision(rs.getBigDecimal("monto_comision"));
                    d.setMontoNetoEmpresa(rs.getBigDecimal("monto_neto_empresa"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }
}
