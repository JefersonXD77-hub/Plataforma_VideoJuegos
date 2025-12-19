package dtos;

public class EmpresaRegistroRequest_dtos {
    private Empresa_dtos empresa;
    private UsuarioEmpresaRequest_dtos usuarioResponsable;

    public Empresa_dtos getEmpresa() { return empresa; }
    public void setEmpresa(Empresa_dtos empresa) { this.empresa = empresa; }

    public UsuarioEmpresaRequest_dtos getUsuarioResponsable() { return usuarioResponsable; }
    public void setUsuarioResponsable(UsuarioEmpresaRequest_dtos usuarioResponsable) { this.usuarioResponsable = usuarioResponsable; }
}
