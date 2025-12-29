package services;

import dtos.BibliotecaItemView_dtos;
import models.Biblioteca_model;

import java.util.List;

public class BibliotecaService {

    private final Biblioteca_model model = new Biblioteca_model();

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

    public ServiceResult<List<BibliotecaItemView_dtos>> miBiblioteca(int idUsuarioSesion) {
        if (idUsuarioSesion <= 0) {
            return ServiceResult.fail(401, "No autenticado.");
        }

        List<BibliotecaItemView_dtos> lista = model.listarMiBiblioteca(idUsuarioSesion);
        return ServiceResult.ok(200, "OK", lista);
    }
}
