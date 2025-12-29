package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtos.Comentario_videojuego_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.ComentarioService;
import services.ComentarioService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/comentarios")
public class ComentarioController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private final ComentarioService service = new ComentarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        
        String idStr = request.getParameter("idVideojuego");
        if (idStr == null) { escribirJsonError(response, 400, "Debe enviar 'idVideojuego'."); return; }

        int idVideojuego;
        try { idVideojuego = Integer.parseInt(idStr.trim()); }
        catch (Exception e) { escribirJsonError(response, 400, "idVideojuego inválido."); return; }

        String modo = request.getParameter("modo");
        ServiceResult<?> r = (modo != null && modo.trim().equalsIgnoreCase("admin"))
            ? service.listarAdmin(idVideojuego)
            : service.listarPublico(idVideojuego);

        escribirResultado(response, r);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonObject body = leerJsonBody(request);
        if (body == null) { escribirJsonError(response, 400, "Body JSON inválido."); return; }

        String accion = getAsString(body, "accion");
        if (accion == null) { escribirJsonError(response, 400, "Debe enviar 'accion'."); return; }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "crear": {
                Comentario_videojuego_dtos dto = new Comentario_videojuego_dtos();
                Integer idVideojuego = getAsInt(body, "idVideojuego");
                Integer idUsuario = getAsInt(body, "idUsuario");
                Integer idPadre = getAsInt(body, "idComentarioPadre"); // opcional
                String texto = getAsString(body, "texto");

                if (idVideojuego == null || idUsuario == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idVideojuego' e 'idUsuario'.");
                    return;
                }

                dto.setIdVideojuego(idVideojuego);
                dto.setIdUsuario(idUsuario);
                dto.setIdComentarioPadre(idPadre);
                dto.setTexto(texto);

                r = service.crear(dto);
                break;
            }

            case "set_visible_empresa": {
                Integer idComentario = getAsInt(body, "idComentario");
                Integer idEmpresa = getAsInt(body, "idEmpresa");
                Boolean visible = getAsBoolean(body, "visible");
                if (idComentario == null || idEmpresa == null || visible == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idComentario', 'idEmpresa' y 'visible'.");
                    return;
                }
                r = service.setVisibleEmpresa(idComentario, idEmpresa, visible);
                break;
            }

            // Moderación admin
            case "set_visible_admin": {
                Integer idComentario = getAsInt(body, "idComentario");
                Boolean visible = getAsBoolean(body, "visible");
                if (idComentario == null || visible == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idComentario' y 'visible'.");
                    return;
                }
                r = service.setVisibleAdmin(idComentario, visible);
                break;
            }

            default:
                escribirJsonError(response, 400, "Acción inválida. Use: crear | set_visible_empresa | set_visible_admin");
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
            return obj.get(key).getAsString();
        } catch (Exception e) { return null; }
    }

    private Integer getAsInt(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return obj.get(key).getAsInt();
        } catch (Exception e) { return null; }
    }

    private Boolean getAsBoolean(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return obj.get(key).getAsBoolean();
        } catch (Exception e) { return null; }
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
