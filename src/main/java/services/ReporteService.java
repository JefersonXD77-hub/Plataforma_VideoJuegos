package services;

import dtos.ReporteResumenPlataforma_dtos;
import dtos.ReporteVentaEmpresa_dtos;
import dtos.ReporteTopJuego_dtos;
import models.Reporte_model;

import java.sql.Date;
import java.util.List;

public class ReporteService {

    private final Reporte_model model = new Reporte_model();

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

    public ServiceResult<ReporteResumenPlataforma_dtos> resumenPlataforma(Date ini, Date fin) {
        ReporteResumenPlataforma_dtos r = model.resumenPlataforma(ini, fin);
        if (r == null) return ServiceResult.fail(500, "No se pudo generar reporte.");
        return ServiceResult.ok(200, "OK", r);
    }

    public ServiceResult<List<ReporteVentaEmpresa_dtos>> ventasPorEmpresa(Date ini, Date fin) {
        return ServiceResult.ok(200, "OK", model.ventasPorEmpresa(ini, fin));
    }

    public ServiceResult<List<ReporteTopJuego_dtos>> topVideojuegos(Integer limit, Date ini, Date fin) {
        int lim = (limit == null) ? 10 : limit;
        return ServiceResult.ok(200, "OK", model.topVideojuegos(lim, ini, fin));
    }
}
