package dtos;

import java.math.BigDecimal;

public class Recomendacion_dtos {
    private int idVideojuego;
    private String titulo;
    private BigDecimal precio;
    private Double promedioCalificacion;
    private Integer ventas;
    private Double score;

    public int getIdVideojuego() { return idVideojuego; }
    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Double getPromedioCalificacion() { return promedioCalificacion; }
    public void setPromedioCalificacion(Double promedioCalificacion) { this.promedioCalificacion = promedioCalificacion; }

    public Integer getVentas() { return ventas; }
    public void setVentas(Integer ventas) { this.ventas = ventas; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}
