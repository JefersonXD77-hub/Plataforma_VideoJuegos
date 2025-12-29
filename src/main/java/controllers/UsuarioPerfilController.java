package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import models.Perfil_model;
import models.Preferencias_usuario_model;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/usuario/perfil")
public class UsuarioPerfilController extends HttpServlet {

    private final Gson gson = new Gson();
    private final Perfil_model perfilModel = new Perfil_model();
    private final Preferencias_usuario_model prefModel = new Preferencias_usuario_model();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");

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
        if (accion == null) {
            escribirJsonError(response, 400, "Debe enviar accion (actualizarPerfil/actualizarPreferencias).");
            return;
        }

        switch (accion.trim().toLowerCase()) {
            case "actualizarperfil":
                actualizarPerfil(usuario, body, response);
                break;
            case "actualizarpreferencias":
                actualizarPreferencias(usuario, body, response);
                break;
            default:
                escribirJsonError(response, 400, "Acción no soportada.");
        }
    }

    private void actualizarPerfil(Usuario_dtos usuario, JsonObject body, HttpServletResponse response) throws IOException {
        
        String nickname = getAsString(body, "nickname");
        String nombreCompleto = getAsString(body, "nombreCompleto");
        String telefono = getAsString(body, "telefono");
        Integer idPais = getAsIntNullable(body, "idPais");

        boolean ok = perfilModel.actualizarPerfil(usuario.getIdUsuario(), nickname, nombreCompleto, telefono, idPais);
        if (!ok) {
            escribirJsonError(response, 400, "No hubo cambios o no se pudo actualizar.");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Perfil actualizado.");
        escribirJson(response, out, 200);
    }

    private void actualizarPreferencias(Usuario_dtos usuario, JsonObject body, HttpServletResponse response) throws IOException {
        Boolean bibliotecaPublica = getAsBooleanNullable(body, "bibliotecaPublica");
        String avatarUrl = getAsString(body, "avatarUrl");

        boolean ok = prefModel.actualizar(usuario.getIdUsuario(), bibliotecaPublica, avatarUrl);
        if (!ok) {
            escribirJsonError(response, 400, "No hubo cambios o no se pudo actualizar.");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Preferencias actualizadas.");
        escribirJson(response, out, 200);
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
    }

    private Integer getAsIntNullable(JsonObject obj, String field) {
        try {
            return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsInt() : null;
        } catch (Exception e) { return null; }
    }

    private Boolean getAsBooleanNullable(JsonObject obj, String field) {
        try {
            return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsBoolean() : null;
        } catch (Exception e) { return null; }
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
