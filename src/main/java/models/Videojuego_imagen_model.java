
package models;

import db.ConexionMySQL;
import dtos.Videojuego_imagen_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Videojuego_imagen_model {

    // Lista todas las imágenes de un videojuego, portada primero
    public List<Videojuego_imagen_dtos> listarPorVideojuego(int idVideojuego) {
        List<Videojuego_imagen_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_imagen, id_videojuego, url_imagen, es_portada, orden FROM videojuego_imagen WHERE id_videojuego = ? ORDER BY es_portada DESC, orden ASC, id_imagen ASC";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoImagen_model.listarPorVideojuego");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idVideojuego);
            rs = ps.executeQuery();

            while (rs.next()) {
                Videojuego_imagen_dtos img = crearImagenDesdeFila(rs);
                lista.add(img);
            }

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoImagen_model.listarPorVideojuego(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    // Inserta una nueva imagen y devuelve el DTO con idImagen asignado
    public Videojuego_imagen_dtos insertar(Videojuego_imagen_dtos img) {
        String sql = "INSERT INTO videojuego_imagen (id_videojuego, url_imagen, es_portada, orden) VALUES (?, ?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoImagen_model.insertar");
                return null;
            }

            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, img.getIdVideojuego());
            ps.setString(2, img.getUrlImagen());
            ps.setBoolean(3, img.isEsPortada());

            if (img.getOrden() != null) {
                ps.setInt(4, img.getOrden());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) {
                System.out.println("No se insertó ninguna imagen");
                return null;
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                img.setIdImagen(rs.getInt(1));
            }

            return img;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoImagen_model.insertar(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // Actualizar datos de una imagen existente
    public boolean actualizar(Videojuego_imagen_dtos img) {
        String sql = "UPDATE videojuego_imagen SET url_imagen = ?, es_portada = ?, orden = ? WHERE id_imagen = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoImagen_model.actualizar");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, img.getUrlImagen());
            ps.setBoolean(2, img.isEsPortada());

            if (img.getOrden() != null) {
                ps.setInt(3, img.getOrden());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setInt(4, img.getIdImagen());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoImagen_model.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // Eliminar una imagen por id
    public boolean eliminar(int idImagen) {
        String sql = "DELETE FROM videojuego_imagen WHERE id_imagen = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoImagen_model.eliminar");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idImagen);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoImagen_model.eliminar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    // Marca una imagen como portada y desmarca las otras del mismo videojuego (transacción)
    public boolean marcarComoPortada(int idVideojuego, int idImagen) {
        String sqlUnmark = "UPDATE videojuego_imagen SET es_portada = 0 WHERE id_videojuego = ?";
        String sqlMark   = "UPDATE videojuego_imagen SET es_portada = 1 WHERE id_imagen = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement psUnmark = null;
        PreparedStatement psMark = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en VideojuegoImagen_model.marcarComoPortada");
                return false;
            }

            conn.setAutoCommit(false);

            psUnmark = conn.prepareStatement(sqlUnmark);
            psUnmark.setInt(1, idVideojuego);
            psUnmark.executeUpdate();

            psMark = conn.prepareStatement(sqlMark);
            psMark.setInt(1, idImagen);
            int filas = psMark.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en VideojuegoImagen_model.marcarComoPortada(): " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {}
            return false;
        } finally {
            try { if (psUnmark != null) psUnmark.close(); } catch (Exception ex) {}
            try { if (psMark != null) psMark.close(); } catch (Exception ex) {}
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }

    private Videojuego_imagen_dtos crearImagenDesdeFila(ResultSet rs) throws SQLException {
        Videojuego_imagen_dtos img = new Videojuego_imagen_dtos();
        img.setIdImagen(rs.getInt("id_imagen"));
        img.setIdVideojuego(rs.getInt("id_videojuego"));
        img.setUrlImagen(rs.getString("url_imagen"));
        img.setEsPortada(rs.getBoolean("es_portada"));

        int orden = rs.getInt("orden");
        if (rs.wasNull()) {
            img.setOrden(null);
        } else {
            img.setOrden(orden);
        }

        return img;
    }

    public boolean reemplazarImagenes(int idVideojuego, List<Videojuego_imagen_dtos> imgs) {
        if (imgs == null) imgs = Collections.emptyList();

        String del = "DELETE FROM videojuego_imagen WHERE id_videojuego = ?";
        String ins = "INSERT INTO videojuego_imagen(id_videojuego, url_imagen, es_portada, orden) VALUES (?, ?, ?, ?)";

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

            if (!imgs.isEmpty()) {
                // Si ninguna viene como portada, forzamos la primera como portada
                boolean hayPortada = imgs.stream().anyMatch(Videojuego_imagen_dtos::isEsPortada);

                psIns = conn.prepareStatement(ins);
                for (int i = 0; i < imgs.size(); i++) {
                    Videojuego_imagen_dtos img = imgs.get(i);
                    if (img == null || img.getUrlImagen() == null || img.getUrlImagen().trim().isEmpty()) continue;

                    boolean esPortada = img.isEsPortada() || (!hayPortada && i == 0);

                    psIns.setInt(1, idVideojuego);
                    psIns.setString(2, img.getUrlImagen().trim());
                    psIns.setBoolean(3, esPortada);

                    if (img.getOrden() != null) psIns.setInt(4, img.getOrden());
                    else psIns.setNull(4, Types.INTEGER);

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

    public int contarImagenes(int idVideojuego) {
        String sql = "SELECT COUNT(*) c FROM videojuego_imagen WHERE id_videojuego = ?";
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
    
    
    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        conexionMySQL.desconectar(conn);
    }
}
