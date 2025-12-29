package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.GrupoFamiliarService;
import services.GrupoFamiliarService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/familia/grupos")
public class GrupoFamiliarController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final GrupoFamiliarService service = new GrupoFamiliarService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String tipo = request.getParameter("tipo"); 
        String idStr = request.getParameter("idUsuario");
        if (idStr == null || idStr.trim().isEmpty()) {
            escribirJsonError(response, 400, "Debe enviar 'idUsuario'.");
            return;
        }

        int idUsuario;
        try { idUsuario = Integer.parseInt(idStr.trim()); }
        catch (Exception e) { escribirJsonError(response, 400, "idUsuario inválido."); return; }

        ServiceResult<?> r;
        if (tipo != null && tipo.trim().equalsIgnoreCase("invitaciones")) {
            r = service.listarInvitacionesPendientes(idUsuario);
        } else {
            r = service.listarGruposDeUsuario(idUsuario);
        }

        escribirResultado(response, r);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonObject body = leerJsonBody(request);
        if (body == null) { escribirJsonError(response, 400, "Body JSON inválido."); return; }

        String accion = getAsString(body, "accion");
        if (accion == null) { escribirJsonError(response, 400, "Debe enviar el campo 'accion'."); return; }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "crear_grupo": {
                Integer idCreador = getAsInt(body, "idCreador");
                String nombre = getAsString(body, "nombre");
                if (idCreador == null) { escribirJsonError(response, 400, "Debe enviar 'idCreador'."); return; }
                r = service.crearGrupo(idCreador, nombre);
                break;
            }

            case "invitar": {
                Integer idGrupo = getAsInt(body, "idGrupo");
                Integer idEmisor = getAsInt(body, "idEmisor");
                Integer idReceptor = getAsInt(body, "idReceptor");
                if (idGrupo == null || idEmisor == null || idReceptor == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idGrupo', 'idEmisor' e 'idReceptor'.");
                    return;
                }
                r = service.invitar(idGrupo, idEmisor, idReceptor);
                break;
            }

            case "aceptar": {
                Integer idGrupo = getAsInt(body, "idGrupo");
                Integer idUsuario = getAsInt(body, "idUsuario");
                if (idGrupo == null || idUsuario == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idGrupo' e 'idUsuario'.");
                    return;
                }
                r = service.aceptarInvitacion(idGrupo, idUsuario);
                break;
            }

            case "rechazar": {
                Integer idGrupo = getAsInt(body, "idGrupo");
                Integer idUsuario = getAsInt(body, "idUsuario");
                if (idGrupo == null || idUsuario == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idGrupo' e 'idUsuario'.");
                    return;
                }
                r = service.rechazarInvitacion(idGrupo, idUsuario);
                break;
            }

            default:
                escribirJsonError(response, 400, "Acción inválida. Use: crear_grupo | invitar | aceptar | rechazar");
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
