package dtos;

import java.math.BigDecimal;

public class ReporteResumenPlataforma_dtos {
    private int totalCompras;
    private BigDecimal totalBruto;
    private BigDecimal totalComisionPlataforma;
    private BigDecimal totalNetoEmpresas;

    public int getTotalCompras() { return totalCompras; }
    public void setTotalCompras(int totalCompras) { this.totalCompras = totalCompras; }

    public BigDecimal getTotalBruto() { return totalBruto; }
    public void setTotalBruto(BigDecimal totalBruto) { this.totalBruto = totalBruto; }

    public BigDecimal getTotalComisionPlataforma() { return totalComisionPlataforma; }
    public void setTotalComisionPlataforma(BigDecimal totalComisionPlataforma) { this.totalComisionPlataforma = totalComisionPlataforma; }

    public BigDecimal getTotalNetoEmpresas() { return totalNetoEmpresas; }
    public void setTotalNetoEmpresas(BigDecimal totalNetoEmpresas) { this.totalNetoEmpresas = totalNetoEmpresas; }
}