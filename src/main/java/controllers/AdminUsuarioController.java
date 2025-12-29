package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import models.Usuario_model;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/admin/usuarios")
public class AdminUsuarioController extends HttpServlet {

    private final Gson gson = new Gson();
    private final Usuario_model model = new Usuario_model();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos actual = (Usuario_dtos) session.getAttribute("usuarioActual");
        if (actual.getIdRol() != 1) {
            escribirJsonError(response, 403, "No autorizado.");
            return;
        }

        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception e) {
            escribirJsonError(response, 400, "Body JSON inválido.");
            return;
        }
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON vacío.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null || !accion.equalsIgnoreCase("cambiarEstado")) {
            escribirJsonError(response, 400, "Debe enviar accion=cambiarEstado.");
            return;
        }

        int idUsuario = getAsInt(body, "idUsuario");
        String estado = getAsString(body, "estado");

        if (idUsuario <= 0 || estado == null) {
            escribirJsonError(response, 400, "Debe enviar idUsuario y estado.");
            return;
        }

        String est = estado.trim().toUpperCase();
        if (!est.equals("ACTIVO") && !est.equals("SUSPENDIDO") && !est.equals("ELIMINADO") && !est.equals("BLOQUEADO")) {
            if (est.equals("BLOQUEADO")) est = "SUSPENDIDO";
        }

        boolean ok = model.cambiarEstado(idUsuario, est);
        if (!ok) {
            escribirJsonError(response, 404, "No se pudo cambiar estado (usuario no encontrado?).");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Estado actualizado a " + est + ".");
        escribirJson(response, out, 200);
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
    }

    private int getAsInt(JsonObject obj, String field) {
        try {
            return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsInt() : 0;
        } catch (Exception e) { return 0; }
    }

    private void escribirJsonError(HttpServletResponse response, int status, String mensaje) throws IOException {
        JsonObject out = new JsonObject();
        out.addProperty("ok", false);
        out.addProperty("mensaje", mensaje);
        escribirJson(response, out, status);
    }

    private void escribirJson(HttpServletResponse response, JsonObject out, int statusCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        try (PrintWriter pw = response.getWriter()) {
            pw.print(gson.toJson(out));
        }
    }
}
