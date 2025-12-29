package dtos;

public class ReporteTopVentas_dtos {
    private int idVideojuego;
    private String titulo;
    private String empresa;
    private int ventas;

    public int getIdVideojuego() { return idVideojuego; }
    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public int getVentas() { return ventas; }
    public void setVentas(int ventas) { this.ventas = ventas; }
}

