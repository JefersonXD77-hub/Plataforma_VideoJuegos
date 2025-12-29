package services;

import db.ConexionMySQL;
import dtos.UsuarioSimple_dtos;
import models.Prestamo_grupo_model;

import java.sql.Connection;
import java.util.List;

public class PrestamoGrupoService {

    private final Prestamo_grupo_model model = new Prestamo_grupo_model();

    public static class ServiceResult<T> {
        public final boolean ok;
        public final int httpStatus;
        public final String mensaje;
        public final T data;

        private ServiceResult(boolean ok, int httpStatus, String mensaje, T data) {
            this.ok = ok;
            this.httpStatus = httpStatus;
            this.mensaje = mensaje;
            this.data = data;
        }
        public static <T> ServiceResult<T> ok(int httpStatus, String mensaje, T data) {
            return new ServiceResult<>(true, httpStatus, mensaje, data);
        }
        public static <T> ServiceResult<T> fail(int httpStatus, String mensaje) {
            return new ServiceResult<>(false, httpStatus, mensaje, null);
        }
    }

    public ServiceResult<List<UsuarioSimple_dtos>> miembrosDisponibles(int idGrupo, int idDueno) {
        if (idGrupo <= 0) return ServiceResult.fail(400, "idGrupo inválido.");
        if (idDueno <= 0) return ServiceResult.fail(400, "idDueno inválido.");

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return ServiceResult.fail(500, "No se pudo obtener conexión a BD.");

            if (!model.grupoActivo(conn, idGrupo)) return ServiceResult.fail(404, "Grupo no encontrado o inactivo.");
            if (!model.miembroActivo(conn, idGrupo, idDueno)) return ServiceResult.fail(403, "El dueño no es miembro ACTIVO del grupo.");

            List<UsuarioSimple_dtos> lista = model.listarMiembrosActivos(conn, idGrupo, idDueno);
            return ServiceResult.ok(200, "OK", lista);

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResult.fail(500, "Error al listar miembros.");
        } finally {
            cx.desconectar(conn);
        }
    }

    //  registrar préstamo 
    public ServiceResult<Integer> prestar(int idGrupo, int idDueno, int idReceptor, int idVideojuego) {
        if (idGrupo <= 0) return ServiceResult.fail(400, "idGrupo inválido.");
        if (idDueno <= 0) return ServiceResult.fail(400, "idDueno inválido.");
        if (idReceptor <= 0) return ServiceResult.fail(400, "idReceptor inválido.");
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");
        if (idDueno == idReceptor) return ServiceResult.fail(400, "No puedes prestarte a ti mismo.");

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return ServiceResult.fail(500, "No se pudo obtener conexión a BD.");
            conn.setAutoCommit(false);

            if (!model.grupoActivo(conn, idGrupo)) {
                conn.rollback();
                return ServiceResult.fail(404, "Grupo no encontrado o inactivo.");
            }

            // Ambos deben ser miembros activos del mismo grupo
            if (!model.miembroActivo(conn, idGrupo, idDueno)) {
                conn.rollback();
                return ServiceResult.fail(403, "El dueño no es miembro ACTIVO del grupo.");
            }
            if (!model.miembroActivo(conn, idGrupo, idReceptor)) {
                conn.rollback();
                return ServiceResult.fail(400, "El receptor no es miembro ACTIVO del grupo.");
            }

            //  validar juego prestable
            if (!model.duenoTieneJuegoPropio(conn, idDueno, idVideojuego)) {
                conn.rollback();
                return ServiceResult.fail(400, "Este juego no puede ser prestado en este momento (no es de tu propiedad).");
            }
            if (model.existePrestamoActivoDelJuego(conn, idDueno, idVideojuego)) {
                conn.rollback();
                return ServiceResult.fail(400, "Este juego no puede ser prestado en este momento (ya está prestado).");
            }

            // destinatario no debe tener el juego
            if (model.receptorYaTieneJuego(conn, idReceptor, idVideojuego)) {
                conn.rollback();
                return ServiceResult.fail(400, "El usuario ya posee este juego en su biblioteca.");
            }

            // Registrar préstamo
            int idPrestamo = model.crearPrestamo(conn, idGrupo, idDueno, idReceptor, idVideojuego);
            if (idPrestamo <= 0) {
                conn.rollback();
                return ServiceResult.fail(500, "No se pudo registrar el préstamo.");
            }

            // Actualizar biblioteca insertar PRESTADO
            boolean okBiblio = model.insertarEnBibliotecaPrestado(conn, idReceptor, idVideojuego, idDueno);
            if (!okBiblio) {
                conn.rollback();
                return ServiceResult.fail(500, "Préstamo creado, pero no se pudo actualizar la biblioteca del receptor.");
            }

            conn.commit();
            return ServiceResult.ok(201, "Préstamo registrado.", idPrestamo);

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            return ServiceResult.fail(500, "Error al registrar préstamo.");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

    // Devolver préstamo 
    public ServiceResult<Void> devolver(int idPrestamo) {
        return cerrarPrestamoGenerico(idPrestamo, "DEVUELTO");
    }

    // Cancelar préstamo 
    public ServiceResult<Void> cancelar(int idPrestamo) {
        return cerrarPrestamoGenerico(idPrestamo, "CANCELADO");
    }

    private ServiceResult<Void> cerrarPrestamoGenerico(int idPrestamo, String estado) {
        if (idPrestamo <= 0) return ServiceResult.fail(400, "idPrestamo inválido.");

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return ServiceResult.fail(500, "No se pudo obtener conexión a BD.");
            conn.setAutoCommit(false);

            int[] info = model.obtenerPrestamoActivo(conn, idPrestamo);
            if (info == null) {
                conn.rollback();
                return ServiceResult.fail(404, "Préstamo no encontrado o no está ACTIVO.");
            }

            int idReceptor = info[0];
            int idVideojuego = info[1];

            boolean okCerrar = model.cerrarPrestamo(conn, idPrestamo, estado);
            if (!okCerrar) {
                conn.rollback();
                return ServiceResult.fail(500, "No se pudo cerrar el préstamo.");
            }

          
            model.eliminarDeBibliotecaPrestado(conn, idReceptor, idVideojuego);

            conn.commit();
            return ServiceResult.ok(200, "Préstamo actualizado.", null);

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            return ServiceResult.fail(500, "Error al actualizar préstamo.");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }
}


