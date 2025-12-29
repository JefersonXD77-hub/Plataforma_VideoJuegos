package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import dtos.Videojuego_imagen_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.VideojuegoService;
import services.VideojuegoService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/videojuegos")
public class VideojuegoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int ROL_ADMIN_SISTEMA = 1;
    private static final int ROL_USUARIO_EMPRESA = 2;

    private final Gson gson = new Gson();
    private final VideojuegoService service = new VideojuegoService();

    private List<Integer> getAsIntList(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull() || !obj.get(field).isJsonArray()) {
            return null;
        }
        List<Integer> out = new ArrayList<>();
        JsonArray arr = obj.getAsJsonArray(field);
        for (JsonElement el : arr) {
            try {
                out.add(el.getAsInt());
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private List<Videojuego_imagen_dtos> getAsImagenList(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull() || !obj.get(field).isJsonArray()) {
            return null;
        }
        List<Videojuego_imagen_dtos> out = new ArrayList<>();
        JsonArray arr = obj.getAsJsonArray(field);

        for (JsonElement el : arr) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject j = el.getAsJsonObject();

            Videojuego_imagen_dtos dto = new Videojuego_imagen_dtos();
            dto.setUrlImagen(j.has("urlImagen") && !j.get("urlImagen").isJsonNull() ? j.get("urlImagen").getAsString() : null);
            dto.setEsPortada(j.has("esPortada") && !j.get("esPortada").isJsonNull() && j.get("esPortada").getAsBoolean());
            dto.setOrden(j.has("orden") && !j.get("orden").isJsonNull() ? safeInt(j.get("orden")) : null);

            out.add(dto);
        }
        return out;
    }

    private Integer safeInt(JsonElement el) {
        try {
            return el.getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        String texto = request.getParameter("texto");
        String idEmpresa = request.getParameter("idEmpresa");

        ServiceResult<?> r;
        if (id != null && !id.isBlank()) {
            r = service.obtenerPorId(id);
        } else if (texto != null && !texto.isBlank()) {
            r = service.buscarPorTexto(texto);
        } else if (idEmpresa != null && !idEmpresa.isBlank()) {
            r = service.listarPorEmpresa(idEmpresa);
        } else {
            r = service.listarTodos();
        }

        escribirJsonEstandar(response, r);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) Sesión requerida
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        // 2) Rol requerido
        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");
        boolean rolPermitido = (usuario.getIdRol() == ROL_ADMIN_SISTEMA || usuario.getIdRol() == ROL_USUARIO_EMPRESA);
        if (!rolPermitido) {
            escribirJsonError(response, 403, "No autorizado para esta operación.");
            return;
        }

        // 3) Leer body JSON
        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception e) {
            body = null;
        }
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON vacío o inválido.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null) {
            escribirJsonError(response, 400, "Debe enviar el campo 'accion' (crear/actualizar/cambiarEstado).");
            return;
        }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {
            case "crear":
                boolean esAdmin = (usuario.getIdRol() == ROL_ADMIN_SISTEMA);
                r = service.crear(
                        usuario.getIdUsuario(),
                        esAdmin,
                        getAsInteger(body, "idEmpresa"), 
                        getAsString(body, "titulo"),
                        getAsString(body, "descripcion"),
                        getAsBigDecimalFlexible(body, "precio"),
                        getAsString(body, "recursosMinimos"),
                        getAsInteger(body, "idClasificacion"),
                        getAsString(body, "fechaLanzamiento"),
                        getAsString(body, "estado"),
                        getAsIntList(body, "categorias"),
                        getAsImagenList(body, "imagenes")
                );
                break;

            case "actualizar":
                r = service.actualizar(
                        getAsInteger(body, "idVideojuego"),
                        getAsInteger(body, "idEmpresa"),
                        getAsString(body, "titulo"),
                        getAsString(body, "descripcion"),
                        getAsBigDecimalFlexible(body, "precio"),
                        getAsString(body, "recursosMinimos"),
                        getAsInteger(body, "idClasificacion"),
                        getAsString(body, "fechaLanzamiento"),
                        getAsString(body, "estado")
                );
                break;

            case "cambiarestado":
                boolean esAdmin2 = (usuario.getIdRol() == ROL_ADMIN_SISTEMA);
                r = service.cambiarEstadoConReglas(
                        usuario.getIdUsuario(),
                        esAdmin2,
                        getAsInteger(body, "idVideojuego"),
                        getAsString(body, "nuevoEstado")
                );
                break;

            default:
                escribirJsonError(response, 400, "Acción no soportada: " + accion);
                return;
        }

        escribirJsonEstandar(response, r);
    }

    private void escribirJsonEstandar(HttpServletResponse response, ServiceResult<?> r) throws IOException {
        JsonObject out = new JsonObject();
        out.addProperty("ok", r.ok);
        out.addProperty("mensaje", r.mensaje);

        if (r.data != null) {
            out.add("data", gson.toJsonTree(r.data));
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(r.httpStatus);

        try ( PrintWriter pw = response.getWriter()) {
            pw.print(gson.toJson(out));
        }
    }

    private void escribirJsonError(HttpServletResponse response, int status, String mensaje) throws IOException {
        JsonObject out = new JsonObject();
        out.addProperty("ok", false);
        out.addProperty("mensaje", mensaje);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        try ( PrintWriter pw = response.getWriter()) {
            pw.print(gson.toJson(out));
        }
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(field).getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    
    private BigDecimal getAsBigDecimalFlexible(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(field).getAsBigDecimal();
        } catch (Exception e) {
            try {
                String s = obj.get(field).getAsString();
                if (s == null) {
                    return null;
                }
                s = s.trim();
                if (s.isEmpty()) {
                    return null;
                }
                return new BigDecimal(s);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
