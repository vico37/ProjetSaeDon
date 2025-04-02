package iut.dam.projetsaedon.admin;

/**
 * Représente un don récurrent dans l'application
 * Cette classe encapsule les informations relatives à un don récurrent,
 * telles que l'identifiant du don, le montant, la date de début, la date de fin,
 * l'état actif et la fréquence du don.
 */
public class RecurrentDonation {
    private int idDonRec;
    private double montant;
    private String debutdate;
    private String enddate;
    private boolean actif;
    private String frequency;

    /**
     * Constructeur pour créer une instance de {@code RecurrentDonation}.
     *
     * @param idDonRec L'identifiant unique du don récurrent.
     * @param montant Le montant du don.
     * @param debutdate La date de début du don (format attendu par exemple "yyyy-MM-dd").
     * @param enddate La date de fin du don (format attendu par exemple "yyyy-MM-dd").
     * @param actif {@code true} si le don est actif, {@code false} sinon.
     * @param frequency La fréquence du don (exemple : "mensuel", "annuel").
     */
    public RecurrentDonation(int idDonRec, double montant, String debutdate, String enddate, boolean actif, String frequency) {
        this.idDonRec = idDonRec;
        this.montant = montant;
        this.debutdate = debutdate;
        this.enddate = enddate;
        this.actif = actif;
        this.frequency = frequency;
    }

    /**
     * Renvoie l'identifiant unique du don récurrent.
     *
     * @return L'identifiant du don récurrent.
     */
    public int getIdDonRec() {
        return idDonRec;
    }

    /**
     * Renvoie le montant du don.
     *
     * @return Le montant du don.
     */
    public double getMontant() {
        return montant;
    }

    /**
     * Renvoie la date de début du don.
     *
     * @return La date de début sous forme de chaîne de caractères.
     */
    public String getDebutdate() {
        return debutdate;
    }

    /**
     * Renvoie la date de fin du don.
     *
     * @return La date de fin sous forme de chaîne de caractères.
     */
    public String getEnddate() {
        return enddate;
    }

    /**
     * Indique si le don est actif.
     *
     * @return {@code true} si le don est actif, sinon {@code false}.
     */
    public boolean isActif() {
        return actif;
    }

    /**
     * Renvoie la fréquence du don.
     *
     * @return La fréquence du don (exemple : "mensuel", "annuel").
     */
    public String getFrequency() {
        return frequency;
    }
}
