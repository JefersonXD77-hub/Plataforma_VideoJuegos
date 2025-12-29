package dtos;

import java.sql.Timestamp;

public class Comentario_videojuego_dtos {

    private int idComentario;
    private int idVideojuego;
    private int idUsuario;
    private Integer idComentarioPadre;
    private String texto;
    private Timestamp fecha;
    private boolean visibleEmpresa;
    private boolean visibleAdmin;

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public int getIdVideojuego() {
        return idVideojuego;
    }

    public void setIdVideojuego(int idVideojuego) {
        this.idVideojuego = idVideojuego;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdComentarioPadre() {
        return idComentarioPadre;
    }

    public void setIdComentarioPadre(Integer idComentarioPadre) {
        this.idComentarioPadre = idComentarioPadre;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public boolean isVisibleEmpresa() {
        return visibleEmpresa;
    }

    public void setVisibleEmpresa(boolean visibleEmpresa) {
        this.visibleEmpresa = visibleEmpresa;
    }

    public boolean isVisibleAdmin() {
        return visibleAdmin;
    }

    public void setVisibleAdmin(boolean visibleAdmin) {
        this.visibleAdmin = visibleAdmin;
    }

    private String nicknameUsuario;

    public String getNicknameUsuario() {
        return nicknameUsuario;
    }

    public void setNicknameUsuario(String nicknameUsuario) {
        this.nicknameUsuario = nicknameUsuario;
    }

}
