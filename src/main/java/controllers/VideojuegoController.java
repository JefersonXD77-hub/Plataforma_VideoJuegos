
package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import dtos.Videojuegos_dtos;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import models.Videojuegos_model;

@WebServlet("/videojuegos")
public class VideojuegoController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private final Videojuegos_model videojuegosModel = new Videojuegos_model();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        String textoParam = request.getParameter("texto");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();

        try (PrintWriter out = response.getWriter()) {

            if (idParam != null && !idParam.isBlank()) {
                // Detalle de un videojuego por id
                int id = Integer.parseInt(idParam);
                Videojuegos_dtos juego = videojuegosModel.buscarPorId(id);

                JsonElement json = gson.toJsonTree(juego);
                out.print(gson.toJson(json));

            } else if (textoParam != null && !textoParam.isBlank()) {
                // Búsqueda por texto 
                List<Videojuegos_dtos> lista = videojuegosModel.buscarPorTexto(textoParam);

                JsonElement json = gson.toJsonTree(lista);
                out.print(gson.toJson(json));

            } else {
                
                List<Videojuegos_dtos> lista = videojuegosModel.listarJuegos();

                JsonElement json = gson.toJsonTree(lista);
                out.print(gson.toJson(json));
            }

            out.flush();

        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El id debe ser numérico");
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error consultando videojuegos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
                "Creación/edición de videojuegos aún no implementada.");
    }
}
