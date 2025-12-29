package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Usuario_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Registro_model;

@WebServlet("/registro")
public class Registro extends HttpServlet {

    private static final long serialVersionUID = 1L;

   
    private static final int ID_ROL_COMUN = 3;

    private final Registro_model registroModel = new Registro_model();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        Gson gson = new Gson();
        JsonObject json = new JsonObject();

        if ("checkCorreo".equalsIgnoreCase(accion)) {
            // Validar correo único
            String correo = request.getParameter("correo");

            if (correo == null || correo.isBlank()) {
                json.addProperty("ok", false);
                json.addProperty("mensaje", "Debe enviar el parámetro 'correo'.");
                escribirJson(response, json, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            boolean disponible = registroModel.esCorreoDisponible(correo.trim().toLowerCase());

            json.addProperty("ok", true);
            json.addProperty("disponible", disponible);
            if (disponible) {
                json.addProperty("mensaje", "Correo disponible.");
            } else {
                json.addProperty("mensaje", "El correo ya está registrado.");
            }

            escribirJson(response, json, HttpServletResponse.SC_OK);
            return;
        }

      
        json.addProperty("mensaje", "Endpoint de registro activo. Use POST para registrar usuario común.");
        escribirJson(response, json, HttpServletResponse.SC_OK);
    }

  
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject jsonResp = new JsonObject();

        try {
           
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
            if (body == null) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Body JSON vacío o inválido.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

           
            String nickname          = getAsString(body, "nickname");
            String nombreCompleto    = getAsString(body, "nombreCompleto");
            String correo            = getAsString(body, "correo");
            String password          = getAsString(body, "password");
            String confirmarPassword = getAsString(body, "confirmarPassword");
            String fechaNacStr       = getAsString(body, "fechaNacimiento");
            String telefono          = getAsString(body, "telefono");
            Integer idPais           = getAsInteger(body, "idPais"); 

         
            if (isBlank(nickname) || isBlank(correo) || isBlank(password) ||
                isBlank(confirmarPassword) || isBlank(fechaNacStr)) {

                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Faltan campos obligatorios.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

         
            if (!password.equals(confirmarPassword)) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Las contraseñas no coinciden.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

          
            if (!correo.contains("@")) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Formato de correo inválido.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

          
            boolean disponible = registroModel.esCorreoDisponible(correo.trim().toLowerCase());
            if (!disponible) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "El correo electrónico ya está registrado.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_CONFLICT);
                return;
            }

            
            LocalDate hoy = LocalDate.now();
            LocalDate fechaNac;
            try {
                fechaNac = LocalDate.parse(fechaNacStr); 
            } catch (Exception ex) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "Formato de fechaNacimiento inválido (use yyyy-MM-dd).");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (fechaNac.isAfter(hoy)) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "La fecha de nacimiento no puede ser futura.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            
            Usuario_dtos nuevo = new Usuario_dtos();
            nuevo.setNickname(nickname);
            nuevo.setNombreCompleto(nombreCompleto);
            nuevo.setCorreo(correo.trim().toLowerCase());
            nuevo.setPassword(password);
            nuevo.setFechaNacimiento(Date.valueOf(fechaNac)); 
            nuevo.setTelefono(telefono);
            nuevo.setIdPais(idPais);
            nuevo.setIdRol(ID_ROL_COMUN); 
            Usuario_dtos creado = registroModel.registrarUsuarioComun(nuevo, ID_ROL_COMUN);

            if (creado == null || creado.getIdUsuario() == 0) {
                jsonResp.addProperty("ok", false);
                jsonResp.addProperty("mensaje", "No se pudo completar el registro.");
                escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

          
            jsonResp.addProperty("ok", true);
            jsonResp.addProperty("mensaje", "Registro exitoso. Ahora puede iniciar sesión.");
            jsonResp.addProperty("idUsuario", creado.getIdUsuario());
            jsonResp.addProperty("nickname", creado.getNickname());
            jsonResp.addProperty("correo", creado.getCorreo());

            escribirJson(response, jsonResp, HttpServletResponse.SC_CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            jsonResp.addProperty("ok", false);
            jsonResp.addProperty("mensaje", "Error procesando el registro.");
            escribirJson(response, jsonResp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

 

    private void escribirJson(HttpServletResponse response, JsonObject jsonResp, int statusCode)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            out.print(gson.toJson(jsonResp));
            out.flush();
        }
    }

    private String getAsString(JsonObject obj, String field) {
        return (obj.has(field) && !obj.get(field).isJsonNull())
                ? obj.get(field).getAsString().trim()
                : null;
    }

    private Integer getAsInteger(JsonObject obj, String field) {
        if (!obj.has(field) || obj.get(field).isJsonNull()) {
            return null;
        }
        try {
            return obj.get(field).getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
