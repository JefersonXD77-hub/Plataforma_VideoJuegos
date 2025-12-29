package services;

import dtos.Categoria_dtos;
import models.Categoria_model;

import java.util.List;

public class CategoriaService {

    private final Categoria_model categoriaModel = new Categoria_model();

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

    public ServiceResult<List<Categoria_dtos>> listarTodas(String estado) {
        List<Categoria_dtos> lista = categoriaModel.listarTodas();
        if (lista == null) {
            return ServiceResult.fail(500, "No se pudo listar categorías.");
        }
        if (estado != null && !estado.trim().isEmpty()) {
            String e = estado.trim().toUpperCase();
            lista.removeIf(c -> c.getEstado() == null || !c.getEstado().equalsIgnoreCase(e));
        }
        return ServiceResult.ok(200, "OK", lista);
    }

    public ServiceResult<Categoria_dtos> buscarPorId(int idCategoria) {
        if (idCategoria <= 0) {
            return ServiceResult.fail(400, "idCategoria inválido.");
        }
        Categoria_dtos c = categoriaModel.buscarPorId(idCategoria);
        if (c == null) {
            return ServiceResult.fail(404, "Categoría no encontrada.");
        }
        return ServiceResult.ok(200, "OK", c);
    }

    public ServiceResult<Categoria_dtos> crear(Categoria_dtos dto) {
        String v = validar(dto, true);
        if (v != null) {
            return ServiceResult.fail(400, v);
        }

       
        if (categoriaModel.existeNombre(dto.getNombre())) {
            return ServiceResult.fail(409, "Ya existe una categoría con ese nombre.");
        }

        Categoria_dtos creada = categoriaModel.insertar(dto);
        if (creada == null) {
            return ServiceResult.fail(500, "No se pudo crear la categoría.");
        }
        return ServiceResult.ok(201, "Categoría creada.", creada);
    }

    public ServiceResult<Void> actualizar(Categoria_dtos dto) {
        if (dto == null || dto.getIdCategoria() <= 0) {
            return ServiceResult.fail(400, "idCategoria es obligatorio.");
        }
        String v = validar(dto, false);
        if (v != null) {
            return ServiceResult.fail(400, v);
        }

        Categoria_dtos existente = categoriaModel.buscarPorId(dto.getIdCategoria());
        if (existente == null) {
            return ServiceResult.fail(404, "Categoría no encontrada.");
        }

        if (dto.getEstado() == null || dto.getEstado().trim().isEmpty()) {
            dto.setEstado(existente.getEstado());
        }

       
        if (dto.getNombre() != null && !dto.getNombre().trim().equalsIgnoreCase(existente.getNombre())) {
            if (categoriaModel.existeNombre(dto.getNombre())) {
                return ServiceResult.fail(409, "Ya existe una categoría con ese nombre.");
            }
        }

        boolean ok = categoriaModel.actualizar(dto);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo actualizar la categoría.");
        }
        return ServiceResult.ok(200, "Categoría actualizada.", null);
    }

    public ServiceResult<Void> cambiarEstado(int idCategoria, String nuevoEstado) {
        if (idCategoria <= 0) {
            return ServiceResult.fail(400, "idCategoria inválido.");
        }
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return ServiceResult.fail(400, "nuevoEstado es obligatorio.");
        }

        String est = nuevoEstado.trim().toUpperCase();
        if (!est.equals("ACTIVA") && !est.equals("INACTIVA")) {
            return ServiceResult.fail(400, "Estado inválido. Use ACTIVA o INACTIVA.");
        }

        Categoria_dtos existente = categoriaModel.buscarPorId(idCategoria);
        if (existente == null) {
            return ServiceResult.fail(404, "Categoría no encontrada.");
        }

        boolean ok = categoriaModel.cambiarEstado(idCategoria, est);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo cambiar el estado.");
        }
        return ServiceResult.ok(200, "Estado actualizado.", null);
    }

    private String validar(Categoria_dtos dto, boolean crear) {
        if (dto == null) {
            return "Debe enviar el objeto categoría.";
        }

        String nombre = dto.getNombre();
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.trim().length() > 50) {
            return "El nombre no puede exceder 50 caracteres.";
        }

        String desc = dto.getDescripcion();
        if (desc != null && desc.length() > 500) {
            return "La descripción no puede exceder 500 caracteres.";
        }

        if (crear) {
            if (dto.getEstado() == null || dto.getEstado().trim().isEmpty()) {
                dto.setEstado("ACTIVA");
            }
        } else {
            if (dto.getEstado() != null && !dto.getEstado().trim().isEmpty()) {
                String e = dto.getEstado().trim().toUpperCase();
                if (!e.equals("ACTIVA") && !e.equals("INACTIVA")) {
                    return "Estado inválido. Use ACTIVA o INACTIVA.";
                }
                dto.setEstado(e);
            }
        }

        dto.setNombre(nombre.trim());
        if (dto.getDescripcion() != null) {
            dto.setDescripcion(dto.getDescripcion().trim());
        }
        return null;
    }
}
