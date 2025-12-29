package dtos;

public class ReporteTopCalidad_dtos {
    private int idVideojuego;
    private String titulo;
    private String empresa;
    private double promedio;
    private int votos;

    public int getIdVideojuego() { return idVideojuego; }
    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public double getPromedio() { return promedio; }
    public void setPromedio(double promedio) { this.promedio = promedio; }

    public int getVotos() { return votos; }
    public void setVotos(int votos) { this.votos = votos; }
}