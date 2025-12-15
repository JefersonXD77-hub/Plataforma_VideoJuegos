
package dtos;

import java.sql.Timestamp;


public class Biblioteca_usuario_dtos {
    
    private int idBiblioteca;
    private int idUsuario;
    private int idVideojuego;
    private String origen;
    private int idUsuarioDueno;
    private Timestamp fechaAlta;

    public int getIdBiblioteca() {
        return idBiblioteca;
    }

    public void setIdBiblioteca(int idBiblioteca) {
        this.idBiblioteca = idBiblioteca;
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

    public int getIdUsuarioDueno() {
        return idUsuarioDueno;
    }

    public void setIdUsuarioDueno(int idUsuarioDueno) {
        this.idUsuarioDueno = idUsuarioDueno;
    }

    public Timestamp getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Timestamp fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

}
