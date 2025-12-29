package services;

import dtos.Recomendacion_dtos;
import models.Recomendacion_model;

import java.util.List;

public class RecomendacionService {

    private final Recomendacion_model model = new Recomendacion_model();

    public ServiceResult<List<Recomendacion_dtos>> mejorBalance(Integer limit, Integer m) {
        int lim = (limit == null) ? 20 : limit;
        int mm = (m == null) ? 10 : m;

        if (lim <= 0) lim = 20;
        if (mm <= 0) mm = 10;

        List<Recomendacion_dtos> lista = model.mejorBalance(lim, mm);
        return ServiceResult.ok(200, "OK", lista);
    }
}
