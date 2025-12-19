package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dtos.EmpresaRegistroRequest_dtos;
import dtos.Empresa_dtos;
import dtos.Empresa_usuario_dtos;
import dtos.UsuarioEmpresaRequest_dtos;
import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.Empresa_model;
import models.Usuario_model;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;

@WebServlet("/empresas")
public class Empresa extends HttpServlet {

    private static final int ROL_ADMIN_SISTEMA = 1;
    private static final int ROL_USUARIO_EMPRESA = 2;

    private final Empresa_model empresaModel = new Empresa_model();
    private final Usuario_model usuarioModel = new Usuario_model();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject jsonResp = new JsonObject();
        Gson gson = new Gson();

        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioActual") == null) {
            jsonResp.addProperty("ok", false);
            jsonResp.addProperty("mensaje", "No autenticado.");
            escribirJson(response, jsonResp, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Usuario_dtos admin = (Usuario_dtos) session.getAttribute("usuarioActual");
        if (admin.getIdRol() != ROL_ADMIN_SISTEMA) {
            jsonResp.addProperty("ok", false);
            jsonResp.addProperty("mensaje", "No autorizado.");
            escribirJson(response, jsonResp, HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            EmpresaRegistroRequest_dtos req = gson.fromJson(request.getReader(), EmpresaRegistroRequest_dtos.class);
            if (req == null || req.getEmpresa() == null || req.getUsuarioResponsable() == null) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Debe enviar 'empresa' y 'usuarioResponsable'.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Empresa_dtos empresa = req.getEmpresa();
            UsuarioEmpresaRequest_dtos uReq = req.getUsuarioResponsable();

            if (empresa.getNombre() == null || empresa.getNombre().isBlank()) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Nombre de empresa es obligatorio.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (uReq.getCorreo() == null || uReq.getCorreo().isBlank()
                    || uReq.getPassword() == null || uReq.getPassword().isBlank()
                    || uReq.getNombreCompleto() == null || uReq.getNombreCompleto().isBlank()
                    || uReq.getFechaNacimiento() == null || uReq.getFechaNacimiento().isBlank()) {

                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Usuario responsable requiere correo, password, nombreCompleto y fechaNacimiento.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Usuario_dtos usuarioResp = new Usuario_dtos();
            usuarioResp.setCorreo(uReq.getCorreo());
            usuarioResp.setPassword(uReq.getPassword());
            usuarioResp.setNombreCompleto(uReq.getNombreCompleto());
            usuarioResp.setTelefono(uReq.getTelefono());
            usuarioResp.setIdPais(uReq.getIdPais());
            usuarioResp.setNickname(uReq.getNickname());

            try {
                usuarioResp.setFechaNacimiento(Date.valueOf(uReq.getFechaNacimiento()));
            } catch (Exception ex) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "fechaNacimiento inválida. Use formato YYYY-MM-DD.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String correo = usuarioResp.getCorreo().trim().toLowerCase();
            usuarioResp.setCorreo(correo);

            if (usuarioModel.existeCorreo(correo)) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "El correo ya está registrado.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_CONFLICT);
                return;
            }

            usuarioResp.setIdRol(ROL_USUARIO_EMPRESA);
            if (usuarioResp.getEstado() == null || usuarioResp.getEstado().isBlank()) {
                usuarioResp.setEstado("ACTIVO");
            }

            if (usuarioResp.getNickname() == null || usuarioResp.getNickname().isBlank()) {
                String nick = correo.split("@")[0];
                usuarioResp.setNickname(nick);
            }

            Empresa_dtos empresaInsertada = empresaModel.insertar(empresa);
            if (empresaInsertada == null || empresaInsertada.getIdEmpresa() <= 0) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "No se pudo crear la empresa.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            Usuario_dtos usuarioInsertado = usuarioModel.insertar(usuarioResp);
            if (usuarioInsertado == null || usuarioInsertado.getIdUsuario() <= 0) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Empresa creada, pero no se pudo crear el usuario responsable.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            Empresa_usuario_dtos eu = new Empresa_usuario_dtos();
            eu.setIdEmpresa(empresaInsertada.getIdEmpresa());
            eu.setIdUsuario(usuarioInsertado.getIdUsuario());
            eu.setEsResponsable(true);

            boolean vinculoOk = empresaModel.registrarResponsableEmpresa(eu);
            if (!vinculoOk) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Empresa y usuario creados, pero falló el vínculo empresa_usuario.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            jsonResp.addProperty("ok", true);
            jsonResp.addProperty("mensaje", "Empresa y usuario responsable creados.");
            jsonResp.addProperty("idEmpresa", empresaInsertada.getIdEmpresa());
            jsonResp.addProperty("idUsuarioResponsable", usuarioInsertado.getIdUsuario());
            escribirJson(response, jsonResp, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            jsonResp.addProperty("ok", false);
            jsonResp.addProperty("mensaje", "Error procesando creación de empresa.");
            escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void escribirJson(HttpServletResponse response, JsonObject jsonResp, int statusCode)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        try (PrintWriter out = response.getWriter()) {
            out.print(new Gson().toJson(jsonResp));
            out.flush();
        }
    }
}
