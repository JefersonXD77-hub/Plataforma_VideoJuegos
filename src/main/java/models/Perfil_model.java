package models;

import db.ConexionMySQL;
import dtos.Preferencias_usuario_dtos;
import dtos.Usuario_dtos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Perfil_model {

    public static class PerfilPublico {
        public Usuario_dtos usuario;
        public Preferencias_usuario_dtos preferencias;
        public String paisNombre;
    }

    public PerfilPublico obtenerPerfilPublicoPorNickname(String nickname) {
        String sql = "SELECT u.id_usuario, u.nickname, u.nombre_completo, u.fecha_nacimiento, u.telefono, u.id_pais, u.estado, " +
                     "p.biblioteca_publica, p.avatar_url, pa.nombre AS pais_nombre " +
                     "FROM usuario u " +
                     "LEFT JOIN preferencias_usuario p ON p.id_usuario = u.id_usuario " +
                     "LEFT JOIN pais pa ON pa.id_pais = u.id_pais " +
                     "WHERE u.nickname = ? LIMIT 1";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return null;

            ps = conn.prepareStatement(sql);
            ps.setString(1, nickname);
            rs = ps.executeQuery();

            if (!rs.next()) return null;

            PerfilPublico out = new PerfilPublico();

            Usuario_dtos u = new Usuario_dtos();
            u.setIdUsuario(rs.getInt("id_usuario"));
            u.setNickname(rs.getString("nickname"));
            u.setNombreCompleto(rs.getString("nombre_completo"));
            u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
            u.setTelefono(rs.getString("telefono"));
            u.setIdPais(rs.getInt("id_pais"));
            u.setEstado(rs.getString("estado"));
            out.usuario = u;

            Preferencias_usuario_dtos pref = new Preferencias_usuario_dtos();
            pref.setIdUsuario(u.getIdUsuario());
            pref.setBibliotecaPublica(rs.getBoolean("biblioteca_publica"));
            pref.setAvatarUrl(rs.getString("avatar_url"));
            out.preferencias = pref;

            out.paisNombre = rs.getString("pais_nombre");
            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean actualizarPerfil(int idUsuario, String nickname, String nombreCompleto, String telefono, Integer idPais) {
        
        StringBuilder sb = new StringBuilder("UPDATE usuario SET ");
        boolean first = true;

        if (nickname != null && !nickname.trim().isEmpty()) {
            sb.append("nickname=?");
            first = false;
        }
        if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
            if (!first) sb.append(", ");
            sb.append("nombre_completo=?");
            first = false;
        }
        if (telefono != null) {
            if (!first) sb.append(", ");
            sb.append("telefono=?");
            first = false;
        }
        if (idPais != null && idPais > 0) {
            if (!first) sb.append(", ");
            sb.append("id_pais=?");
            first = false;
        }

        if (first) return false;
        sb.append(" WHERE id_usuario=?");

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sb.toString());
            int idx = 1;
            if (nickname != null && !nickname.trim().isEmpty()) ps.setString(idx++, nickname.trim());
            if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) ps.setString(idx++, nombreCompleto.trim());
            if (telefono != null) ps.setString(idx++, telefono.trim());
            if (idPais != null && idPais > 0) ps.setInt(idx++, idPais);
            ps.setInt(idx, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean actualizarPasswordPorCorreo(String correo, String nuevaPassword) {
        String sql = "UPDATE usuario SET password=? WHERE correo=? LIMIT 1";

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;

            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevaPassword);
            ps.setString(2, correo.trim().toLowerCase());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean existeCorreo(String correo) {
        String sql = "SELECT 1 FROM usuario WHERE correo=? LIMIT 1";
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cx.conectar();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setString(1, correo.trim().toLowerCase());
            rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }
}