package models;

import db.ConexionMySQL;
import dtos.Banner_dtos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Banner_model {

    public List<Banner_dtos> listarActivosVigentes(Date hoy) {
        String sql =
            "SELECT id_banner, titulo, descripcion, url_imagen, id_videojuego, fecha_inicio, fecha_fin, activo " +
            "FROM banner " +
            "WHERE activo = 1 " +
            "AND (fecha_inicio IS NULL OR fecha_inicio <= ?) " +
            "AND (fecha_fin IS NULL OR fecha_fin >= ?) " +
            "ORDER BY id_banner DESC";

        List<Banner_dtos> lista = new ArrayList<>();

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setDate(1, hoy);
            ps.setDate(2, hoy);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapRow(rs));
            }

            return lista;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.listarActivosVigentes(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public List<Banner_dtos> listarTodos() {
        String sql =
            "SELECT id_banner, titulo, descripcion, url_imagen, id_videojuego, fecha_inicio, fecha_fin, activo " +
            "FROM banner ORDER BY id_banner DESC";

        List<Banner_dtos> lista = new ArrayList<>();

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapRow(rs));
            }

            return lista;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.listarTodos(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public int contarActivosExcluyendo(Integer idBannerExcluir) {
    String sql = "SELECT COUNT(*) AS total FROM banner WHERE activo = 1"
            + (idBannerExcluir != null ? " AND id_banner <> ?" : "");

    ConexionMySQL cx = new ConexionMySQL();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        conn = cx.conectar();
        if (conn == null) return -1;

        ps = conn.prepareStatement(sql);
        if (idBannerExcluir != null) ps.setInt(1, idBannerExcluir);
        rs = ps.executeQuery();

        if (rs.next()) return rs.getInt("total");
        return 0;

    } catch (SQLException e) {
        System.out.println("Error en Banner_model.contarActivosExcluyendo(): " + e.getMessage());
        e.printStackTrace();
        return -1;
    } finally {
        cerrar(rs, ps, conn, cx);
    }
}


    public Banner_dtos buscarPorId(int idBanner) {
        String sql =
            "SELECT id_banner, titulo, descripcion, url_imagen, id_videojuego, fecha_inicio, fecha_fin, activo " +
            "FROM banner WHERE id_banner = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idBanner);
            rs = ps.executeQuery();

            if (rs.next()) return mapRow(rs);
            return null;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public boolean crear(Banner_dtos b) {
        String sql =
            "INSERT INTO banner (id_videojuego, activo, fecha_inicio, fecha_fin, url_imagen, titulo, descripcion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            if (b.getIdVideojuego() == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, b.getIdVideojuego());

            ps.setBoolean(2, b.isActivo());
            ps.setDate(3, b.getFechaInicio());
            ps.setDate(4, b.getFechaFin());
            ps.setString(5, b.getUrlImagen());
            ps.setString(6, b.getTitulo());
            ps.setString(7, b.getDescripcion());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.crear(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean actualizar(Banner_dtos b) {
        String sql =
            "UPDATE banner SET id_videojuego=?, activo=?, fecha_inicio=?, fecha_fin=?, url_imagen=?, titulo=?, descripcion=? " +
            "WHERE id_banner=?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);

            if (b.getIdVideojuego() == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, b.getIdVideojuego());

            ps.setBoolean(2, b.isActivo());
            ps.setDate(3, b.getFechaInicio());
            ps.setDate(4, b.getFechaFin());
            ps.setString(5, b.getUrlImagen());
            ps.setString(6, b.getTitulo());
            ps.setString(7, b.getDescripcion());
            ps.setInt(8, b.getIdBanner());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean setActivo(int idBanner, boolean activo) {
        String sql = "UPDATE banner SET activo = ? WHERE id_banner = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, activo);
            ps.setInt(2, idBanner);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.setActivo(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean eliminar(int idBanner) {
        String sql = "DELETE FROM banner WHERE id_banner = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idBanner);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error en Banner_model.eliminar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    private Banner_dtos mapRow(ResultSet rs) throws SQLException {
        Banner_dtos b = new Banner_dtos();
        b.setIdBanner(rs.getInt("id_banner"));
        b.setTitulo(rs.getString("titulo"));
        b.setDescripcion(rs.getString("descripcion"));
        b.setUrlImagen(rs.getString("url_imagen"));

        int idVj = rs.getInt("id_videojuego");
        if (rs.wasNull()) b.setIdVideojuego(null);
        else b.setIdVideojuego(idVj);

        b.setFechaInicio(rs.getDate("fecha_inicio"));
        b.setFechaFin(rs.getDate("fecha_fin"));
        b.setActivo(rs.getBoolean("activo"));
        return b;
    }

    private void cerrar(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL cx) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        cx.desconectar(conn);
    }
}

