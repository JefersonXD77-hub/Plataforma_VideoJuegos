package controllers;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Login_dtos;
import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.Usuario_model;

@WebServlet("/login")
public class Login extends HttpServlet {

    private static final int ROL_ADMIN_SISTEMA = 1;
    private final Usuario_model usuarioModel = new Usuario_model();

    /* GET /login */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject json = new JsonObject();
        json.addProperty("mensaje", "Endpoint de login activo. ");

        escribirJson(response, json, HttpServletResponse.SC_OK);
    }

   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject jsonResp = new JsonObject();

        try {
           
            Gson gson = new Gson();
            Login_dtos loginDTO = gson.fromJson(request.getReader(), Login_dtos.class);

            String correo = (loginDTO != null && loginDTO.getCorreo() != null)
                    ? loginDTO.getCorreo().trim().toLowerCase()
                    : null;
            String password = (loginDTO != null) ? loginDTO.getPassword() : null;

           
            if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Debe ingresar correo y contraseña.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            
            Usuario_dtos usuario = usuarioModel.buscarPorCorreoYPassword(correo, password);

            
            if (usuario == null) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Correo o contraseña incorrectos, o cuenta inactiva.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            
            HttpSession session = request.getSession(true);
            session.setAttribute("usuarioActual", usuario);

            
            jsonResp.addProperty("ok", true);
            jsonResp.addProperty("mensaje", "Login exitoso.");
            jsonResp.addProperty("idUsuario", usuario.getIdUsuario());
            jsonResp.addProperty("nickname", usuario.getNickname());
            jsonResp.addProperty("idRol", usuario.getIdRol());
            jsonResp.addProperty("estado", usuario.getEstado());
          

            boolean esAdmin = (usuario.getIdRol() == ROL_ADMIN_SISTEMA);
            jsonResp.addProperty("esAdmin", esAdmin);

            escribirJson(response, jsonResp, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            
            e.printStackTrace();
            jsonResp.addProperty("ok", false);
            jsonResp.addProperty("mensaje", "Error procesando el login.");
            escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

  
    private void escribirJson(HttpServletResponse response, JsonObject jsonResp, int statusCode)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        try ( PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            out.print(gson.toJson(jsonResp));
            out.flush();
        }
    }
}
