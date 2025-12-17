
package dtos;

import java.sql.Timestamp;


public class Prestamo_grupo_dtos {
    
    private int idPrestamo;
    private int idGrupo;
    private int idUsuarioDueno;
    private int idUsuarioReceptor;
    private int idVideojuego;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private String estado;

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdUsuarioDueno() {
        return idUsuarioDueno;
    }

    public void setIdUsuarioDueno(int idUsuarioDueno) {
        this.idUsuarioDueno = idUsuarioDueno;
    }

    public int getIdUsuarioReceptor() {
        return idUsuarioReceptor;
    }

    public void setIdUsuarioReceptor(int idUsuarioReceptor) {
        this.idUsuarioReceptor = idUsuarioReceptor;
    }

    public int getIdVideojuego() {
        return idVideojuego;
    }

    public void setIdVideojuego(int idVideojuego) {
        this.idVideojuego = idVideojuego;
    }

    public Timestamp getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Timestamp fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Timestamp getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Timestamp fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
}
