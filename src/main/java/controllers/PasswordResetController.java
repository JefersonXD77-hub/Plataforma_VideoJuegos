package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Perfil_model;
import services.PasswordResetService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/password")
public class PasswordResetController extends HttpServlet {

    private final Gson gson = new Gson();
    private final PasswordResetService resetService = new PasswordResetService();
    private final Perfil_model perfilModel = new Perfil_model();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception e) {
            escribirJsonError(response, 400, "Body JSON inválido.");
            return;
        }
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON vacío.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null) {
            escribirJsonError(response, 400, "Debe enviar 'accion' (solicitar/restablecer).");
            return;
        }

        switch (accion.trim().toLowerCase()) {
            case "solicitar":
                solicitar(body, response);
                break;
            case "restablecer":
                restablecer(body, response);
                break;
            default:
                escribirJsonError(response, 400, "Acción no soportada.");
        }
    }

    private void solicitar(JsonObject body, HttpServletResponse response) throws IOException {
        String correo = getAsString(body, "correo");
       
        PasswordResetService.IssueResult r = resetService.emitirCodigo(correo, 10);

        JsonObject out = new JsonObject();
        out.addProperty("ok", r.ok);
        out.addProperty("mensaje", r.mensaje);

        
        boolean existe = (correo != null && perfilModel.existeCorreo(correo));
        if (existe && r.codigoDemo != null) {
            out.addProperty("codigoDemo", r.codigoDemo);
        }

        escribirJson(response, out, 200);
    }

    private void restablecer(JsonObject body, HttpServletResponse response) throws IOException {
        String correo = getAsString(body, "correo");
        String codigo = getAsString(body, "codigo");
        String nuevaPassword = getAsString(body, "nuevaPassword");

        if (correo == null || codigo == null || nuevaPassword == null) {
            escribirJsonError(response, 400, "Debe enviar correo, codigo y nuevaPassword.");
            return;
        }

        PasswordResetService.VerifyResult vr = resetService.validarCodigo(correo, codigo);
        if (!vr.ok) {
            escribirJsonError(response, 400, vr.mensaje);
            return;
        }

        boolean ok = perfilModel.actualizarPasswordPorCorreo(correo, nuevaPassword);
        if (!ok) {
            escribirJsonError(response, 500, "No se pudo actualizar la contraseña.");
            return;
        }

        resetService.invalidar(correo);

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Contraseña actualizada.");
        escribirJson(response, out, 200);
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
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