package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtos.Banner_dtos;
import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import services.BannerService;
import services.BannerService.ServiceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;

@WebServlet("/admin/banner")
public class BannerAdminController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int ROL_ADMIN_SISTEMA = 1;

    private final Gson gson = new Gson();
    private final BannerService bannerService = new BannerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


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

        ServiceResult<?> r = bannerService.listarTodos();
        escribirResultado(response, r);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


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

        JsonObject body = leerJsonBody(request);
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON inválido.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null) {
            escribirJsonError(response, 400, "Debe enviar el campo 'accion'.");
            return;
        }

        ServiceResult<?> r;

        switch (accion.trim().toLowerCase()) {

            case "crear": {
                Banner_dtos b;
                try {
                    b = parseBanner(body, false);
                } catch (IllegalArgumentException ex) {
                    escribirJsonError(response, 400, ex.getMessage());
                    return;
                }
                r = bannerService.crear(b);
                break;
            }

            case "actualizar": {
                Banner_dtos b;
                try {
                    b = parseBanner(body, true);
                } catch (IllegalArgumentException ex) {
                    escribirJsonError(response, 400, ex.getMessage());
                    return;
                }
                if (b.getIdBanner() <= 0) {
                    escribirJsonError(response, 400, "Debe enviar 'idBanner'.");
                    return;
                }
                r = bannerService.actualizar(b);
                break;
            }

            case "set_activo": {
                Integer idBanner = getAsInt(body, "idBanner");
                Boolean activo = getAsBoolean(body, "activo");
                if (idBanner == null || activo == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idBanner' y 'activo'.");
                    return;
                }
                r = bannerService.setActivo(idBanner, activo);
                break;
            }

            case "eliminar": {
                Integer idBanner = getAsInt(body, "idBanner");
                if (idBanner == null) {
                    escribirJsonError(response, 400, "Debe enviar 'idBanner'.");
                    return;
                }
                r = bannerService.eliminar(idBanner);
                break;
            }

            default:
                escribirJsonError(response, 400, "Acción inválida. Use: crear | actualizar | set_activo | eliminar");
                return;
        }

        escribirResultado(response, r);
    }

    private Banner_dtos parseBanner(JsonObject body, boolean incluyeId) {
        Banner_dtos b = new Banner_dtos();
        if (incluyeId) {
            Integer id = getAsInt(body, "idBanner");
            if (id != null) {
                b.setIdBanner(id);
            }
        }

        b.setTitulo(getAsString(body, "titulo"));
        b.setDescripcion(getAsString(body, "descripcion"));
        b.setUrlImagen(getAsString(body, "urlImagen"));
        b.setIdVideojuego(getAsInt(body, "idVideojuego"));

        String fi = getAsString(body, "fechaInicio");
        String ff = getAsString(body, "fechaFin");

        if (fi != null && !fi.trim().isEmpty()) {
            try {
                b.setFechaInicio(Date.valueOf(fi.trim()));
            } catch (Exception ex) {
                throw new IllegalArgumentException("fechaInicio inválida. Use formato yyyy-mm-dd.");
            }
        }

        if (ff != null && !ff.trim().isEmpty()) {
            try {
                b.setFechaFin(Date.valueOf(ff.trim()));
            } catch (Exception ex) {
                throw new IllegalArgumentException("fechaFin inválida. Use formato yyyy-mm-dd.");
            }
        }

        Boolean activo = getAsBoolean(body, "activo");
        b.setActivo(activo != null ? activo : false);

        return b;
    }

    private void escribirResultado(HttpServletResponse response, ServiceResult<?> r) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("ok", r.ok);
        json.addProperty("mensaje", r.mensaje);
        if (r.data != null) {
            json.add("data", gson.toJsonTree(r.data));
        }
        escribirJson(response, json, r.httpStatus);
    }

    private JsonObject leerJsonBody(HttpServletRequest request) {
        try ( BufferedReader br = request.getReader()) {
            return JsonParser.parseReader(br).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private String getAsString(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                return null;
            }
            return obj.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getAsInt(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                return null;
            }
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getAsBoolean(JsonObject obj, String key) {
        try {
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                return null;
            }
            return obj.get(key).getAsBoolean();
        } catch (Exception e) {
            return null;
        }
    }

    private void escribirJson(HttpServletResponse response, JsonObject json, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(json));
        }
    }

    private void escribirJsonError(HttpServletResponse response, int status, String mensaje) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("mensaje", mensaje);
        escribirJson(response, json, status);
    }
}
