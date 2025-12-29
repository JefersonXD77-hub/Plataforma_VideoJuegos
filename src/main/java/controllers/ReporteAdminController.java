package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.ReporteService;
import services.ReporteService.ServiceResult;
import services.SimplePdfService;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/admin/reportes", "/admin/reportes/pdf"})
public class ReporteAdminController extends HttpServlet {

    private static final int ROL_ADMIN_SISTEMA = 1;

    private final Gson gson = new Gson();
    private final ReporteService service = new ReporteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        String path = request.getServletPath();
        boolean pdf = path != null && path.endsWith("/pdf");

        String tipo = request.getParameter("tipo");
        if (tipo == null || tipo.trim().isEmpty()) {
            if (pdf) {
                response.setStatus(400);
                return;
            }
            escribirJsonError(response, 400, "Debe enviar 'tipo' (resumen|ventas_empresa|top_juegos).");
            return;
        }

        Date ini = parseDate(request.getParameter("ini"));
        Date fin = parseDate(request.getParameter("fin"));
        Integer limit = null;
        try { if (request.getParameter("limit") != null) limit = Integer.parseInt(request.getParameter("limit")); } catch (Exception ignored) {}

        ServiceResult<?> r;
        switch (tipo.trim().toLowerCase()) {
            case "resumen":
                r = service.resumenPlataforma(ini, fin);
                break;
            case "ventas_empresa":
                r = service.ventasPorEmpresa(ini, fin);
                break;
            case "top_juegos":
                r = service.topVideojuegos(limit, ini, fin);
                break;
            default:
                escribirJsonError(response, 400, "Tipo inválido. Use resumen|ventas_empresa|top_juegos.");
                return;
        }

        if (!pdf) {
            escribirJsonEstandar(response, r);
            return;
        }

       
        List<String> lines = new ArrayList<>();
        lines.add("Reporte: " + tipo);
        if (ini != null) lines.add("Desde: " + ini);
        if (fin != null) lines.add("Hasta: " + fin);
        lines.add("");

        if (r.data != null) {
            lines.add(gson.toJson(r.data));
        } else {
            lines.add("Sin datos.");
        }

        byte[] pdfBytes = SimplePdfService.onePageText(lines);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte.pdf");
        response.setStatus(r.httpStatus);
        response.getOutputStream().write(pdfBytes);
    }

    private Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Date.valueOf(s.trim()); } catch (Exception e) { return null; }
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

