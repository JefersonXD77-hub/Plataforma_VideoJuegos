package dtos;

import java.util.Date;

public class Notificacion_dtos {
    private String tipo; 
    private String titulo;
    private String mensaje;
    private Date fecha;
    private String refTipo; 
    private int refId;

    public Notificacion_dtos() {}

    public Notificacion_dtos(String tipo, String titulo, String mensaje, Date fecha, String refTipo, int refId) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.refTipo = refTipo;
        this.refId = refId;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getRefTipo() { return refTipo; }
    public void setRefTipo(String refTipo) { this.refTipo = refTipo; }

    public int getRefId() { return refId; }
    public void setRefId(int refId) { this.refId = refId; }
}
