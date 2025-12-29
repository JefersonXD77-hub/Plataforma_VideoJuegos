package models;

import db.ConexionMySQL;
import dtos.BibliotecaItemView_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Biblioteca_model {

    public boolean existePropio(Connection conn, int idUsuario, int idVideojuego) throws Exception {
        String sql = "SELECT 1 FROM biblioteca_usuario " +
                     "WHERE id_usuario=? AND id_videojuego=? AND origen='PROPIO' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean insertarPropio(Connection conn, int idUsuario, int idVideojuego) throws Exception {
        String sql = "INSERT INTO biblioteca_usuario (id_usuario, id_videojuego, origen, id_usuario_dueno) " +
                     "VALUES (?, ?, 'PROPIO', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }
    
    public List<BibliotecaItemView_dtos> listarMiBiblioteca(int idUsuario) {
        List<BibliotecaItemView_dtos> lista = new ArrayList<>();

        String sql = ""
            + "SELECT "
            + "  b.id_biblioteca, b.id_usuario, b.id_videojuego, b.origen, b.id_usuario_dueno, b.fecha_alta, "
            + "  v.id_empresa, v.id_clasificacion, v.titulo, v.descripcion, v.precio, v.fecha_lanzamiento, v.estado "
            + "FROM biblioteca_usuario b "
            + "JOIN videojuego v ON v.id_videojuego = b.id_videojuego "
            + "WHERE b.id_usuario = ? "
            + "ORDER BY b.fecha_alta DESC, b.id_biblioteca DESC";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return lista;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            while (rs.next()) {
                BibliotecaItemView_dtos it = new BibliotecaItemView_dtos();
                it.setIdBiblioteca(rs.getInt("id_biblioteca"));
                it.setIdUsuario(rs.getInt("id_usuario"));
                it.setIdVideojuego(rs.getInt("id_videojuego"));
                it.setOrigen(rs.getString("origen"));

                int idDueno = rs.getInt("id_usuario_dueno");
                it.setIdUsuarioDueno(idDueno);
                it.setFechaAlta(rs.getTimestamp("fecha_alta"));
                it.setIdEmpresa(rs.getInt("id_empresa"));
                it.setIdClasificacion(rs.getInt("id_clasificacion"));
                it.setTitulo(rs.getString("titulo"));
                it.setDescripcion(rs.getString("descripcion"));
                it.setPrecio(rs.getBigDecimal("precio"));
                it.setFechaLanzamiento(rs.getDate("fecha_lanzamiento"));
                it.setEstado(rs.getString("estado"));

                lista.add(it);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }

        return lista;
    }
}
