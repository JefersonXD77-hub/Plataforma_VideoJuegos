
package dtos;

import java.sql.Timestamp;


public class Grupo_miembro_dtos {
   
    private int idGrupo;
    private int idUsuario;
    private String rol;
    private String estado;
    private Timestamp fechaInvitacion;
    private Timestamp fechaEstado;

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaInvitacion() {
        return fechaInvitacion;
    }

    public void setFechaInvitacion(Timestamp fechaInvitacion) {
        this.fechaInvitacion = fechaInvitacion;
    }

    public Timestamp getFechaEstado() {
        return fechaEstado;
    }

    public void setFechaEstado(Timestamp fechaEstado) {
        this.fechaEstado = fechaEstado;
    }
    
}
