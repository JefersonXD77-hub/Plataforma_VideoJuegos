package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import dtos.Usuario_dtos;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import services.ComisionService;
import services.ComisionService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/admin/comision")
public class ComisionController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int ROL_ADMIN_SISTEMA = 1;

    private final Gson gson = new Gson();
    private final ComisionService comisionService = new ComisionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


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

       
        String tipo = request.getParameter("tipo");
        if (tipo == null || tipo.trim().isEmpty()) {
            escribirJsonError(response, 400, "Debe enviar 'tipo' (global|empresa).");
            return;
        }

        ServiceResult<?> r;

        switch (tipo.trim().toLowerCase()) {
            case "global":
                r = comisionService.getGlobal();
                break;

            case "empresa": {
                String idStr = request.getParameter("idEmpresa");
                if (idStr == null) { escribirJsonError(response, 400, "Debe enviar 'idEmpresa'."); return; }
                int id;
                try { id = Integer.parseInt(idStr.trim()); } catch (Exception e) { escribirJsonError(response, 400, "idEmpresa inválido."); return; }
                r = comisionService.getComisionEmpresa(id);
                break;
            }

            default:
                escribirJsonError(response, 400, "Tipo inválido. Use global|empresa.");
                return;
        }

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
        if (accion == null) { escribirJsonError(response, 400, "Debe enviar 'accion'."); return; }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "set_global": {
                BigDecimal valor = getAsBigDecimal(body, "valor");
                r = comisionService.setGlobal(valor);
                break;
            }

            case "set_empresa": {
                Integer idEmpresa = getAsInt(body, "idEmpresa");
                BigDecimal valor = getAsBigDecimal(body, "valor");
                if (idEmpresa == null) { escribirJsonError(response, 400, "Debe enviar 'idEmpresa'."); return; }
                r = comisionService.setComisionEmpresa(idEmpresa, valor);
                break;
            }

            default:
                escribirJsonError(response, 400, "Acción inválida. Use set_global | set_empresa");
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

    private BigDecimal getAsBigDecimal(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
            
            return new BigDecimal(obj.get(key).getAsString());
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