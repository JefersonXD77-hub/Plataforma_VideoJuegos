package services;

import dtos.Calificacion_videojuego_dtos;
import models.Calificacion_videojuego_model;
import models.Comentario_videojuego_model;

public class CalificacionService {

    private final Calificacion_videojuego_model model = new Calificacion_videojuego_model();
    private final Comentario_videojuego_model videojuegoCheck = new Comentario_videojuego_model();

    public static class ServiceResult<T> {
        public final boolean ok;
        public final int httpStatus;
        public final String mensaje;
        public final T data;

        private ServiceResult(boolean ok, int httpStatus, String mensaje, T data) {
            this.ok = ok;
            this.httpStatus = httpStatus;
            this.mensaje = mensaje;
            this.data = data;
        }

        public static <T> ServiceResult<T> ok(int httpStatus, String mensaje, T data) {
            return new ServiceResult<>(true, httpStatus, mensaje, data);
        }

        public static <T> ServiceResult<T> fail(int httpStatus, String mensaje) {
            return new ServiceResult<>(false, httpStatus, mensaje, null);
        }
    }

    public ServiceResult<Void> calificar(int idUsuario, int idVideojuego, int puntuacion) {
        if (idUsuario <= 0) return ServiceResult.fail(400, "idUsuario inválido.");
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");
        if (puntuacion < 1 || puntuacion > 5) return ServiceResult.fail(400, "La puntuación debe estar entre 1 y 5.");

        if (!videojuegoCheck.existeVideojuego(idVideojuego)) {
            return ServiceResult.fail(404, "Videojuego no existe.");
        }

        if (!videojuegoCheck.usuarioPoseeJuegoPropio(idUsuario, idVideojuego)) {
            return ServiceResult.fail(403, "Solo puedes calificar videojuegos que has comprado.");
        }

        boolean ok = model.upsert(idUsuario, idVideojuego, puntuacion);
        if (!ok) return ServiceResult.fail(500, "No se pudo guardar la calificación.");
        return ServiceResult.ok(200, "Calificación guardada.", null);
    }

    public ServiceResult<Calificacion_videojuego_dtos> miCalificacion(int idUsuario, int idVideojuego) {
        if (idUsuario <= 0) return ServiceResult.fail(400, "idUsuario inválido.");
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");

        Calificacion_videojuego_dtos c = model.buscar(idUsuario, idVideojuego);
        if (c == null) return ServiceResult.ok(200, "OK", null); 
        return ServiceResult.ok(200, "OK", c);
    }

    public ServiceResult<Double> promedio(int idVideojuego) {
        if (idVideojuego <= 0) return ServiceResult.fail(400, "idVideojuego inválido.");
        if (!videojuegoCheck.existeVideojuego(idVideojuego)) return ServiceResult.fail(404, "Videojuego no existe.");

        Double p = model.promedioPorVideojuego(idVideojuego);
        if (p == null) return ServiceResult.fail(500, "No se pudo calcular el promedio.");
        return ServiceResult.ok(200, "OK", p);
    }
}
