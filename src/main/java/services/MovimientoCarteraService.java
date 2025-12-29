package services;

import dtos.Movimiento_cartera_dtos;
import models.Movimiento_cartera_model;

import java.sql.Timestamp;
import java.util.List;

public class MovimientoCarteraService {

    private final Movimiento_cartera_model model = new Movimiento_cartera_model();

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

    public ServiceResult<List<Movimiento_cartera_dtos>> listar(int idUsuarioSesion, Timestamp desde, Timestamp hasta) {
        if (idUsuarioSesion <= 0) return ServiceResult.fail(401, "No autenticado.");

        List<Movimiento_cartera_dtos> lista;
        if (desde != null && hasta != null) {
            lista = model.listarPorUsuarioYRangoFechas(idUsuarioSesion, desde, hasta);
        } else {
            lista = model.listarPorUsuario(idUsuarioSesion);
        }
        return ServiceResult.ok(200, "OK", lista);
    }
}
