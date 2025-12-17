
package dtos;

import java.sql.Timestamp;


public class Empresa_usuario_dtos {
   
    private int idEmpresa;
    private int idUsuario;
    private boolean esResponsable;
    private Timestamp fechaIngreso;

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public boolean isEsResponsable() {
        return esResponsable;
    }

    public void setEsResponsable(boolean esResponsable) {
        this.esResponsable = esResponsable;
    }

    public Timestamp getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Timestamp fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
    
}
