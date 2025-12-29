
package models;

import db.ConexionMySQL;
import dtos.Categoria_dtos;
import dtos.VideojuegoCategoriaModeracionItem_dtos;
import dtos.Videojuego_categoria_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Videojuego_Categoria_model {
 
    public List<Categoria_dtos> listarCategoriasVideojuego(int idVideojuego){
    List<Categoria_dtos> lista = new ArrayList<>();
    
    String sql = "SELECT c.id_categoria, c.nombre, c.descripcion, c.estado FROM videojuego_categoria vc INNER JOIN categoria c ON vc.id_categoria = c.id_categoria WHERE vc.id_videojuego = ?";
        
   ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
    
     try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoCategoria_model.listarCategoriasDeVideojuego");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria_dtos c = new Categoria_dtos();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                c.setEstado(rs.getString("estado"));
                lista.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoCategoria_model.listarCategoriasDeVideojuego(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }



    public boolean reemplazarCategorias(int idVideojuego, List<Integer> idsCategoria) {
        return reemplazarCategoriasConEstado(idVideojuego, idsCategoria, "APROBADA", null);
    }

    public int contarCategorias(int idVideojuego) {
        String sql = "SELECT COUNT(*) c FROM videojuego_categoria WHERE id_videojuego = ?";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return 0;
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("c") : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }

   
    public boolean reemplazarCategoriasConEstado(int idVideojuego, List<Integer> idsCategoria, String estadoRel, Integer idAdminRevision) {
        if (idsCategoria == null) idsCategoria = Collections.emptyList();
        if (estadoRel == null || estadoRel.isBlank()) estadoRel = "APROBADA";

        String del = "DELETE FROM videojuego_categoria WHERE id_videojuego = ?";
        // Esquema mínimo: solo (id_videojuego, id_categoria, estado)
        // (sin auditoría/fechas) para reducir cambios en la BD.
        String ins = "INSERT INTO videojuego_categoria(id_videojuego, id_categoria, estado) VALUES (?, ?, ?)";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement psDel = null;
        PreparedStatement psIns = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            psDel = conn.prepareStatement(del);
            psDel.setInt(1, idVideojuego);
            psDel.executeUpdate();

            if (!idsCategoria.isEmpty()) {
                psIns = conn.prepareStatement(ins);
                for (Integer idCat : idsCategoria) {
                    if (idCat == null) continue;
                    psIns.setInt(1, idVideojuego);
                    psIns.setInt(2, idCat);
                    psIns.setString(3, estadoRel.trim().toUpperCase());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { if (psIns != null) psIns.close(); } catch (Exception ignored) {}
            try { if (psDel != null) psDel.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }
    
    

    public boolean agregarCategoria(Videojuego_categoria_dtos vc) {
        String sql = "INSERT INTO videojuego_categoria (id_videojuego, id_categoria) VALUES (?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoCategoria_model.agregarCategoria");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, vc.getIdVideojuego());
            ps.setInt(2, vc.getIdCategoria());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoCategoria_model.agregarCategoria(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    
    public List<VideojuegoCategoriaModeracionItem_dtos> listarPendientesModeracion() {
        List<VideojuegoCategoriaModeracionItem_dtos> out = new ArrayList<>();
        String sql = "SELECT vc.id_videojuego, v.titulo, v.id_empresa, e.nombre AS empresa, "
                + "       vc.id_categoria, c.nombre AS categoria, vc.estado, NULL AS fecha_solicitud "
                + "FROM videojuego_categoria vc "
                + "JOIN videojuego v ON v.id_videojuego = vc.id_videojuego "
                + "JOIN empresa e ON e.id_empresa = v.id_empresa "
                + "JOIN categoria c ON c.id_categoria = vc.id_categoria "
                + "WHERE vc.estado = 'PENDIENTE' "
                + "ORDER BY vc.id_videojuego ASC, vc.id_categoria ASC";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = cx.conectar();
            if (conn == null) return out;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                VideojuegoCategoriaModeracionItem_dtos it = new VideojuegoCategoriaModeracionItem_dtos();
                it.setIdVideojuego(rs.getInt("id_videojuego"));
                it.setTituloVideojuego(rs.getString("titulo"));
                it.setIdEmpresa(rs.getInt("id_empresa"));
                it.setNombreEmpresa(rs.getString("empresa"));
                it.setIdCategoria(rs.getInt("id_categoria"));
                it.setNombreCategoria(rs.getString("categoria"));
                it.setEstado(rs.getString("estado"));
                it.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
                out.add(it);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
        return out;
    }
    
    public boolean revisarAsignacionCategoria(int idVideojuego, int idCategoria, String nuevoEstado, int idAdmin) {
        if (idVideojuego <= 0 || idCategoria <= 0) return false;
        if (nuevoEstado == null) return false;
        String est = nuevoEstado.trim().toUpperCase();
        if (!est.equals("APROBADA") && !est.equals("RECHAZADA")) return false;

        // Esquema mínimo: solo actualiza el estado.
        String sql = "UPDATE videojuego_categoria SET estado=? WHERE id_videojuego=? AND id_categoria=?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setString(1, est);
            ps.setInt(2, idVideojuego);
            ps.setInt(3, idCategoria);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }
    
    
    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        conexionMySQL.desconectar(conn);
    }
}