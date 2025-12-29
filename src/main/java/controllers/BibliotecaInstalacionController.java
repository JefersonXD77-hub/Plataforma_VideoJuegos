package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.InstalacionService;
import services.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/biblioteca/instalacion")
public class BibliotecaInstalacionController extends HttpServlet {

    private final Gson gson = new Gson();
    private final InstalacionService service = new InstalacionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");

        JsonObject body;
        try { body = gson.fromJson(request.getReader(), JsonObject.class); }
        catch (Exception e) { body = null; }

        if (body == null) {
            escribirJsonError(response, 400, "Body JSON vacío o inválido.");
            return;
        }

        String accion = (body.has("accion") && !body.get("accion").isJsonNull())
                ? body.get("accion").getAsString().trim().toLowerCase()
                : null;

        Integer idVideojuego = (body.has("idVideojuego") && !body.get("idVideojuego").isJsonNull())
                ? body.get("idVideojuego").getAsInt()
                : null;

        if (accion == null || idVideojuego == null) {
            escribirJsonError(response, 400, "Debe enviar 'accion' (instalar/desinstalar) e 'idVideojuego'.");
            return;
        }

        ServiceResult<?> r;
        switch (accion) {
            case "instalar":
                r = service.instalar(usuario.getIdUsuario(), idVideojuego);
                break;
            case "desinstalar":
                r = service.desinstalar(usuario.getIdUsuario(), idVideojuego);
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
        if (r.data != null) out.add("data", gson.toJsonTree(r.data));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(r.httpStatus);

        try (PrintWriter pw = response.getWriter()) {
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

        try (PrintWriter pw = response.getWriter()) {
            pw.print(gson.toJson(out));
        }
    }
}
