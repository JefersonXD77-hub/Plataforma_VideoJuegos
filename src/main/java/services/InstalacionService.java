package services;

import db.ConexionMySQL;
import models.Historial_instalacion_model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InstalacionService {

    private final Historial_instalacion_model histModel = new Historial_instalacion_model();

    public ServiceResult<Void> instalar(int idUsuario, int idVideojuego) {
        if (idUsuario <= 0) return ServiceResult.fail(401, "No autenticado.");
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return ServiceResult.fail(500, "No se pudo conectar a BD.");
            conn.setAutoCommit(false);

            String origen = obtenerOrigenBiblioteca(conn, idUsuario, idVideojuego);
            if (origen == null) {
                conn.rollback();
                return ServiceResult.fail(404, "El videojuego no está en tu biblioteca.");
            }

           
            if (histModel.estaInstalado(conn, idUsuario, idVideojuego, origen)) {
                conn.commit();
                return ServiceResult.ok(200, "Ya está instalado.", null);
            }

            if ("PRESTADO".equalsIgnoreCase(origen)) {
                int max = obtenerMaxPrestadosInstalados(conn); 
                int actuales = histModel.contarPrestadosInstalados(conn, idUsuario);

                if (actuales >= max) {
                    conn.rollback();
                    return ServiceResult.fail(400, "No puedes instalar más juegos prestados. Desinstala el juego prestado actual primero.");
                }
            }

            boolean ok = histModel.insertarInstalacion(conn, idUsuario, idVideojuego, origen);
            if (!ok) {
                conn.rollback();
                return ServiceResult.fail(500, "No se pudo registrar la instalación.");
            }

            conn.commit();
            return ServiceResult.ok(200, "Instalación registrada.", null);

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return ServiceResult.fail(500, "Error al instalar: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }

    public ServiceResult<Void> desinstalar(int idUsuario, int idVideojuego) {
        if (idUsuario <= 0) return ServiceResult.fail(401, "No autenticado.");
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return ServiceResult.fail(500, "No se pudo conectar a BD.");
            conn.setAutoCommit(false);

            String origen = obtenerOrigenBiblioteca(conn, idUsuario, idVideojuego);
            if (origen == null) {
                conn.rollback();
                return ServiceResult.fail(404, "El videojuego no está en tu biblioteca.");
            }

            boolean ok = histModel.desinstalarActual(conn, idUsuario, idVideojuego, origen);
            if (!ok) {
                conn.rollback();
                return ServiceResult.fail(400, "El videojuego no está instalado actualmente.");
            }

            conn.commit();
            return ServiceResult.ok(200, "Desinstalación registrada.", null);

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return ServiceResult.fail(500, "Error al desinstalar: " + e.getMessage());
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            cx.desconectar(conn);
        }
    }

    private String obtenerOrigenBiblioteca(Connection conn, int idUsuario, int idVideojuego) throws Exception {
        String sql = "SELECT origen FROM biblioteca_usuario WHERE id_usuario=? AND id_videojuego=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idVideojuego);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private int obtenerMaxPrestadosInstalados(Connection conn) throws Exception {
        String sql = "SELECT valor_numerico FROM parametro_sistema WHERE clave='MAX_JUEGOS_PRESTADOS'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                java.math.BigDecimal v = rs.getBigDecimal(1);
                if (v != null) {
                    int n = v.intValue();
                    return (n <= 0) ? 1 : n;
                }
            }
        }
        return 1;
    }
}
