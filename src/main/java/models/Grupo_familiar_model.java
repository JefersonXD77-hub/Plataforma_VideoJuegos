package models;

import db.ConexionMySQL;
import dtos.Grupo_familiar_dtos;
import dtos.Grupo_miembro_dtos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Grupo_familiar_model {

    public Grupo_familiar_dtos buscarGrupoPorId(int idGrupo) {
        String sql = "SELECT id_grupo, nombre, id_creador, fecha_creacion, estado " +
                     "FROM grupo_familiar WHERE id_grupo = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idGrupo);
            rs = ps.executeQuery();

            if (rs.next()) return mapGrupo(rs);
            return null;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.buscarGrupoPorId(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public int crearGrupo(String nombre, int idCreador) {
        String sql = "INSERT INTO grupo_familiar (nombre, id_creador, estado) VALUES (?, ?, 'ACTIVO')";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return -1;

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setInt(2, idCreador);

            int filas = ps.executeUpdate();
            if (filas <= 0) return -1;

            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

            return -1;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.crearGrupo(): " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public Grupo_miembro_dtos buscarMiembro(int idGrupo, int idUsuario) {
        String sql = "SELECT id_grupo, id_usuario, rol, estado, fecha_invitacion, fecha_estado " +
                     "FROM grupo_miembro WHERE id_grupo = ? AND id_usuario = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idGrupo);
            ps.setInt(2, idUsuario);
            rs = ps.executeQuery();

            if (rs.next()) return mapMiembro(rs);
            return null;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.buscarMiembro(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public boolean insertarMiembro(int idGrupo, int idUsuario, String rol, String estado) {
        String sql = "INSERT INTO grupo_miembro (id_grupo, id_usuario, rol, estado, fecha_estado) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idGrupo);
            ps.setInt(2, idUsuario);
            ps.setString(3, rol);
            ps.setString(4, estado);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.insertarMiembro(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            cerrar(null, ps, conn, cx);
        }
    }

    public boolean actualizarEstadoMiembro(int idGrupo, int idUsuario, String nuevoEstado) {
        String sql = "UPDATE grupo_miembro SET estado = ?, fecha_estado = CURRENT_TIMESTAMP " +
                     "WHERE id_grupo = ? AND id_usuario = ?";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idGrupo);
            ps.setInt(3, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.actualizarEstadoMiembro(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            cerrar(null, ps, conn, cx);
        }
    }

    public int contarMiembrosActivos(int idGrupo) {
        String sql = "SELECT COUNT(*) AS total FROM grupo_miembro WHERE id_grupo = ? AND estado = 'ACTIVO'";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return -1;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idGrupo);
            rs = ps.executeQuery();

            if (rs.next()) return rs.getInt("total");
            return 0;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.contarMiembrosActivos(): " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public List<Grupo_familiar_dtos> listarGruposActivosDeUsuario(int idUsuario) {
        String sql =
            "SELECT g.id_grupo, g.nombre, g.id_creador, g.fecha_creacion, g.estado " +
            "FROM grupo_familiar g " +
            "JOIN grupo_miembro m ON m.id_grupo = g.id_grupo " +
            "WHERE m.id_usuario = ? AND m.estado = 'ACTIVO' AND g.estado = 'ACTIVO' " +
            "ORDER BY g.fecha_creacion DESC";

        List<Grupo_familiar_dtos> lista = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapGrupo(rs));
            }
            return lista;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.listarGruposActivosDeUsuario(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    public List<Grupo_miembro_dtos> listarInvitacionesPendientes(int idUsuario) {
        String sql =
            "SELECT id_grupo, id_usuario, rol, estado, fecha_invitacion, fecha_estado " +
            "FROM grupo_miembro WHERE id_usuario = ? AND estado = 'PENDIENTE' " +
            "ORDER BY fecha_invitacion DESC";

        List<Grupo_miembro_dtos> lista = new ArrayList<>();
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapMiembro(rs));
            }
            return lista;

        } catch (SQLException e) {
            System.out.println("Error en Grupo_familiar_model.listarInvitacionesPendientes(): " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            cerrar(rs, ps, conn, cx);
        }
    }

    private Grupo_familiar_dtos mapGrupo(ResultSet rs) throws SQLException {
        Grupo_familiar_dtos g = new Grupo_familiar_dtos();
        g.setIdGrupo(rs.getInt("id_grupo"));
        g.setNombre(rs.getString("nombre"));
        g.setIdCreador(rs.getInt("id_creador"));
        g.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        g.setEstado(rs.getString("estado"));
        return g;
    }

    private Grupo_miembro_dtos mapMiembro(ResultSet rs) throws SQLException {
        Grupo_miembro_dtos m = new Grupo_miembro_dtos();
        m.setIdGrupo(rs.getInt("id_grupo"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setRol(rs.getString("rol"));
        m.setEstado(rs.getString("estado"));
        m.setFechaInvitacion(rs.getTimestamp("fecha_invitacion"));
        m.setFechaEstado(rs.getTimestamp("fecha_estado"));
        return m;
    }

    private void cerrar(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL cx) {
        try { if (rs != null) rs.close(); } catch (Exception ex) {}
        try { if (ps != null) ps.close(); } catch (Exception ex) {}
        cx.desconectar(conn);
    }
}
