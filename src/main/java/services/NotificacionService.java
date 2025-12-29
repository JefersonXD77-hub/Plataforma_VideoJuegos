package services;

import dtos.Notificacion_dtos;
import models.NotificacionDerivada_model;

import java.util.List;

public class NotificacionService {

    private final NotificacionDerivada_model model = new NotificacionDerivada_model();

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

    public ServiceResult<List<Notificacion_dtos>> listar(int idUsuarioSesion) {
        if (idUsuarioSesion <= 0) return ServiceResult.fail(401, "No autenticado.");
        List<Notificacion_dtos> list = model.listarNotificaciones(idUsuarioSesion, 20, 20);
        return ServiceResult.ok(200, "OK", list);
    }
}
