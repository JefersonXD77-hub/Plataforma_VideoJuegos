package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import services.BannerService;
import services.BannerService.ServiceResult;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/banner")
public class BannerPublicoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final Gson gson = new Gson();
    private final BannerService bannerService = new BannerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ServiceResult<?> r = bannerService.listarActivosVigentes();

        JsonObject json = new JsonObject();
        json.addProperty("ok", r.ok);
        json.addProperty("mensaje", r.mensaje);
        if (r.data != null) json.add("data", gson.toJsonTree(r.data));

        response.setStatus(r.httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(json));
        }
    }
}
