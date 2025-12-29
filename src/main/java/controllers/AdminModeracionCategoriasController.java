package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import models.Videojuego_Categoria_model;

import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/admin/moderacion-categorias")
public class AdminModeracionCategoriasController extends HttpServlet {

    private static final int ROL_ADMIN_SISTEMA = 1;

    private final Gson gson = new Gson();
    private final Videojuego_Categoria_model model = new Videojuego_Categoria_model();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario_dtos admin = requireAdmin(request, response);
        if (admin == null) return;

        String accion = request.getParameter("accion");
        if (accion == null || accion.isBlank()) {
            escribirJsonError(response, 400, "Debe enviar accion=pendientes.");
            return;
        }

        if (!accion.trim().equalsIgnoreCase("pendientes")) {
            escribirJsonError(response, 400, "Acción no soportada.");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "OK");
        out.add("data", gson.toJsonTree(model.listarPendientesModeracion()));
        escribirJson(response, out, 200);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario_dtos admin = requireAdmin(request, response);
        if (admin == null) return;

        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception e) {
            body = null;
        }
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON inválido.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null || !accion.equalsIgnoreCase("resolver")) {
            escribirJsonError(response, 400, "Debe enviar accion=resolver.");
            return;
        }

        int idVideojuego = getAsInt(body, "idVideojuego");
        int idCategoria = getAsInt(body, "idCategoria");
        String estado = getAsString(body, "estado");

        if (idVideojuego <= 0 || idCategoria <= 0 || estado == null) {
            escribirJsonError(response, 400, "Debe enviar idVideojuego, idCategoria y estado (APROBADA|RECHAZADA)." );
            return;
        }

        boolean ok = model.revisarAsignacionCategoria(idVideojuego, idCategoria, estado, admin.getIdUsuario());
        if (!ok) {
            escribirJsonError(response, 404, "No se pudo actualizar (¿existe la asignación?).");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Asignación actualizada a " + estado.trim().toUpperCase());
        escribirJson(response, out, 200);
    }

    private Usuario_dtos requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return null;
        }
        Usuario_dtos u = (Usuario_dtos) session.getAttribute("usuarioActual");
        if (u.getIdRol() != ROL_ADMIN_SISTEMA) {
            escribirJsonError(response, 403, "Solo administrador.");
            return null;
        }
        return u;
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsString().trim() : null;
    }

    private int getAsInt(JsonObject obj, String field) {
        try {
            return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
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

