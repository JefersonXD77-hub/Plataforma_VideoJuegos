package services;

import models.Compra_model;

import java.util.List;
import java.sql.Timestamp;
import dtos.CompraView_dtos;

public class CompraService {

    private final Compra_model compraModel = new Compra_model();

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

    public ServiceResult<Integer> comprar(int idUsuarioSesion, List<Integer> idsVideojuego, Timestamp fechaCompra) {
        if (idUsuarioSesion <= 0) {
            return ServiceResult.fail(401, "No autenticado.");
        }
        if (idsVideojuego == null || idsVideojuego.isEmpty()) {
            return ServiceResult.fail(400, "Debe enviar al menos un videojuego.");
        }

        Compra_model.CompraResult r = compraModel.comprarVideojuegos(idUsuarioSesion, idsVideojuego, fechaCompra);
        if (!r.ok) {
            return ServiceResult.fail(400, r.mensaje);
        }

        return ServiceResult.ok(201, "Compra realizada.", r.idCompra);
    }

    
    public ServiceResult<Integer> comprar(int idUsuarioSesion, List<Integer> idsVideojuego) {
        return comprar(idUsuarioSesion, idsVideojuego, null);
    }

public ServiceResult<List<CompraView_dtos>> misCompras(int idUsuarioSesion) {
        if (idUsuarioSesion <= 0) {
            return ServiceResult.fail(401, "No autenticado.");
        }

        List<CompraView_dtos> lista = compraModel.listarHistorialCompras(idUsuarioSesion);
        return ServiceResult.ok(200, "OK", lista);
    }

}