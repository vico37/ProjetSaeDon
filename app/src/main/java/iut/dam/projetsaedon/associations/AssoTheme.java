package iut.dam.projetsaedon.associations;

public class AssoTheme {
    private int idAsso;
    private int idTheme;

    private String themeNom;

    public AssoTheme(int idAsso, int idTheme, String themeNom) {
        this.idAsso = idAsso;
        this.idTheme = idTheme;
        this.themeNom = themeNom;
    }


    public String getThemeNom() {
        return themeNom;
    }

    public int getIdAsso() {
        return idAsso;
    }

    public int getIdTheme() {
        return idTheme;
    }
}
