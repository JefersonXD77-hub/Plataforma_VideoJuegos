package services;

public class ServiceResult<T> {

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
