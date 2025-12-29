package services;

import dtos.Comentario_videojuego_dtos;
import models.Comentario_videojuego_model;

import java.util.List;

public class ComentarioService {

    private final Comentario_videojuego_model model = new Comentario_videojuego_model();

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

    public ServiceResult<List<Comentario_videojuego_dtos>> listarPublico(int idVideojuego) {
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");
        if (!model.existeVideojuego(idVideojuego)) return ServiceResult.fail(404, "Videojuego no existe.");

        List<Comentario_videojuego_dtos> lista = model.listarPublicoPorVideojuego(idVideojuego);
        if (lista == null) return ServiceResult.fail(500, "No se pudieron listar comentarios.");
        return ServiceResult.ok(200, "OK", lista);
    }

    
    public ServiceResult<List<Comentario_videojuego_dtos>> listarAdmin(int idVideojuego) {
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");
        if (!model.existeVideojuego(idVideojuego)) return ServiceResult.fail(404, "Videojuego no existe.");

        List<Comentario_videojuego_dtos> lista = model.listarAdminPorVideojuego(idVideojuego);
        if (lista == null) return ServiceResult.fail(500, "No se pudieron listar comentarios.");
        return ServiceResult.ok(200, "OK", lista);
    }

    public ServiceResult<Integer> crear(Comentario_videojuego_dtos dto) {
        String v = validar(dto);
        if (v != null) return ServiceResult.fail(400, v);

        if (!model.existeVideojuego(dto.getIdVideojuego())) {
            return ServiceResult.fail(404, "Videojuego no existe.");
        }

        if (!model.usuarioPoseeJuegoPropio(dto.getIdUsuario(), dto.getIdVideojuego())) {
            return ServiceResult.fail(403, "Solo puedes comentar videojuegos que has comprado.");
        }

        if (dto.getIdComentarioPadre() != null) {
            Comentario_videojuego_dtos padre = model.buscarPorId(dto.getIdComentarioPadre());
            if (padre == null) return ServiceResult.fail(404, "Comentario padre no existe.");
            if (padre.getIdVideojuego() != dto.getIdVideojuego()) {
                return ServiceResult.fail(400, "El comentario padre no pertenece a ese videojuego.");
            }
        }

        int id = model.crearComentario(dto);
        if (id <= 0) return ServiceResult.fail(500, "No se pudo crear el comentario.");
        return ServiceResult.ok(201, "Comentario creado.", id);
    }

    public ServiceResult<Void> setVisibleEmpresa(int idComentario, int idEmpresa, boolean visible) {
        if (idComentario <= 0) return ServiceResult.fail(400, "idComentario inválido.");
        if (idEmpresa <= 0) return ServiceResult.fail(400, "idEmpresa inválido.");

        Comentario_videojuego_dtos c = model.buscarPorId(idComentario);
        if (c == null) return ServiceResult.fail(404, "Comentario no encontrado.");

        Integer empVj = model.obtenerEmpresaDeVideojuego(c.getIdVideojuego());
        if (empVj == null) return ServiceResult.fail(404, "Videojuego no encontrado.");
        if (empVj != idEmpresa) return ServiceResult.fail(403, "No autorizado para moderar comentarios de este videojuego.");

        boolean ok = model.setVisibleEmpresa(idComentario, visible);
        if (!ok) return ServiceResult.fail(500, "No se pudo actualizar visibilidad (empresa).");
        return ServiceResult.ok(200, "Actualizado.", null);
    }

    public ServiceResult<Void> setVisibleAdmin(int idComentario, boolean visible) {
        if (idComentario <= 0) return ServiceResult.fail(400, "idComentario inválido.");

        Comentario_videojuego_dtos c = model.buscarPorId(idComentario);
        if (c == null) return ServiceResult.fail(404, "Comentario no encontrado.");

        boolean ok = model.setVisibleAdmin(idComentario, visible);
        if (!ok) return ServiceResult.fail(500, "No se pudo actualizar visibilidad (admin).");
        return ServiceResult.ok(200, "Actualizado.", null);
    }

    private String validar(Comentario_videojuego_dtos dto) {
        if (dto == null) return "Debe enviar el objeto comentario.";
        if (dto.getIdVideojuego() <= 0) return "idVideojuego inválido.";
        if (dto.getIdUsuario() <= 0) return "idUsuario inválido.";
        if (dto.getTexto() == null || dto.getTexto().trim().isEmpty()) return "El texto es obligatorio.";
        if (dto.getTexto().trim().length() > 2000) return "El texto no puede exceder 2000 caracteres.";
        dto.setTexto(dto.getTexto().trim());
        return null;
    }
}
