package dtos;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Compra_dtos {

    private int idCompra;
    private int idUsuario;
    private Timestamp fecha;
    private BigDecimal totalBruto;
    private BigDecimal totalComisionPlataforma;
    private BigDecimal totalNetoEmpresas;
    private String estado;

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getTotalComisionPlataforma() {
        return totalComisionPlataforma;
    }

    public void setTotalComisionPlataforma(BigDecimal totalComisionPlataforma) {
        this.totalComisionPlataforma = totalComisionPlataforma;
    }

    public BigDecimal getTotalNetoEmpresas() {
        return totalNetoEmpresas;
    }

    public void setTotalNetoEmpresas(BigDecimal totalNetoEmpresas) {
        this.totalNetoEmpresas = totalNetoEmpresas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
