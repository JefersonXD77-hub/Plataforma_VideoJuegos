package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import services.CompraService;
import services.CompraService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;
import services.FechaCompraService;

@WebServlet("/compras")
public class CompraController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final CompraService service = new CompraService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos usuario = (Usuario_dtos) session.getAttribute("usuarioActual");
        ServiceResult<?> r = service.misCompras(usuario.getIdUsuario());
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
            escribirJsonError(response, 400, "Debe enviar el campo 'accion' (comprar).");
            return;
        }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "comprar":
                List<Integer> ids = getAsIntList(body, "idsVideojuego");
                if (ids == null || ids.isEmpty()) {
                    escribirJsonError(response, 400, "Debe enviar 'idsVideojuego' como arreglo con al menos un id.");
                    return;
                }
                Timestamp fechaCompra = null;
                try {
                    String fc = getAsString(body, "fechaCompra");
                    fechaCompra = FechaCompraService.parseNullable(fc);
                } catch (Exception ex) {
                    escribirJsonError(response, 400, "fechaCompra inválida. Use yyyy-MM-dd o yyyy-MM-dd'T'HH:mm:ss");
                    return;
                }
                r = service.comprar(usuario.getIdUsuario(), ids, fechaCompra);
                escribirJsonEstandar(response, r);
                return;

            default:
                escribirJsonError(response, 400, "Acción no soportada: " + accion);
        }
    }

    private List<Integer> getAsIntList(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull() || !obj.get(field).isJsonArray()) {
            return null;
        }
        List<Integer> out = new ArrayList<>();
        JsonArray arr = obj.getAsJsonArray(field);
        for (JsonElement el : arr) {
            try {
                int v = el.getAsInt();
                if (v > 0) out.add(v);
            } catch (Exception ignored) {}
        }
        return out;
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
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
