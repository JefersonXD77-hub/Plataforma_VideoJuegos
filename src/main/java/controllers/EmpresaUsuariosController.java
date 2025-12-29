package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import models.EmpresaUsuario_model;
import models.Usuario_model;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;


@WebServlet("/empresa/usuarios")
public class EmpresaUsuariosController extends HttpServlet {

    private static final int ROL_USUARIO_EMPRESA = 2;

    private final Gson gson = new Gson();
    private final Usuario_model usuarioModel = new Usuario_model();
    private final EmpresaUsuario_model empUsuModel = new EmpresaUsuario_model();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            escribirJsonError(response, 401, "No autenticado.");
            return;
        }

        Usuario_dtos actual = (Usuario_dtos) session.getAttribute("usuarioActual");
        if (actual.getIdRol() != ROL_USUARIO_EMPRESA) {
            escribirJsonError(response, 403, "Solo usuarios de empresa.");
            return;
        }

        Integer idEmpresa = empUsuModel.obtenerEmpresaDeUsuario(actual.getIdUsuario());
        if (idEmpresa == null) {
            escribirJsonError(response, 403, "Usuario empresa sin empresa asignada.");
            return;
        }

        JsonObject body;
        try {
            body = gson.fromJson(request.getReader(), JsonObject.class);
        } catch (Exception e) {
            body = null;
        }
        if (body == null) {
            escribirJsonError(response, 400, "Body JSON inválido.");
            return;
        }

        String accion = getAsString(body, "accion");
        if (accion == null) {
            escribirJsonError(response, 400, "Debe enviar accion (crear|eliminar)." );
            return;
        }

        switch (accion.trim().toLowerCase()) {
            case "crear":
                crearUsuarioEmpresa(response, idEmpresa, body);
                break;
            case "eliminar":
                eliminarUsuarioEmpresa(response, idEmpresa, actual.getIdUsuario(), body);
                break;
            default:
                escribirJsonError(response, 400, "Acción inválida. Use crear|eliminar.");
        }
    }

    private void crearUsuarioEmpresa(HttpServletResponse response, int idEmpresa, JsonObject body) throws IOException {
        String correo = getAsString(body, "correo");
        String nombre = getAsString(body, "nombreCompleto");
        String fechaNacStr = getAsString(body, "fechaNacimiento");
        String password = getAsString(body, "password");

        if (correo == null || nombre == null || fechaNacStr == null) {
            escribirJsonError(response, 400, "Debe enviar correo, nombreCompleto y fechaNacimiento." );
            return;
        }

        correo = correo.trim().toLowerCase();
        if (usuarioModel.existeCorreo(correo)) {
            escribirJsonError(response, 409, "El correo ya está registrado." );
            return;
        }

        Date fechaNac;
        try {
            fechaNac = Date.valueOf(fechaNacStr.trim());
        } catch (Exception e) {
            escribirJsonError(response, 400, "fechaNacimiento inválida. Use YYYY-MM-DD." );
            return;
        }

        if (password == null || password.isBlank()) {
            password = "Temp" + (int)(Math.random() * 9000 + 1000);
        }

        Usuario_dtos nuevo = new Usuario_dtos();
        nuevo.setIdRol(ROL_USUARIO_EMPRESA);
        nuevo.setCorreo(correo);
        nuevo.setPassword(password);
        nuevo.setNombreCompleto(nombre);
        nuevo.setFechaNacimiento(fechaNac);
        nuevo.setEstado("ACTIVO");

        String nick = correo.contains("@") ? correo.substring(0, correo.indexOf('@')) : correo;
        nuevo.setNickname(nick);

        Usuario_dtos insertado = usuarioModel.insertar(nuevo);
        if (insertado == null || insertado.getIdUsuario() <= 0) {
            escribirJsonError(response, 500, "No se pudo crear el usuario.");
            return;
        }

        boolean vinc = empUsuModel.vincularUsuario(idEmpresa, insertado.getIdUsuario(), false);
        if (!vinc) {
            escribirJsonError(response, 500, "Usuario creado, pero falló el vínculo empresa_usuario.");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Usuario interno creado.");
        out.addProperty("idUsuario", insertado.getIdUsuario());
        out.addProperty("correo", correo);
        out.addProperty("password", password);
        escribirJson(response, out, 201);
    }

    private void eliminarUsuarioEmpresa(HttpServletResponse response, int idEmpresa, int idUsuarioSesion, JsonObject body) throws IOException {
        int idUsuario = getAsInt(body, "idUsuario");
        if (idUsuario <= 0) {
            escribirJsonError(response, 400, "Debe enviar idUsuario.");
            return;
        }
        if (idUsuario == idUsuarioSesion) {
            escribirJsonError(response, 400, "No puede eliminarse a sí mismo." );
            return;
        }

       
        boolean desv = empUsuModel.desvincularUsuario(idEmpresa, idUsuario);
        if (!desv) {
            escribirJsonError(response, 404, "No se encontró vínculo empresa_usuario." );
            return;
        }
        usuarioModel.cambiarEstado(idUsuario, "ELIMINADO");

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "Usuario interno eliminado (estado=ELIMINADO)." );
        escribirJson(response, out, 200);
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsString().trim() : null;
    }

    private int getAsInt(JsonObject obj, String field) {
        try {
            return (obj.has(field) && !obj.get(field).isJsonNull()) ? obj.get(field).getAsInt() : 0;
        } catch (Exception e) { return 0; }
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

