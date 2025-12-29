package services;

import db.ConexionMySQL;
import dtos.Parametro_sistema_dtos;
import models.Empresa_model;
import models.Parametro_sistema_model;

import java.math.BigDecimal;
import java.sql.Connection;

public class ComisionService {

    public static final String CLAVE_COMISION_GLOBAL = "COMISION_GLOBAL";

    private final Parametro_sistema_model parametroModel = new Parametro_sistema_model();
    private final Empresa_model empresaModel = new Empresa_model();

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

    public ServiceResult<BigDecimal> getGlobal() {
        Parametro_sistema_dtos p = parametroModel.buscarPorClave(CLAVE_COMISION_GLOBAL);
        if (p == null || p.getValorNumerico() == null) {
            return ServiceResult.ok(200, "OK", BigDecimal.ZERO);
        }
        return ServiceResult.ok(200, "OK", p.getValorNumerico());
    }

  
    public ServiceResult<Void> setGlobal(BigDecimal nuevaGlobal) {
        String v = validarPorcentaje(nuevaGlobal);
        if (v != null) return ServiceResult.fail(400, v);

        ConexionMySQL cx = new ConexionMySQL();
        Connection conn = null;

        try {
            conn = cx.conectar();
            if (conn == null) return ServiceResult.fail(500, "No se pudo obtener conexión a BD.");

            conn.setAutoCommit(false);

            boolean ok = parametroModel.upsertValorNumerico(conn, CLAVE_COMISION_GLOBAL, nuevaGlobal);
            if (!ok) {
                conn.rollback();
                return ServiceResult.fail(500, "No se pudo actualizar la comisión global.");
            }

           
            empresaModel.clampComisionesMayoresA(conn, nuevaGlobal);

            conn.commit();
            return ServiceResult.ok(200, "Comisión global actualizada.", null);

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            System.out.println("Error en ComisionService.setGlobal(): " + e.getMessage());
            e.printStackTrace();
            return ServiceResult.fail(500, "Error al actualizar comisión global.");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) {}
            cx.desconectar(conn);
        }
    }

   
    public ServiceResult<BigDecimal> getComisionEmpresa(int idEmpresa) {
        if (idEmpresa <= 0) return ServiceResult.fail(400, "idEmpresa inválido.");

        
        if (!empresaModel.existeEmpresa(idEmpresa)) {
            return ServiceResult.fail(404, "Empresa no encontrada.");
        }
        BigDecimal com = empresaModel.obtenerComisionEmpresa(idEmpresa);

        if (com == null) com = getGlobal().data;

        return ServiceResult.ok(200, "OK", com);
    }

    public ServiceResult<Void> setComisionEmpresa(int idEmpresa, BigDecimal nuevaComision) {
        if (idEmpresa <= 0) return ServiceResult.fail(400, "idEmpresa inválido.");
        String v = validarPorcentaje(nuevaComision);
        if (v != null) return ServiceResult.fail(400, v);

        if (!empresaModel.existeEmpresa(idEmpresa)) {
            return ServiceResult.fail(404, "Empresa no encontrada.");
        }

        BigDecimal global = getGlobal().data;
        if (nuevaComision.compareTo(global) > 0) {
            return ServiceResult.fail(400, "La comisión de empresa no puede ser mayor a la comisión global (" + global + ").");
        }

        boolean ok = empresaModel.actualizarSoloComision(idEmpresa, nuevaComision);
        if (!ok) return ServiceResult.fail(500, "No se pudo actualizar la comisión de la empresa.");
        return ServiceResult.ok(200, "Comisión de empresa actualizada.", null);
    }

    private String validarPorcentaje(BigDecimal v) {
        if (v == null) return "Debe enviar el valor.";
        if (v.compareTo(BigDecimal.ZERO) < 0) return "El porcentaje no puede ser negativo.";
        if (v.compareTo(new BigDecimal("100")) > 0) return "El porcentaje no puede ser mayor a 100.";
        return null;
    }
}
