package iut.dam.projetsaedon.admin;

public class RecurrentDonation {
    private int idDonRec;
    private double montant;
    private String debutdate;
    private String enddate;
    private boolean actif;
    private String frequency;

    public RecurrentDonation(int idDonRec, double montant, String debutdate, String enddate, boolean actif, String frequency) {
        this.idDonRec = idDonRec;
        this.montant = montant;
        this.debutdate = debutdate;
        this.enddate = enddate;
        this.actif = actif;
        this.frequency = frequency;
    }

    public int getIdDonRec() { return idDonRec; }
    public double getMontant() { return montant; }
    public String getDebutdate() { return debutdate; }
    public String getEnddate() { return enddate; }
    public boolean isActif() { return actif; }
    public String getFrequency() { return frequency; }
}
