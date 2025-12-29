package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.BibliotecaService;
import services.BibliotecaService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/biblioteca")
public class BibliotecaController extends HttpServlet {

    private final Gson gson = new Gson();
    private final BibliotecaService service = new BibliotecaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");
        ServiceResult<?> r = service.miBiblioteca(usuario.getIdUsuario());
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

