package services;

import dtos.Videojuegos_dtos;
import models.Videojuegos_model;
import dtos.Videojuego_imagen_dtos;
import models.Videojuego_Categoria_model;
import models.Videojuego_imagen_model;
import models.EmpresaUsuario_model;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class VideojuegoService {

    private final Videojuegos_model videojuegosModel = new Videojuegos_model();
    private final Videojuego_Categoria_model catModel = new Videojuego_Categoria_model();
    private final Videojuego_imagen_model imgModel = new Videojuego_imagen_model();
    private final EmpresaUsuario_model empUsuModel = new EmpresaUsuario_model();

    private boolean precioValido(BigDecimal p) {
        return p != null && p.compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean estadoValido(String e) {
        if (e == null) {
            return false;
        }
        String x = e.trim().toUpperCase();
        return x.equals("ACTIVO") || x.equals("SUSPENDIDO");
    }

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

    public ServiceResult<Videojuegos_dtos> obtenerPorId(String idParam) {
        Integer id = parseInt(idParam);
        if (id == null) {
            return ServiceResult.fail(400, "El parámetro 'id' debe ser numérico.");
        }

        Videojuegos_dtos juego = videojuegosModel.buscarPorId(id);
        if (juego == null) {
            return ServiceResult.fail(404, "No existe videojuego con id=" + id);
        }

        return ServiceResult.ok(200, "OK", juego);
    }

    public ServiceResult<List<Videojuegos_dtos>> buscarPorTexto(String texto) {
        if (isBlank(texto)) {
            return ServiceResult.fail(400, "El parámetro 'texto' es obligatorio.");
        }
        return ServiceResult.ok(200, "OK", videojuegosModel.buscarPorTexto(texto.trim()));
    }

    public ServiceResult<List<Videojuegos_dtos>> buscarTienda(String titulo, Integer idEmpresa, Integer idCategoria,
            BigDecimal precioMin, BigDecimal precioMax) {
        List<Videojuegos_dtos> data = videojuegosModel.buscarTienda(titulo, idEmpresa, idCategoria, precioMin, precioMax);
        return ServiceResult.ok(200, "OK", data);
    }

    public ServiceResult<List<Videojuegos_dtos>> listarPorEmpresa(String idEmpresaParam) {
        Integer idEmpresa = parseInt(idEmpresaParam);
        if (idEmpresa == null) {
            return ServiceResult.fail(400, "El parámetro 'idEmpresa' debe ser numérico.");
        }
        return ServiceResult.ok(200, "OK", videojuegosModel.listarPorEmpresa(idEmpresa));
    }

    public ServiceResult<List<Videojuegos_dtos>> listarTodos() {
        return ServiceResult.ok(200, "OK", videojuegosModel.listarJuegos());
    }

    public ServiceResult<Integer> crear(
            int idUsuarioSesion,
            boolean esAdminSistema,
            Integer idEmpresaBody,
            String titulo,
            String descripcion,
            BigDecimal precio,
            String recursosMinimos,
            Integer idClasificacion,
            String fechaLanzamientoStr,
            String estado,
            List<Integer> categorias,
            List<Videojuego_imagen_dtos> imagenes
    ) {

        Integer idEmpresa;
        if (esAdminSistema) {
            idEmpresa = idEmpresaBody;
            if (idEmpresa == null) {
                return ServiceResult.fail(400, "Como admin debe enviar idEmpresa.");
            }
        } else {
            idEmpresa = empUsuModel.obtenerEmpresaDeUsuario(idUsuarioSesion);
            if (idEmpresa == null) {
                return ServiceResult.fail(403, "Usuario Empresa sin empresa asignada.");
            }
        }

        if (isBlank(titulo) || idClasificacion == null || !precioValido(precio)) {
            return ServiceResult.fail(400, "Campos obligatorios inválidos: titulo, precio (>=0), idClasificacion.");
        }
        if (categorias == null || categorias.isEmpty()) {
            return ServiceResult.fail(400, "Debe seleccionar al menos una categoría.");
        }
        if (imagenes == null || imagenes.isEmpty()) {
            return ServiceResult.fail(400, "Debe cargar al menos una imagen representativa.");
        }

        Date fecha = parseSqlDate(fechaLanzamientoStr);
        if (fechaLanzamientoStr != null && !fechaLanzamientoStr.trim().isEmpty() && fecha == null) {
            return ServiceResult.fail(400, "fechaLanzamiento inválida. Use yyyy-MM-dd.");
        }

        if (isBlank(estado)) {
            estado = "ACTIVO";
        }
        estado = estado.trim().toUpperCase();
        if (!estadoValido(estado)) {
            return ServiceResult.fail(400, "Estado inválido. Valores permitidos: ACTIVO, SUSPENDIDO.");
        }

        Videojuegos_dtos juego = new Videojuegos_dtos();
        juego.setIdEmpresa(idEmpresa);
        juego.setTitulo(titulo.trim());
        juego.setDescripcion(isBlank(descripcion) ? null : descripcion.trim());
        juego.setPrecio(precio);
        juego.setRecursosMinimos(isBlank(recursosMinimos) ? null : recursosMinimos.trim());
        juego.setIdClasificacion(idClasificacion);
        juego.setFechaLanzamiento(fecha);
        juego.setEstado(estado);

        Videojuegos_dtos creado = videojuegosModel.insertar(juego);
        if (creado == null || creado.getIdVideojuego() <= 0) {
            return ServiceResult.fail(500, "No se pudo crear el videojuego.");
        }

        int idVid = creado.getIdVideojuego();

      
        boolean okCats;
        if (esAdminSistema) {
            okCats = catModel.reemplazarCategoriasConEstado(idVid, categorias, "APROBADA", idUsuarioSesion);
        } else {
            okCats = catModel.reemplazarCategoriasConEstado(idVid, categorias, "PENDIENTE", null);
        }
        boolean okImgs = imgModel.reemplazarImagenes(idVid, imagenes);

        if (!okCats || !okImgs) {
            return ServiceResult.fail(500, "Videojuego creado, pero falló asociar categorías/imágenes.");
        }

        return ServiceResult.ok(201, "Videojuego creado.", idVid);
    }

    public ServiceResult<Void> actualizar(
            Integer idVideojuego,
            Integer idEmpresa,
            String titulo,
            String descripcion,
            BigDecimal precio,
            String recursosMinimos,
            Integer idClasificacion,
            String fechaLanzamientoStr,
            String estado
    ) {
        if (idVideojuego == null || idEmpresa == null || isBlank(titulo) || precio == null || idClasificacion == null) {
            return ServiceResult.fail(400, "Faltan campos obligatorios: idVideojuego, idEmpresa, titulo, precio, idClasificacion.");
        }

        Videojuegos_dtos juego = new Videojuegos_dtos();
        juego.setIdVideojuego(idVideojuego);
        juego.setIdEmpresa(idEmpresa);
        juego.setTitulo(titulo.trim());
        juego.setDescripcion(isBlank(descripcion) ? null : descripcion.trim());
        juego.setPrecio(precio);
        juego.setRecursosMinimos(isBlank(recursosMinimos) ? null : recursosMinimos.trim());
        juego.setIdClasificacion(idClasificacion);

        Date fecha = parseSqlDate(fechaLanzamientoStr);
        if (fechaLanzamientoStr != null && !fechaLanzamientoStr.trim().isEmpty() && fecha == null) {
            return ServiceResult.fail(400, "fechaLanzamiento inválida. Use yyyy-MM-dd.");
        }
        juego.setFechaLanzamiento(fecha);

        if (isBlank(estado)) {
            estado = "ACTIVO";
        }
        estado = estado.trim().toUpperCase();

        if (!estadoValido(estado)) {
            return ServiceResult.fail(400, "Estado inválido. Valores permitidos: ACTIVO, SUSPENDIDO.");
        }
        juego.setEstado(estado);

        boolean ok = videojuegosModel.actualizar(juego);
        if (!ok) {
            return ServiceResult.fail(404, "No se pudo actualizar (¿existe el idVideojuego?).");
        }

        return ServiceResult.ok(200, "Videojuego actualizado.", null);
    }

    public ServiceResult<Void> cambiarEstadoConReglas(
            int idUsuarioSesion,
            boolean esAdminSistema,
            Integer idVideojuego,
            String nuevoEstado
    ) {
        if (idVideojuego == null || isBlank(nuevoEstado)) {
            return ServiceResult.fail(400, "Debe enviar idVideojuego y nuevoEstado.");
        }

        nuevoEstado = nuevoEstado.trim().toUpperCase();
        if (!estadoValido(nuevoEstado)) {
            return ServiceResult.fail(400, "Estado inválido. Valores permitidos: ACTIVO, SUSPENDIDO.");
        }

        Videojuegos_dtos juego = videojuegosModel.buscarPorId(idVideojuego);
        if (juego == null) {
            return ServiceResult.fail(404, "No existe videojuego con id=" + idVideojuego);
        }

        if (!esAdminSistema) {
            Integer idEmpresaSesion = empUsuModel.obtenerEmpresaDeUsuario(idUsuarioSesion);
            if (idEmpresaSesion == null || idEmpresaSesion != juego.getIdEmpresa()) {
                return ServiceResult.fail(403, "No puede cambiar estado de un juego que no pertenece a su empresa.");
            }
        }

        if (nuevoEstado.equals("ACTIVO")) {
            if (isBlank(juego.getTitulo()) || !precioValido(juego.getPrecio()) || juego.getIdClasificacion() <= 0) {
                return ServiceResult.fail(400, "No se puede activar: faltan datos obligatorios (título/precio/clasificación).");
            }
            if (catModel.contarCategorias(idVideojuego) <= 0) {
                return ServiceResult.fail(400, "No se puede activar un juego sin categorías.");
            }
            if (imgModel.contarImagenes(idVideojuego) <= 0) {
                return ServiceResult.fail(400, "No se puede activar un juego sin imagen representativa.");
            }
        }

        boolean ok = videojuegosModel.cambiarEstado(idVideojuego, nuevoEstado);
        if (!ok) {
            return ServiceResult.fail(500, "No se pudo cambiar el estado.");
        }

        return ServiceResult.ok(200, "Estado actualizado.", null);
    }

    private Integer parseInt(String s) {
        if (isBlank(s)) {
            return null;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Date parseSqlDate(String yyyyMmDd) {
        if (isBlank(yyyyMmDd)) {
            return null;
        }
        try {
            LocalDate ld = LocalDate.parse(yyyyMmDd.trim());
            return Date.valueOf(ld);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
