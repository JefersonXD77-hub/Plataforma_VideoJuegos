package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.PrestamoGrupoService;
import services.PrestamoGrupoService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/familia/prestamos")
public class PrestamoGrupoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final PrestamoGrupoService service = new PrestamoGrupoService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonObject body = leerJsonBody(request);
        if (body == null) { escribirJsonError(response, 400, "Body JSON inválido."); return; }

        String accion = getAsString(body, "accion");
        if (accion == null) { escribirJsonError(response, 400, "Debe enviar el campo 'accion'."); return; }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "miembros_disponibles": {
                Integer idGrupo = getAsInt(body, "idGrupo");
                Integer idDueno = getAsInt(body, "idDueno");
                if (idGrupo == null || idDueno == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idGrupo' e 'idDueno'.");
                    return;
                }
                r = service.miembrosDisponibles(idGrupo, idDueno);
                break;
            }

            case "prestar": {
                Integer idGrupo = getAsInt(body, "idGrupo");
                Integer idDueno = getAsInt(body, "idDueno");
                Integer idReceptor = getAsInt(body, "idReceptor");
                Integer idVideojuego = getAsInt(body, "idVideojuego");
                if (idGrupo == null || idDueno == null || idReceptor == null || idVideojuego == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idGrupo', 'idDueno', 'idReceptor', 'idVideojuego'.");
                    return;
                }
                r = service.prestar(idGrupo, idDueno, idReceptor, idVideojuego);
                break;
            }

            case "devolver": {
                Integer idPrestamo = getAsInt(body, "idPrestamo");
                if (idPrestamo == null) { escribirJsonError(response, 400, "Debe enviar 'idPrestamo'."); return; }
                r = service.devolver(idPrestamo);
                break;
            }

            case "cancelar": {
                Integer idPrestamo = getAsInt(body, "idPrestamo");
                if (idPrestamo == null) { escribirJsonError(response, 400, "Debe enviar 'idPrestamo'."); return; }
                r = service.cancelar(idPrestamo);
                break;
            }

            default:
                escribirJsonError(response, 400, "Acción inválida. Use: miembros_disponibles | prestar | devolver | cancelar");
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

