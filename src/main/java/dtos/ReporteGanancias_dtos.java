package dtos;

public class ReporteGanancias_dtos {
    private double totalBruto;
    private double totalComisionPlataforma;
    private double totalNetoEmpresas;

    public double getTotalBruto() { return totalBruto; }
    public void setTotalBruto(double totalBruto) { this.totalBruto = totalBruto; }

    public double getTotalComisionPlataforma() { return totalComisionPlataforma; }
    public void setTotalComisionPlataforma(double totalComisionPlataforma) { this.totalComisionPlataforma = totalComisionPlataforma; }

    public double getTotalNetoEmpresas() { return totalNetoEmpresas; }
    public void setTotalNetoEmpresas(double totalNetoEmpresas) { this.totalNetoEmpresas = totalNetoEmpresas; }
}
