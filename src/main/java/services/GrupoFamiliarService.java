package services;

import dtos.Grupo_familiar_dtos;
import dtos.Grupo_miembro_dtos;
import dtos.Usuario_dtos;
import models.Grupo_familiar_model;
import models.Usuario_model;

import java.util.List;

public class GrupoFamiliarService {

    private final Grupo_familiar_model grupoModel = new Grupo_familiar_model();
    private final Usuario_model usuarioModel = new Usuario_model();

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

   
    public ServiceResult<List<Grupo_familiar_dtos>> listarGruposDeUsuario(int idUsuario) {
        if (idUsuario <= 0) return ServiceResult.fail(400, "idUsuario inválido.");

        List<Grupo_familiar_dtos> lista = grupoModel.listarGruposActivosDeUsuario(idUsuario);
        if (lista == null) return ServiceResult.fail(500, "No se pudo listar grupos.");
        return ServiceResult.ok(200, "OK", lista);
    }

   
    public ServiceResult<List<Grupo_miembro_dtos>> listarInvitacionesPendientes(int idUsuario) {
        if (idUsuario <= 0) return ServiceResult.fail(400, "idUsuario inválido.");

        List<Grupo_miembro_dtos> lista = grupoModel.listarInvitacionesPendientes(idUsuario);
        if (lista == null) return ServiceResult.fail(500, "No se pudo listar invitaciones.");
        return ServiceResult.ok(200, "OK", lista);
    }

    
    public ServiceResult<Integer> crearGrupo(int idCreador, String nombreGrupo) {
        if (idCreador <= 0) return ServiceResult.fail(400, "idCreador inválido.");
        if (nombreGrupo == null || nombreGrupo.trim().isEmpty()) return ServiceResult.fail(400, "El nombre del grupo es obligatorio.");
        if (nombreGrupo.trim().length() > 100) return ServiceResult.fail(400, "El nombre no puede exceder 100 caracteres.");

        Usuario_dtos u = usuarioModel.buscarPorId(idCreador);
        if (u == null) return ServiceResult.fail(404, "Usuario creador no existe.");
        if (u.getEstado() != null && !u.getEstado().equalsIgnoreCase("ACTIVO")) {
            return ServiceResult.fail(400, "El usuario creador debe estar ACTIVO.");
        }

        int idGrupo = grupoModel.crearGrupo(nombreGrupo.trim(), idCreador);
        if (idGrupo <= 0) return ServiceResult.fail(500, "No se pudo crear el grupo.");

        
        boolean okMiembro = grupoModel.insertarMiembro(idGrupo, idCreador, "CREADOR", "ACTIVO");
        if (!okMiembro) return ServiceResult.fail(500, "Grupo creado, pero no se pudo registrar al creador como miembro.");

        return ServiceResult.ok(201, "Grupo creado.", idGrupo);
    }

    
    public ServiceResult<Void> invitar(int idGrupo, int idEmisor, int idReceptor) {
        if (idGrupo <= 0) return ServiceResult.fail(400, "idGrupo inválido.");
        if (idEmisor <= 0) return ServiceResult.fail(400, "idEmisor inválido.");
        if (idReceptor <= 0) return ServiceResult.fail(400, "idReceptor inválido.");
        if (idEmisor == idReceptor) return ServiceResult.fail(400, "No puedes invitarte a ti mismo.");

        Grupo_familiar_dtos grupo = grupoModel.buscarGrupoPorId(idGrupo);
        if (grupo == null) return ServiceResult.fail(404, "Grupo no encontrado.");
        if (!"ACTIVO".equalsIgnoreCase(grupo.getEstado())) return ServiceResult.fail(400, "El grupo está INACTIVO.");

        
        if (grupo.getIdCreador() != idEmisor) return ServiceResult.fail(403, "Solo el creador del grupo puede invitar.");

        Usuario_dtos receptor = usuarioModel.buscarPorId(idReceptor);
        if (receptor == null) return ServiceResult.fail(404, "Usuario receptor no existe.");
        if (receptor.getEstado() != null && !receptor.getEstado().equalsIgnoreCase("ACTIVO")) {
            return ServiceResult.fail(400, "El usuario receptor debe estar ACTIVO.");
        }

        
        int activos = grupoModel.contarMiembrosActivos(idGrupo);
        if (activos < 0) return ServiceResult.fail(500, "No se pudo validar cupo del grupo.");
        if (activos >= 6) return ServiceResult.fail(400, "El grupo ya alcanzó el límite de 6 miembros.");

        
        Grupo_miembro_dtos existente = grupoModel.buscarMiembro(idGrupo, idReceptor);
        if (existente != null) {
            String est = existente.getEstado();
            if ("ACTIVO".equalsIgnoreCase(est)) return ServiceResult.fail(409, "El usuario ya es miembro del grupo.");
            if ("PENDIENTE".equalsIgnoreCase(est)) return ServiceResult.fail(409, "Ya existe una invitación pendiente para ese usuario.");
           
            return ServiceResult.fail(409, "El usuario ya tiene historial en el grupo (" + est + ").");
        }

        boolean ok = grupoModel.insertarMiembro(idGrupo, idReceptor, "MIEMBRO", "PENDIENTE");
        if (!ok) return ServiceResult.fail(500, "No se pudo enviar la invitación.");

        return ServiceResult.ok(200, "Invitación enviada.", null);
    }

