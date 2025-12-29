package dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CompraView_dtos {
    private int idCompra;
    private Timestamp fecha;
    private BigDecimal totalBruto;
    private BigDecimal totalComisionPlataforma;
    private BigDecimal totalNetoEmpresas;
    private String estado;

    private List<CompraDetalleView_dtos> detalles = new ArrayList<>();

    public int getIdCompra() { return idCompra; }
    public void setIdCompra(int idCompra) { this.idCompra = idCompra; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public BigDecimal getTotalBruto() { return totalBruto; }
    public void setTotalBruto(BigDecimal totalBruto) { this.totalBruto = totalBruto; }

    public BigDecimal getTotalComisionPlataforma() { return totalComisionPlataforma; }
    public void setTotalComisionPlataforma(BigDecimal totalComisionPlataforma) { this.totalComisionPlataforma = totalComisionPlataforma; }

    public BigDecimal getTotalNetoEmpresas() { return totalNetoEmpresas; }
    public void setTotalNetoEmpresas(BigDecimal totalNetoEmpresas) { this.totalNetoEmpresas = totalNetoEmpresas; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<CompraDetalleView_dtos> getDetalles() { return detalles; }
    public void setDetalles(List<CompraDetalleView_dtos> detalles) { this.detalles = detalles; }
}
