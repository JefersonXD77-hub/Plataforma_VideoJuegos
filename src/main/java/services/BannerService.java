package services;

import dtos.Banner_dtos;
import dtos.Videojuegos_dtos;
import models.Banner_model;
import models.Videojuegos_model;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class BannerService {

    private final Banner_model bannerModel = new Banner_model();
    private final Videojuegos_model videojuegosModel = new Videojuegos_model();

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

    public ServiceResult<List<Banner_dtos>> listarActivosVigentes() {
        Date hoy = Date.valueOf(LocalDate.now());
        List<Banner_dtos> lista = bannerModel.listarActivosVigentes(hoy);
        if (lista == null) {
            return ServiceResult.fail(500, "No se pudo listar banner.");
        }
        return ServiceResult.ok(200, "OK", lista);
    }

    public ServiceResult<List<Banner_dtos>> listarTodos() {
        List<Banner_dtos> lista = bannerModel.listarTodos();
        if (lista == null) {
            return ServiceResult.fail(500, "No se pudo listar banners.");
        }
        return ServiceResult.ok(200, "OK", lista);
    }

    public ServiceResult<Void> crear(Banner_dtos dto) {
        String v = validar(dto);
        if (v != null) {
            return ServiceResult.fail(400, v);
        }

        if (dto.isActivo()) {
            int activos = bannerModel.contarActivosExcluyendo(null);
            if (activos >= 5) {
                return ServiceResult.fail(400, "El banner ya tiene 5 elementos activos. Desactive uno antes de agregar otro.");
            }
        }

        String vj = validarVideojuegoActivo(dto.getIdVideojuego());
        if (vj != null) {
            return ServiceResult.fail(400, vj);
        }

        boolean ok = bannerModel.crear(dto);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo crear el banner.");
        }
        return ServiceResult.ok(201, "Banner creado.", null);
    }

    public ServiceResult<Void> actualizar(Banner_dtos dto) {
        if (dto == null || dto.getIdBanner() <= 0) {
            return ServiceResult.fail(400, "idBanner es obligatorio.");
        }

        Banner_dtos existente = bannerModel.buscarPorId(dto.getIdBanner());
        if (existente == null) {
            return ServiceResult.fail(404, "Banner no encontrado.");
        }

        String v = validar(dto);
        if (v != null) {
            return ServiceResult.fail(400, v);
        }

        if (dto.isActivo()) {
            int activos = bannerModel.contarActivosExcluyendo(dto.getIdBanner());
            if (activos < 0) {
                return ServiceResult.fail(500, "No se pudo validar el cupo de elementos activos del banner.");
            }
            if (activos >= 5) {
                return ServiceResult.fail(400, "El banner ya tiene 5 elementos activos. Desactive uno antes de activar éste.");
            }

        }

        String vj = validarVideojuegoActivo(dto.getIdVideojuego());
        if (vj != null) {
            return ServiceResult.fail(400, vj);
        }

        boolean ok = bannerModel.actualizar(dto);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo actualizar el banner.");
        }
        return ServiceResult.ok(200, "Banner actualizado.", null);
    }

    public ServiceResult<Void> setActivo(int idBanner, boolean activo) {
        if (idBanner <= 0) {
            return ServiceResult.fail(400, "idBanner inválido.");
        }

        Banner_dtos existente = bannerModel.buscarPorId(idBanner);
        if (existente == null) {
            return ServiceResult.fail(404, "Banner no encontrado.");
        }

        if (activo) {

            int activos = bannerModel.contarActivosExcluyendo(idBanner);
            if (activos < 0) {
                return ServiceResult.fail(500, "No se pudo validar el cupo de elementos activos del banner.");
            }
            if (activos >= 5) {
                return ServiceResult.fail(400, "El banner ya tiene 5 elementos activos.");
            }

        }

        if (activo) {
            String vj = validarVideojuegoActivo(existente.getIdVideojuego());
            if (vj != null) {
                return ServiceResult.fail(400, vj);
            }
        }

        boolean ok = bannerModel.setActivo(idBanner, activo);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo actualizar el estado del banner.");
        }
        return ServiceResult.ok(200, "Estado actualizado.", null);
    }

    public ServiceResult<Void> eliminar(int idBanner) {
        if (idBanner <= 0) {
            return ServiceResult.fail(400, "idBanner inválido.");
        }

        Banner_dtos existente = bannerModel.buscarPorId(idBanner);
        if (existente == null) {
            return ServiceResult.fail(404, "Banner no encontrado.");
        }

        boolean ok = bannerModel.eliminar(idBanner);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo eliminar el banner.");
        }
        return ServiceResult.ok(200, "Banner eliminado.", null);
    }

    private String validar(Banner_dtos dto) {
        if (dto == null) {
            return "Debe enviar el objeto banner.";
        }

        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty()) {
            return "El título es obligatorio.";
        }
        if (dto.getTitulo().trim().length() > 120) {
            return "El título no puede exceder 120 caracteres.";
        }

        if (dto.getUrlImagen() == null || dto.getUrlImagen().trim().isEmpty()) {
            return "La URL de imagen es obligatoria.";
        }
        if (dto.getUrlImagen().trim().length() > 255) {
            return "La URL de imagen no puede exceder 255 caracteres.";
        }

        if (dto.getDescripcion() != null && dto.getDescripcion().length() > 500) {
            return "La descripción no puede exceder 500 caracteres.";
        }

        if (dto.getFechaInicio() != null && dto.getFechaFin() != null) {
            if (dto.getFechaInicio().after(dto.getFechaFin())) {
                return "fechaInicio no puede ser posterior a fechaFin.";
            }
        }

        dto.setTitulo(dto.getTitulo().trim());
        dto.setUrlImagen(dto.getUrlImagen().trim());
        if (dto.getDescripcion() != null) {
            dto.setDescripcion(dto.getDescripcion().trim());
        }
        return null;
    }

    private String validarVideojuegoActivo(Integer idVideojuego) {
        if (idVideojuego == null || idVideojuego <= 0) {
            return "Debe enviar idVideojuego.";
        }
        Videojuegos_dtos vj = videojuegosModel.buscarPorId(idVideojuego);
        if (vj == null) {
            return "El videojuego no existe.";
        }
        if (vj.getEstado() == null || !vj.getEstado().equalsIgnoreCase("ACTIVO")) {
            return "El videojuego debe estar ACTIVO para estar en el banner.";
        }
        return null;
    }
}
