
package models;

import db.ConexionMySQL;
import dtos.Usuario_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Registro_model {

    //Método encargado de verificar si el correo está disponible para usarse.
    public boolean esCorreoDisponible(String correo) {
        String sql = "SELECT COUNT(*) AS total FROM usuario WHERE correo = ? AND estado != 'ELIMINADO'";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conectado = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectado = conexionMySQL.conectar();
            if (conectado == null) {
                System.out.println("No se pudo obtener conexión en Registro_model.esCorreoDisponible");
                return false; 
            }

            ps = conectado.prepareStatement(sql);
            ps.setString(1, correo.trim().toLowerCase());
            rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                return total == 0;
            }

        } catch (SQLException e) {
            System.out.println("Error en Registro_model.esCorreoDisponible(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            conexionMySQL.desconectar(conectado);
        }

        return false;
    }

    //Método encargado de registrar un usuario común y sus atributos.
    public Usuario_dtos registrarUsuarioComun(Usuario_dtos usuario, int idRolComun) {
        String sqlInsertUsuario = "INSERT INTO usuario (id_rol, nickname, nombre_completo, correo, password, fecha_nacimiento, telefono, id_pais, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO')";

        String sqlInsertCartera = "INSERT INTO cartera (id_usuario, saldo) VALUES (?, 0.00)";

        String sqlInsertPreferencias = "INSERT INTO preferencias_usuario (id_usuario, biblioteca_publica, avatar_url) VALUES (?, 1, NULL)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement psUsuario = null;
        PreparedStatement psCartera = null;
        PreparedStatement psPref = null;
        ResultSet rsKeys = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Registro_model.registrarUsuarioComun");
                return null;
            }

            // Iniciamos transacción
            conn.setAutoCommit(false);

            // 1) Insertar usuario
            psUsuario = conn.prepareStatement(sqlInsertUsuario, PreparedStatement.RETURN_GENERATED_KEYS);
            psUsuario.setInt(1, idRolComun);
            psUsuario.setString(2, usuario.getNickname());
            psUsuario.setString(3, usuario.getNombreCompleto());
            psUsuario.setString(4, usuario.getCorreo().trim().toLowerCase());
            psUsuario.setString(5, usuario.getPassword());
            psUsuario.setDate(6, usuario.getFechaNacimiento());
            psUsuario.setString(7, usuario.getTelefono());
            if (usuario.getIdPais() != null) {
                psUsuario.setInt(8, usuario.getIdPais());
            } else {
                psUsuario.setNull(8, java.sql.Types.INTEGER);
            }

            int filasUsuario = psUsuario.executeUpdate();
            if (filasUsuario == 0) {
                conn.rollback();
                System.out.println("No se insertó usuario en Registro_model.registrarUsuarioComun");
                return null;
            }

            rsKeys = psUsuario.getGeneratedKeys();
            int idGenerado = -1;
            if (rsKeys.next()) {
                idGenerado = rsKeys.getInt(1);
                usuario.setIdUsuario(idGenerado);
            } else {
                conn.rollback();
                System.out.println("No se obtuvo id generado de usuario");
                return null;
            }

            // 2) Crear cartera
            psCartera = conn.prepareStatement(sqlInsertCartera);
            psCartera.setInt(1, idGenerado);
            psCartera.executeUpdate();

            // 3) Crear preferencias_usuario
            psPref = conn.prepareStatement(sqlInsertPreferencias);
            psPref.setInt(1, idGenerado);
            psPref.executeUpdate();

            // Si todo sale bien, confirmamos la transacción
            conn.commit();
            conn.setAutoCommit(true);

            return usuario;

        } catch (SQLException e) {
            System.out.println("Error en Registro_model.registrarUsuarioComun(): " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
            }
            return null;
        } finally {
            try { if (rsKeys != null) rsKeys.close(); } catch (Exception ex) {}
            try { if (psPref != null) psPref.close(); } catch (Exception ex) {}
            try { if (psCartera != null) psCartera.close(); } catch (Exception ex) {}
            try { if (psUsuario != null) psUsuario.close(); } catch (Exception ex) {}
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (Exception ex) {}
            conexionMySQL.desconectar(conn);
        }
    }
}

