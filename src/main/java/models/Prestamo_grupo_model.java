package models;

import db.ConexionMySQL;
import dtos.UsuarioSimple_dtos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Prestamo_grupo_model {

    // Verifica si el grupo está ACTIVO
    public boolean grupoActivo(Connection conn, int idGrupo) throws SQLException {
        String sql = "SELECT 1 FROM grupo_familiar WHERE id_grupo=? AND estado='ACTIVO' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Verifica miembro ACTIVO del grupo
    public boolean miembroActivo(Connection conn, int idGrupo, int idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM grupo_miembro WHERE id_grupo=? AND id_usuario=? AND estado='ACTIVO' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // El dueño tiene el juego como PROPIO
    public boolean duenoTieneJuegoPropio(Connection conn, int idDueno, int idVideojuego) throws SQLException {
        String sql = "SELECT 1 FROM biblioteca_usuario " +
                     "WHERE id_usuario=? AND id_videojuego=? AND origen='PROPIO' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDueno);
            ps.setInt(2, idVideojuego);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // El juego ya está prestado activamente por el dueño
    public boolean existePrestamoActivoDelJuego(Connection conn, int idDueno, int idVideojuego) throws SQLException {
        String sql = "SELECT 1 FROM prestamo_grupo " +
                     "WHERE id_usuario_dueno=? AND id_videojuego=? AND estado='ACTIVO' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDueno);
            ps.setInt(2, idVideojuego);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Receptor ya tiene el juego PROPIO o PRESTADO
    public boolean receptorYaTieneJuego(Connection conn, int idReceptor, int idVideojuego) throws SQLException {
        String sql = "SELECT 1 FROM biblioteca_usuario WHERE id_usuario=? AND id_videojuego=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReceptor);
            ps.setInt(2, idVideojuego);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    //  si el receptor ya tiene otro juego prestado, no puede recibir uno más
    public boolean receptorTieneOtroPrestamo(Connection conn, int idReceptor) throws SQLException {
        String sql = "SELECT 1 FROM biblioteca_usuario WHERE id_usuario=? AND origen='PRESTADO' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReceptor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Lista miembros disponibles del grupo 
    public List<UsuarioSimple_dtos> listarMiembrosActivos(Connection conn, int idGrupo, int idExcluir) throws SQLException {
        String sql =
            "SELECT u.id_usuario, u.nickname " +
            "FROM grupo_miembro gm " +
            "JOIN usuario u ON u.id_usuario = gm.id_usuario " +
            "WHERE gm.id_grupo=? AND gm.estado='ACTIVO' AND u.estado='ACTIVO' AND u.id_usuario <> ? " +
            "ORDER BY u.nickname ASC";

        List<UsuarioSimple_dtos> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UsuarioSimple_dtos u = new UsuarioSimple_dtos();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNickname(rs.getString("nickname"));
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    // Inserta prestamo y retorna id_prestamo
    public int crearPrestamo(Connection conn, int idGrupo, int idDueno, int idReceptor, int idVideojuego) throws SQLException {
        String sql = "INSERT INTO prestamo_grupo (id_grupo, id_usuario_dueno, id_usuario_receptor, id_videojuego) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idGrupo);
            ps.setInt(2, idDueno);
            ps.setInt(3, idReceptor);
            ps.setInt(4, idVideojuego);

            int filas = ps.executeUpdate();
            if (filas <= 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            return -1;
        }
    }

    // Inserta en biblioteca_usuario el juego prestado para el receptor
    public boolean insertarEnBibliotecaPrestado(Connection conn, int idReceptor, int idVideojuego, int idDueno) throws SQLException {
        String sql = "INSERT INTO biblioteca_usuario (id_usuario, id_videojuego, origen, id_usuario_dueno) " +
                     "VALUES (?, ?, 'PRESTADO', ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReceptor);
            ps.setInt(2, idVideojuego);
            ps.setInt(3, idDueno);
            return ps.executeUpdate() > 0;
        }
    }

    // Marca préstamo como cancelado o devuelto.
    public boolean cerrarPrestamo(Connection conn, int idPrestamo, String nuevoEstado) throws SQLException {
        String sql = "UPDATE prestamo_grupo SET estado=?, fecha_fin=CURRENT_TIMESTAMP " +
                     "WHERE id_prestamo=? AND estado='ACTIVO'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPrestamo);
            return ps.executeUpdate() > 0;
        }
    }

    // Obtiene datos mínimos del préstamo para borrar biblioteca
    public int[] obtenerPrestamoActivo(Connection conn, int idPrestamo) throws SQLException {
        String sql = "SELECT id_usuario_receptor, id_videojuego " +
                     "FROM prestamo_grupo WHERE id_prestamo=? AND estado='ACTIVO'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new int[]{ rs.getInt("id_usuario_receptor"), rs.getInt("id_videojuego") };
            }
        }
    }

    // Borra de biblioteca_usuario el juego prestado del receptor
    public boolean eliminarDeBibliotecaPrestado(Connection conn, int idReceptor, int idVideojuego) throws SQLException {
        String sql = "DELETE FROM biblioteca_usuario WHERE id_usuario=? AND id_videojuego=? AND origen='PRESTADO'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReceptor);
            ps.setInt(2, idVideojuego);
            return ps.executeUpdate() > 0;
        }
    }

  
    public interface ConnWork<T> { T run(Connection conn) throws Exception; }

    public <T> T withConn(ConnWork<T> work) {
        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;
        try {
            conn = cx.conectar();
            if (conn == null) return null;
            return work.run(conn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            cx.desconectar(conn);
        }
    }
}
