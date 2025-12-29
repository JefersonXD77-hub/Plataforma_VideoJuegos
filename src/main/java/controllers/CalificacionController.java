package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.CalificacionService;
import services.CalificacionService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/calificaciones")
public class CalificacionController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();
    private final CalificacionService service = new CalificacionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        
        String tipo = request.getParameter("tipo");
        if (tipo == null) { escribirJsonError(response, 400, "Debe enviar 'tipo' (mi|promedio)."); return; }

        ServiceResult<?> r;

        switch (tipo.trim().toLowerCase()) {

            case "mi": {
                String u = request.getParameter("idUsuario");
                String v = request.getParameter("idVideojuego");
                if (u == null || v == null) { escribirJsonError(response, 400, "Debe enviar 'idUsuario' e 'idVideojuego'."); return; }
                int idUsuario, idVideojuego;
                try { idUsuario = Integer.parseInt(u.trim()); idVideojuego = Integer.parseInt(v.trim()); }
                catch (Exception e) { escribirJsonError(response, 400, "Parámetros inválidos."); return; }
                r = service.miCalificacion(idUsuario, idVideojuego);
                break;
            }

            case "promedio": {
                String v = request.getParameter("idVideojuego");
                if (v == null) { escribirJsonError(response, 400, "Debe enviar 'idVideojuego'."); return; }
                int idVideojuego;
                try { idVideojuego = Integer.parseInt(v.trim()); }
                catch (Exception e) { escribirJsonError(response, 400, "idVideojuego inválido."); return; }
                r = service.promedio(idVideojuego);
                break;
            }

            default:
                escribirJsonError(response, 400, "Tipo inválido. Use mi|promedio.");
                return;
        }

        escribirResultado(response, r);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonObject body = leerJsonBody(request);
        if (body == null) { escribirJsonError(response, 400, "Body JSON inválido."); return; }

        String accion = body.has("accion") ? body.get("accion").getAsString() : null;
        if (accion == null) { escribirJsonError(response, 400, "Debe enviar 'accion'."); return; }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {
            case "calificar": {
                Integer idUsuario = getAsInt(body, "idUsuario");
                Integer idVideojuego = getAsInt(body, "idVideojuego");
                Integer puntuacion = getAsInt(body, "puntuacion");
                if (idUsuario == null || idVideojuego == null || puntuacion == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idUsuario', 'idVideojuego' y 'puntuacion'.");
                    return;
                }
                r = service.calificar(idUsuario, idVideojuego, puntuacion);
                break;
            }
            default:
                escribirJsonError(response, 400, "Acción inválida. Use: calificar");
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

    private Integer getAsInt(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            return obj.get(key).getAsInt();
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
