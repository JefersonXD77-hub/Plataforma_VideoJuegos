package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.RecomendacionService;
import services.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/recomendaciones")
public class RecomendacionController extends HttpServlet {

    private final Gson gson = new Gson();
    private final RecomendacionService service = new RecomendacionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer limit = null;
        Integer m = null;

        try {
            if (request.getParameter("limit") != null) limit = Integer.parseInt(request.getParameter("limit").trim());
            if (request.getParameter("m") != null) m = Integer.parseInt(request.getParameter("m").trim());
        } catch (Exception e) {
            escribirJsonError(response, 400, "Parámetros inválidos. Use enteros.");
            return;
        }

        ServiceResult<?> r = service.mejorBalance(limit, m);
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
