package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtos.Categoria_dtos;
import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import services.CategoriaService;
import services.CategoriaService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/categorias")
public class CategoriaController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int ROL_ADMIN_SISTEMA = 1;

    private final Gson gson = new Gson();
    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String idStr = request.getParameter("idCategoria");
        if (idStr != null && !idStr.trim().isEmpty()) {
            int id;
            try { id = Integer.parseInt(idStr.trim()); }
            catch (Exception e) { escribirJsonError(response, 400, "idCategoria inválido."); return; }

            ServiceResult<Categoria_dtos> r = categoriaService.buscarPorId(id);
            escribirResultado(response, r);
            return;
        }

        String estado = request.getParameter("estado");
        ServiceResult<?> r = categoriaService.listarTodas(estado);
        escribirResultado(response, r);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("usuarioActual") == null) {
    escribirJsonError(response, 401, "No autenticado.");
    return;
}
Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");
if (usuario.getIdRol() != ROL_ADMIN_SISTEMA) {
    escribirJsonError(response, 403, "Solo administrador.");
    return;
}

        JsonObject body = leerJsonBody(request);
        if (body == null) { escribirJsonError(response, 400, "Body JSON inválido."); return; }

        String accion = getAsString(body, "accion");
        if (accion == null) { escribirJsonError(response, 400, "Debe enviar el campo 'accion'."); return; }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "crear": {
                Categoria_dtos dto = new Categoria_dtos();
                dto.setNombre(getAsString(body, "nombre"));
                dto.setDescripcion(getAsString(body, "descripcion"));
                dto.setEstado(getAsString(body, "estado"));
                r = categoriaService.crear(dto);
                break;
            }

            case "actualizar": {
                Integer id = getAsInt(body, "idCategoria");
                if (id == null) { escribirJsonError(response, 400, "Debe enviar 'idCategoria'."); return; }

                Categoria_dtos dto = new Categoria_dtos();
                dto.setIdCategoria(id);
                dto.setNombre(getAsString(body, "nombre"));
                dto.setDescripcion(getAsString(body, "descripcion"));
                dto.setEstado(getAsString(body, "estado")); 
                r = categoriaService.actualizar(dto);
                break;
            }

            case "cambiar_estado": {
                Integer id = getAsInt(body, "idCategoria");
                String nuevoEstado = getAsString(body, "nuevoEstado");
                if (id == null || nuevoEstado == null) { escribirJsonError(response, 400, "Debe enviar 'idCategoria' y 'nuevoEstado'."); return; }
                r = categoriaService.cambiarEstado(id, nuevoEstado);
                break;
            }

            default:
                escribirJsonError(response, 400, "Acción inválida. Use: crear | actualizar | cambiar_estado");
                return;
        }

        escribirResultado(response, r);
    }

    private void escribirResultado(HttpServletResponse response, ServiceResult<?> r) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("ok", r.ok);
        json.addProperty("mensaje", r.mensaje);
        if (r.data != null) json.add("data", gson.toJsonTree(r.data));
        escribirJson(response, json, r.httpStatus);
    }

    private JsonObject leerJsonBody(HttpServletRequest request) {
        try (BufferedReader br = request.getReader()) {
            return JsonParser.parseReader(br).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private String getAsString(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            String v = obj.get(key).getAsString();
            return (v == null) ? null : v;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getAsInt(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private void escribirJson(HttpServletResponse response, JsonObject json, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(json));
        }
    }

    private void escribirJsonError(HttpServletResponse response, int status, String mensaje) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("mensaje", mensaje);
        escribirJson(response, json, status);
    }
}