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

public class RecurrentDonationAdapter extends ArrayAdapter<RecurrentDonation> {

    public RecurrentDonationAdapter(Context context, List<RecurrentDonation> donations) {
        super(context, 0, donations);
    }

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

        if(donation.isActif()) {
            itemLayout.setBackgroundColor(Color.parseColor("#ccffcc")); // Vert clair pour actif
        } else {
            itemLayout.setBackgroundColor(Color.parseColor("#ffcccc")); // Rouge clair pour inactif
        }
        return convertView;
    }
}
