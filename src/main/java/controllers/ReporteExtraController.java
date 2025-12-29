package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import models.ReporteExtra_model;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;

@WebServlet("/admin/reportes-extra")
public class ReporteExtraController extends HttpServlet {

    private final Gson gson = new Gson();
    private final ReporteExtra_model model = new ReporteExtra_model();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            escribirJsonError(response, 400, "Debe enviar accion.");
            return;
        }

        Date desde = parseDateNullable(request.getParameter("desde"));
        Date hasta = parseDateNullable(request.getParameter("hasta"));

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "OK");

        switch (accion.trim().toLowerCase()) {
            case "ganancias":
                out.add("data", gson.toJsonTree(model.ganancias(desde, hasta)));
                escribirJson(response, out, 200);
                break;

            case "ranking_compras":
                out.add("data", gson.toJsonTree(model.rankingUsuariosCompras(desde, hasta, 50)));
                escribirJson(response, out, 200);
                break;

            case "ranking_resenas":
                out.add("data", gson.toJsonTree(model.rankingUsuariosResenas(desde, hasta, 50)));
                escribirJson(response, out, 200);
                break;

            case "top_ventas":
                Integer cat = parseIntNullable(request.getParameter("categoriaId"));
                Integer clas = parseIntNullable(request.getParameter("clasificacionId"));
                out.add("data", gson.toJsonTree(model.topVentas(cat, clas, desde, hasta, 50)));
                escribirJson(response, out, 200);
                break;

            case "top_calidad":
                Integer cat2 = parseIntNullable(request.getParameter("categoriaId"));
                Integer clas2 = parseIntNullable(request.getParameter("clasificacionId"));
                out.add("data", gson.toJsonTree(model.topCalidad(cat2, clas2, desde, hasta, 50)));
                escribirJson(response, out, 200);
                break;

            default:
                escribirJsonError(response, 400, "Acción no soportada.");
        }
    }

    private Date parseDateNullable(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Date.valueOf(s.trim()); 
        } catch (Exception e) {
            return null;
        }
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
