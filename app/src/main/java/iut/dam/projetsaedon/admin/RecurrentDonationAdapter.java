package iut.dam.projetsaedon.admin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import iut.dam.projetsaedon.R;

/**
 * Adaptateur permettant d'afficher une liste d'objets {@link RecurrentDonation} dans une ListView.
 * Chaque élément de la liste est représenté par le layout {@code R.layout.item_recurrent_donation}
 * et affiche les informations du don récurrent (montant, fréquence, dates de début/fin, et état actif).
 * La couleur de fond de l'élément change selon que le don est actif ou non.
 */
public class RecurrentDonationAdapter extends ArrayAdapter<RecurrentDonation> {

    /**
     * Constructeur de l'adaptateur.
     *
     * @param context   Le contexte dans lequel l'adaptateur est utilisé.
     * @param donations La liste des dons récurrents à afficher.
     */
    public RecurrentDonationAdapter(Context context, List<RecurrentDonation> donations) {
        super(context, 0, donations);
    }

    /**
     * Retourne la vue correspondant à un élément de la liste à la position spécifiée.
     * Si la vue n'existe pas encore, elle est fait depuis le layout {@code R.layout.item_recurrent_donation}.
     * Les données de l'objet {@link RecurrentDonation} sont ensuite liées aux composants de la vue.
     * La couleur de fond de l'élément est définie en fonction de l'état actif du don.
     *
     * @param position    La position de l'élément dans la liste.
     * @param convertView La vue à réutiliser, si possible.
     * @param parent      Le ViewGroup parent auquel la nouvelle vue sera attachée.
     * @return La vue correspondante à l'élément à afficher.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecurrentDonation donation = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_recurrent_donation, parent, false);
        }

        LinearLayout itemLayout = (LinearLayout) convertView.findViewById(R.id.itemLayout);

        TextView textMontant = convertView.findViewById(R.id.textMontant);
        TextView textFrequency = convertView.findViewById(R.id.textFrequency);
        TextView textDebut = convertView.findViewById(R.id.textDebut);
        TextView textEnd = convertView.findViewById(R.id.textEnd);
        TextView textActif = convertView.findViewById(R.id.textActif);

        textMontant.setText("Montant: " + donation.getMontant() + " €");
        textFrequency.setText("Fréquence: " + donation.getFrequency());
        textDebut.setText("Début: " + donation.getDebutdate());
        textEnd.setText("Fin: " + ((donation.getEnddate() == null || donation.getEnddate().trim().isEmpty()
                || donation.getEnddate().equalsIgnoreCase("null"))
                ? "En cours"
                : donation.getEnddate()));
        textActif.setText("Actif: " + (donation.isActif() ? "Oui" : "Non"));

        if (donation.isActif()) {
            itemLayout.setBackgroundColor(Color.parseColor("#ccffcc"));
        } else {
            itemLayout.setBackgroundColor(Color.parseColor("#ffcccc"));
        }
        return convertView;
    }
}
