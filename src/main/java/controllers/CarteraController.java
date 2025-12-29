package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.CarteraService;
import services.CarteraService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/cartera")
public class CarteraController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final CarteraService service = new CarteraService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");
        ServiceResult<?> r = service.obtenerMiCartera(usuario.getIdUsuario());
        escribirJsonEstandar(response, r);
    }

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
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception e) {
            body = null;
        }
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON vacío o inválido.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null) {
            escribirJsonError(response, 400, "Debe enviar el campo 'accion' (recargar).");
            return;
        }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {
            case "recargar":
                r = service.recargar(
                        usuario.getIdUsuario(),
                        getAsBigDecimalFlexible(body, "monto"),
                        getAsString(body, "descripcion")
                );
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

        if (r.data != null) {
            out.add("data", gson.toJsonTree(r.data));
        }

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

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
    }

    private BigDecimal getAsBigDecimalFlexible(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) return null;
        try {
            return obj.get(field).getAsBigDecimal();
        } catch (Exception e) {
            try {
                String s = obj.get(field).getAsString();
                if (s == null) return null;
                s = s.trim();
                if (s.isEmpty()) return null;
                return new BigDecimal(s);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
