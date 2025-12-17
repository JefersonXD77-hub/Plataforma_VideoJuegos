package dtos;

import java.math.BigDecimal;

public class Compra_detalle_dtos {

    private int idDetalle;
    private int idCompra;
    private int idVideojuego;
    private BigDecimal precioUnitario;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;
    private BigDecimal montoNetoEmpresa;

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdVideojuego() {
        return idVideojuego;
    }

    public void setIdVideojuego(int idVideojuego) {
        this.idVideojuego = idVideojuego;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(BigDecimal porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public BigDecimal getMontoComision() {
        return montoComision;
    }

    public void setMontoComision(BigDecimal montoComision) {
        this.montoComision = montoComision;
    }

    public BigDecimal getMontoNetoEmpresa() {
        return montoNetoEmpresa;
    }

    public void setMontoNetoEmpresa(BigDecimal montoNetoEmpresa) {
        this.montoNetoEmpresa = montoNetoEmpresa;
    }

}
