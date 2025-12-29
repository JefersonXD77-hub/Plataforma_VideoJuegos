package services;

import dtos.Cartera_dtos;
import models.Cartera_model;

import java.math.BigDecimal;

public class CarteraService {

    private final Cartera_model carteraModel = new Cartera_model();

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

    public ServiceResult<Cartera_dtos> obtenerMiCartera(int idUsuarioSesion) {
        if (idUsuarioSesion <= 0) {
            return ServiceResult.fail(400, "Usuario inválido.");
        }

        Cartera_dtos c = carteraModel.buscarPorIdUsuario(idUsuarioSesion);
        if (c == null) {
            return ServiceResult.fail(404, "No existe cartera para el usuario.");
        }

        return ServiceResult.ok(200, "OK", c);
    }

    public ServiceResult<Cartera_dtos> recargar(int idUsuarioSesion, BigDecimal monto, String descripcion) {
        if (idUsuarioSesion <= 0) {
            return ServiceResult.fail(400, "Usuario inválido.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return ServiceResult.fail(400, "El monto debe ser mayor a 0.");
        }

      

        boolean ok = carteraModel.recargarSaldo(idUsuarioSesion, monto, descripcion);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo recargar la cartera.");
        }

        Cartera_dtos c = carteraModel.buscarPorIdUsuario(idUsuarioSesion);
        if (c == null) {
            return ServiceResult.ok(200, "Recarga aplicada.", null);
        }

        return ServiceResult.ok(200, "Recarga aplicada.", c);
    }
}

