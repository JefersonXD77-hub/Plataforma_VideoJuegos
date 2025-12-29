package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dtos.Usuario_dtos;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.MovimientoCarteraService;
import services.MovimientoCarteraService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

@WebServlet("/movimientos-cartera")
public class MovimientoCarteraController extends HttpServlet {

    private final Gson gson = new Gson();
    private final MovimientoCarteraService service = new MovimientoCarteraService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");

        Timestamp desde = parseTs(request.getParameter("desde"));
        Timestamp hasta = parseTs(request.getParameter("hasta"));

        ServiceResult<?> r = service.listar(usuario.getIdUsuario(), desde, hasta);
        escribirJsonEstandar(response, r);
    }

    private Timestamp parseTs(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Timestamp.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
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
