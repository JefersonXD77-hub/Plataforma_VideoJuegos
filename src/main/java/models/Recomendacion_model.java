package models;

import db.ConexionMySQL;
import dtos.Recomendacion_dtos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Recomendacion_model {

    public List<Recomendacion_dtos> mejorBalance(int limit, int m) {
        if (limit <= 0) limit = 20;
        if (m <= 0) m = 10;

        List<Recomendacion_dtos> out = new ArrayList<>();

        
        String sql = ""
            + "SELECT "
            + "  vj.id_videojuego, vj.titulo, vj.precio, "
            + "  COALESCE(ac.R, 0) AS R, "
            + "  COALESCE(ve.v, 0) AS v, "
            + "  ( (COALESCE(ve.v,0) / (COALESCE(ve.v,0) + ?)) * COALESCE(ac.R,0) ) "
            + "  + ( (? / (COALESCE(ve.v,0) + ?)) * COALESCE(cg.C,0) ) AS score "
            + "FROM videojuego vj "
            + "LEFT JOIN ( "
            + "   SELECT id_videojuego, AVG(puntuacion) AS R "
            + "   FROM calificacion_videojuego "
            + "   GROUP BY id_videojuego "
            + ") ac ON ac.id_videojuego = vj.id_videojuego "
            + "LEFT JOIN ( "
            + "   SELECT id_videojuego, COUNT(*) AS v "
            + "   FROM compra_detalle "
            + "   GROUP BY id_videojuego "
            + ") ve ON ve.id_videojuego = vj.id_videojuego "
            + "CROSS JOIN ( "
            + "   SELECT AVG(puntuacion) AS C "
            + "   FROM calificacion_videojuego "
            + ") cg "
            + "WHERE vj.estado='ACTIVO' "
            + "ORDER BY score DESC, v DESC, vj.id_videojuego DESC "
            + "LIMIT ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return out;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, m);
                ps.setInt(2, m);
                ps.setInt(3, m);
                ps.setInt(4, limit);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Recomendacion_dtos it = new Recomendacion_dtos();
                        it.setIdVideojuego(rs.getInt("id_videojuego"));
                        it.setTitulo(rs.getString("titulo"));
                        it.setPrecio(rs.getBigDecimal("precio"));
                        it.setPromedioCalificacion(rs.getDouble("R"));
                        it.setVentas(rs.getInt("v"));
                        it.setScore(rs.getDouble("score"));
                        out.add(it);
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
