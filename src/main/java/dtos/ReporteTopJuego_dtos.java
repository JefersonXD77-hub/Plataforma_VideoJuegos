package dtos;

import java.math.BigDecimal;

public class ReporteTopJuego_dtos {
    private int idVideojuego;
    private String titulo;
    private int ventas;
    private BigDecimal ingresoBruto;

    public int getIdVideojuego() { return idVideojuego; }
    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getVentas() { return ventas; }
    public void setVentas(int ventas) { this.ventas = ventas; }

    public BigDecimal getIngresoBruto() { return ingresoBruto; }
    public void setIngresoBruto(BigDecimal ingresoBruto) { this.ingresoBruto = ingresoBruto; }
}
