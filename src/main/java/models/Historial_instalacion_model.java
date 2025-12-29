package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Historial_instalacion_model {

    public boolean estaInstalado(Connection conn, int idUsuario, int idVideojuego, String origen) throws SQLException {
        String sql = ""
                + "SELECT 1 "
                + "FROM historial_instalacion "
                + "WHERE id_usuario=? AND id_videojuego=? AND origen=? AND fecha_desinstalacion IS NULL "
                + "ORDER BY fecha_instalacion DESC "
                + "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.setString(3, origen);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Cuenta juegos prestados instalados 
    public int contarPrestadosInstalados(Connection conn, int idUsuario) throws SQLException {
        String sql = ""
                + "SELECT COUNT(DISTINCT id_videojuego) "
                + "FROM historial_instalacion "
                + "WHERE id_usuario=? AND origen='PRESTADO' AND fecha_desinstalacion IS NULL";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean insertarInstalacion(Connection conn, int idUsuario, int idVideojuego, String origen) throws SQLException {
        String sql = ""
                + "INSERT INTO historial_instalacion (id_usuario, id_videojuego, origen, fecha_instalacion) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.setString(3, origen);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean desinstalarActual(Connection conn, int idUsuario, int idVideojuego, String origen) throws SQLException {
        Integer idHist = obtenerIdInstalacionActivaMasReciente(conn, idUsuario, idVideojuego, origen);
        if (idHist == null) return false;

        String sql = ""
                + "UPDATE historial_instalacion "
                + "SET fecha_desinstalacion = CURRENT_TIMESTAMP "
                + "WHERE id_historial = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idHist);
            return ps.executeUpdate() > 0;
        }
    }

    private Integer obtenerIdInstalacionActivaMasReciente(Connection conn, int idUsuario, int idVideojuego, String origen) throws SQLException {
        String sql = ""
                + "SELECT id_historial "
                + "FROM historial_instalacion "
                + "WHERE id_usuario=? AND id_videojuego=? AND origen=? AND fecha_desinstalacion IS NULL "
                + "ORDER BY fecha_instalacion DESC "
                + "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            ps.setString(3, origen);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
