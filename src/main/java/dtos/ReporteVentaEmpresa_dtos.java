package dtos;

import java.math.BigDecimal;

public class ReporteVentaEmpresa_dtos {
    private int idEmpresa;
    private String nombreEmpresa;
    private int cantidadVentas;
    private BigDecimal montoBruto;
    private BigDecimal montoNeto;

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public int getCantidadVentas() { return cantidadVentas; }
    public void setCantidadVentas(int cantidadVentas) { this.cantidadVentas = cantidadVentas; }

    public BigDecimal getMontoBruto() { return montoBruto; }
    public void setMontoBruto(BigDecimal montoBruto) { this.montoBruto = montoBruto; }

    public BigDecimal getMontoNeto() { return montoNeto; }
    public void setMontoNeto(BigDecimal montoNeto) { this.montoNeto = montoNeto; }
}