package dtos;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class BibliotecaItemView_dtos {

    // datos biblioteca
    private int idBiblioteca;
    private int idUsuario;
    private int idVideojuego;
    private String origen;
    private int idUsuarioDueno;
    private Timestamp fechaAlta;

    // datos videojuego 
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private String estado;
    private Integer idClasificacion;
    private Integer idEmpresa;
    private Date fechaLanzamiento;

    public int getIdBiblioteca() { return idBiblioteca; }
    public void setIdBiblioteca(int idBiblioteca) { this.idBiblioteca = idBiblioteca; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdVideojuego() { return idVideojuego; }
    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public int getIdUsuarioDueno() { return idUsuarioDueno; }
    public void setIdUsuarioDueno(int idUsuarioDueno) { this.idUsuarioDueno = idUsuarioDueno; }

    public Timestamp getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(Timestamp fechaAlta) { this.fechaAlta = fechaAlta; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIdClasificacion() { return idClasificacion; }
    public void setIdClasificacion(Integer idClasificacion) { this.idClasificacion = idClasificacion; }

    public Integer getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(Integer idEmpresa) { this.idEmpresa = idEmpresa; }

    public Date getFechaLanzamiento() { return fechaLanzamiento; }
    public void setFechaLanzamiento(Date fechaLanzamiento) { this.fechaLanzamiento = fechaLanzamiento; }
}
