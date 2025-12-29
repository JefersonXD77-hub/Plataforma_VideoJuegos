package dtos;

import java.sql.Timestamp;


public class VideojuegoCategoriaModeracionItem_dtos {

    private int idVideojuego;
    private String tituloVideojuego;
    private int idEmpresa;
    private String nombreEmpresa;
    private int idCategoria;
    private String nombreCategoria;
    private String estado;
    private Timestamp fechaSolicitud;

    public int getIdVideojuego() { return idVideojuego; }
    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }

    public String getTituloVideojuego() { return tituloVideojuego; }
    public void setTituloVideojuego(String tituloVideojuego) { this.tituloVideojuego = tituloVideojuego; }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Timestamp fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}
