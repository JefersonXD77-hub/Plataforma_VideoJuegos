package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.VideojuegoService;
import services.VideojuegoService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;


@WebServlet("/tienda")
public class TiendaController extends HttpServlet {

    private final Gson gson = new Gson();
    private final VideojuegoService service = new VideojuegoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String titulo = request.getParameter("titulo");
        Integer idEmpresa = parseIntNullable(request.getParameter("idEmpresa"));
        Integer idCategoria = parseIntNullable(request.getParameter("idCategoria"));
        BigDecimal precioMin = parseDecimalNullable(request.getParameter("precioMin"));
        BigDecimal precioMax = parseDecimalNullable(request.getParameter("precioMax"));

        ServiceResult<?> r = service.buscarTienda(titulo, idEmpresa, idCategoria, precioMin, precioMax);
        JsonObject out = new JsonObject();
        out.addProperty("ok", r.ok);
        out.addProperty("mensaje", r.mensaje);
        if (r.data != null) out.add("data", gson.toJsonTree(r.data));
        escribirJson(response, out, r.httpStatus);
    }

    private Integer parseIntNullable(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            int v = Integer.parseInt(s.trim());
            return v > 0 ? v : null;
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseDecimalNullable(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
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
