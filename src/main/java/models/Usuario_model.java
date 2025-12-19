package models;

import db.ConexionMySQL;
import dtos.Usuario_dtos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuario_model {

    // Listar todos los usuarios (por ejemplo para un admin)
    public List<Usuario_dtos> listarTodos() {
        List<Usuario_dtos> lista = new ArrayList<>();

        String sql = "SELECT id_usuario, id_rol, nickname, nombre_completo, correo, fecha_nacimiento, telefono, id_pais, fecha_registro, estado FROM usuario";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Usuario_model.listarTodos");
                return lista;
            }

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Usuario_dtos u = crearUsuarioDesdeFila(rs);
                lista.add(u);
            }

        } catch (SQLException e) {
            System.out.println("Error en Usuario_model.listarTodos(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return lista;
    }

    //Insertar a un usuario.
    public boolean existeCorreo(String correo) {

        String sql = "SELECT 1 FROM usuario WHERE CORREO = ? limit 1";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            conn = conexionMySQL.conectar();
            if (conn == null) {
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, correo.trim().toLowerCase());
            rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;

        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }
    }   
    
        

    public Usuario_dtos insertar(Usuario_dtos u) {
        String sql = "INSERT INTO usuario (id_rol, nickname, nombre_completo, correo, password, fecha_nacimiento, telefono, id_pais, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                return null;
            }

            ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, u.getIdRol());
            ps.setString(2, u.getNickname());
            ps.setString(3, u.getNombreCompleto());
            ps.setString(4, u.getCorreo().trim().toLowerCase());
            ps.setString(5, u.getPassword());
            ps.setDate(6, u.getFechaNacimiento());

            if (u.getTelefono() != null && !u.getTelefono().isBlank()) {
                ps.setString(7, u.getTelefono());
            } else {
                ps.setNull(7, java.sql.Types.VARCHAR);
            }

            if (u.getIdPais() != null) {
                ps.setInt(8, u.getIdPais());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setString(9, u.getEstado() != null ? u.getEstado() : "ACTIVO");

            int filas = ps.executeUpdate();
            if (filas == 0) {
                return null;
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                u.setIdUsuario(rs.getInt(1));
            }
            return u;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
            }
            conexionMySQL.desconectar(conn);
        }
    }

    // Buscar por id
    public Usuario_dtos buscarPorId(int idUsuario) {
        String sql = "SELECT id_usuario, id_rol, nickname, nombre_completo, correo, fecha_nacimiento, telefono, id_pais, fecha_registro, estado FROM usuario WHERE id_usuario = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Usuario_model.buscarPorId");
                return null;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();

            if (rs.next()) {
                return crearUsuarioDesdeFila(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error en Usuario_model.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return null;
    }

    // Buscar por correo y password
    public Usuario_dtos buscarPorCorreoYPassword(String correo, String password) {
        String sql = "SELECT id_usuario, id_rol, nickname, nombre_completo, correo, fecha_nacimiento, telefono, id_pais, fecha_registro, estado FROM usuario WHERE correo = ? AND password = ? AND estado = 'ACTIVO'";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Usuario_model.buscarPorCorreoYPassword");
                return null;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, correo.trim().toLowerCase());
            ps.setString(2, password);
            rs = ps.executeQuery();

            if (rs.next()) {
                return crearUsuarioDesdeFila(rs);
            }

        } catch (SQLException e) {
            System.out.println("Error en Usuario_model.buscarPorCorreoYPassword(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            cerrarRecursos(rs, ps, conn, conexionMySQL);
        }

        return null;
    }

    // Cambiar estado del usuario (ACTIVO / SUSPENDIDO / ELIMINADO)
    public boolean cambiarEstado(int idUsuario, String nuevoEstado) {
        String sql = "UPDATE usuario SET estado = ? WHERE id_usuario = ?";

        ConexionMySQL conexionMySQL = new ConexionMySQL();
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexionMySQL.conectar();
            if (conn == null) {
                System.out.println("No se pudo obtener conexión en Usuario_model.cambiarEstado");
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error en Usuario_model.cambiarEstado(): " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
            }
            conexionMySQL.desconectar(conn);
        }
    }

    
    private Usuario_dtos crearUsuarioDesdeFila(ResultSet rs) throws SQLException {
        Usuario_dtos u = new Usuario_dtos();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setIdRol(rs.getInt("id_rol"));
        u.setNickname(rs.getString("nickname"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setCorreo(rs.getString("correo"));
        u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
        u.setTelefono(rs.getString("telefono"));
        int idPais = rs.getInt("id_pais");
        if (rs.wasNull()) {
            u.setIdPais(null);
        } else {
            u.setIdPais(idPais);
        }
        u.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        u.setEstado(rs.getString("estado"));
        return u;
    }

    private void cerrarRecursos(ResultSet rs, PreparedStatement ps, Connection conn, ConexionMySQL conexionMySQL) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception ex) {
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception ex) {
        }
        conexionMySQL.desconectar(conn);
    }
}
