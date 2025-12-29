package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import models.Perfil_model;
import services.BibliotecaService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/perfiles")
public class PerfilController extends HttpServlet {

    private final Gson gson = new Gson();
    private final Perfil_model perfilModel = new Perfil_model();
    private final BibliotecaService bibliotecaService = new BibliotecaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nickname = request.getParameter("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            escribirJsonError(response, 400, "Debe enviar nickname.");
            return;
        }

        Perfil_model.PerfilPublico perfil = perfilModel.obtenerPerfilPublicoPorNickname(nickname.trim());
        if (perfil == null || perfil.usuario == null) {
            escribirJsonError(response, 404, "Usuario no encontrado.");
            return;
        }

        JsonObject out = new JsonObject();
        out.addProperty("ok", true);
        out.addProperty("mensaje", "OK");

        JsonObject data = new JsonObject();
        data.add("usuario", gson.toJsonTree(perfil.usuario));
        data.add("preferencias", gson.toJsonTree(perfil.preferencias));
        data.addProperty("paisNombre", perfil.paisNombre);

        
        if (perfil.preferencias != null && perfil.preferencias.isBibliotecaPublica()) {
            var r = bibliotecaService.miBiblioteca(perfil.usuario.getIdUsuario());
            if (r.ok && r.data != null) {
                data.add("biblioteca", gson.toJsonTree(r.data));
            }
        }

        out.add("data", data);
        escribirJson(response, out, 200);
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
        try ( PrintWriter pw = response.getWriter()) {
            pw.print(gson.toJson(out));
        }
    }
}
