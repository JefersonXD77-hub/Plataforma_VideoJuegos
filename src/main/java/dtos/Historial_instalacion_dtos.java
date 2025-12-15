
package dtos;

import java.sql.Timestamp;

public class Historial_instalacion_dtos {
  
    private int idHistorial;
    private int idUsuario;
    private int idVideojuego;
    private String origen;
    private Timestamp fechaInstalacion;
    private Timestamp fechaDesinstalacion;

    public int getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(int idHistorial) {
        this.idHistorial = idHistorial;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdVideojuego() {
        return idVideojuego;
    }

    public void setIdVideojuego(int idVideojuego) {
        this.idVideojuego = idVideojuego;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public Timestamp getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(Timestamp fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public Timestamp getFechaDesinstalacion() {
        return fechaDesinstalacion;
    }

    public void setFechaDesinstalacion(Timestamp fechaDesinstalacion) {
        this.fechaDesinstalacion = fechaDesinstalacion;
    }
    
    
}