    public ServiceResult<Void> aceptarInvitacion(int idGrupo, int idUsuario) {
        if (idGrupo <= 0) return ServiceResult.fail(400, "idGrupo inválido.");
        if (idUsuario <= 0) return ServiceResult.fail(400, "idUsuario inválido.");

        Grupo_familiar_dtos grupo = grupoModel.buscarGrupoPorId(idGrupo);
        if (grupo == null) return ServiceResult.fail(404, "Grupo no encontrado.");
        if (!"ACTIVO".equalsIgnoreCase(grupo.getEstado())) return ServiceResult.fail(400, "El grupo está INACTIVO.");

        Grupo_miembro_dtos inv = grupoModel.buscarMiembro(idGrupo, idUsuario);
        if (inv == null) return ServiceResult.fail(404, "No existe invitación para este usuario.");
        if (!"PENDIENTE".equalsIgnoreCase(inv.getEstado())) return ServiceResult.fail(400, "La invitación no está PENDIENTE.");

        int activos = grupoModel.contarMiembrosActivos(idGrupo);
        if (activos < 0) return ServiceResult.fail(500, "No se pudo validar cupo del grupo.");
        if (activos >= 6) return ServiceResult.fail(400, "El grupo ya alcanzó el límite de 6 miembros.");

        boolean ok = grupoModel.actualizarEstadoMiembro(idGrupo, idUsuario, "ACTIVO");
        if (!ok) return ServiceResult.fail(500, "No se pudo aceptar la invitación.");

        return ServiceResult.ok(200, "Invitación aceptada.", null);
    }

    public ServiceResult<Void> rechazarInvitacion(int idGrupo, int idUsuario) {
        if (idGrupo <= 0) return ServiceResult.fail(400, "idGrupo inválido.");
        if (idUsuario <= 0) return ServiceResult.fail(400, "idUsuario inválido.");

        Grupo_miembro_dtos inv = grupoModel.buscarMiembro(idGrupo, idUsuario);
        if (inv == null) return ServiceResult.fail(404, "No existe invitación para este usuario.");
        if (!"PENDIENTE".equalsIgnoreCase(inv.getEstado())) return ServiceResult.fail(400, "La invitación no está PENDIENTE.");

        boolean ok = grupoModel.actualizarEstadoMiembro(idGrupo, idUsuario, "RECHAZADO");
        if (!ok) return ServiceResult.fail(500, "No se pudo rechazar la invitación.");

        return ServiceResult.ok(200, "Invitación rechazada.", null);
    }
}
