package models;

import db.ConexionMySQL;
import dtos.Preferencias_usuario_dtos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Preferencias_usuario_model {

    public Preferencias_usuario_dtos obtener(int idUsuario) {
        String sql = "SELECT id_usuario, biblioteca_publica, avatar_url FROM preferencias_usuario WHERE id_usuario = ?";
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

            if (rs.next()) {
                Preferencias_usuario_dtos p = new Preferencias_usuario_dtos();
                p.setIdUsuario(rs.getInt("id_usuario"));
                p.setBibliotecaPublica(rs.getBoolean("biblioteca_publica"));
                p.setAvatarUrl(rs.getString("avatar_url"));
                return p;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    public boolean actualizar(int idUsuario, Boolean bibliotecaPublica, String avatarUrl) {
       
        StringBuilder sb = new StringBuilder("UPDATE preferencias_usuario SET ");
        boolean first = true;
        if (bibliotecaPublica != null) {
            sb.append("biblioteca_publica=?");
            first = false;
        }
        if (avatarUrl != null) {
            if (!first) sb.append(", ");
            sb.append("avatar_url=?");
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
            if (bibliotecaPublica != null) ps.setBoolean(idx++, bibliotecaPublica);
            if (avatarUrl != null) ps.setString(idx++, avatarUrl);
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
}
