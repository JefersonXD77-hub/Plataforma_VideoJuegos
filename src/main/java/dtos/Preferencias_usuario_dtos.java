
package dtos;


public class Preferencias_usuario_dtos {
   
    private int idUsuario;
    private boolean bibliotecaPublica;
    private String avatarUrl;

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public boolean isBibliotecaPublica() {
        return bibliotecaPublica;
    }

    public void setBibliotecaPublica(boolean bibliotecaPublica) {
        this.bibliotecaPublica = bibliotecaPublica;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    
    
}
