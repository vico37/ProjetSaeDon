package iut.dam.projetsaedon.mesdonsrecurents;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.admin.RecurrentDonation;

/**
 * Activité affichant la liste des dons récurrents de l'utilisateur.
 * Cette activité charge les dons récurrents depuis un service web et les affiche
 * dans une ListView à l'aide d'un adapter personnalisé. Chaque item de la liste affiche
 * les informations du don, et permet de désactiver un don actif via un bouton dédié.
 */
public class MyRecurringDonationsActivity extends AppCompatActivity {

    private ListView listViewRecurring;
    private DonationAdapter adapter;
    private ArrayList<RecurrentDonation> donationList;
    private static final String LIST_URL = "http://donation.out-online.net/donation_app_bdd/user_donations_list.php";
    private static final String DISABLE_URL = "http://donation.out-online.net/donation_app_bdd/disable_donation.php";
    private String userId;
    private Button buttonBack;

    /**
     * Méthode appelée lors de la création de l'activité.
     * Elle initialise l'interface utilisateur, configure la ListView avec son adapter
     * et récupère l'identifiant utilisateur depuis les SharedPreferences.
     * Elle lance ensuite le chargement des dons récurrents.
     *
     * @param savedInstanceState save d'état
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recurring_donations);

        listViewRecurring = findViewById(R.id.listViewRecurring);
        buttonBack = findViewById(R.id.buttonBack);
        donationList = new ArrayList<>();
        adapter = new DonationAdapter(this, donationList);
        listViewRecurring.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "0");

        loadDonations(userId);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Charge les dons récurrents de l'utilisateur à partir du service web.
     * La méthode effectue une requête HTTP GET sur l'URL définie pour récupérer la liste
     * des dons récurrents. La réponse JSON est analysée pour créer des objets
     * {@link RecurrentDonation} qui sont ajoutés à la liste. Ensuite, l'interface est mise à jour
     * sur le thread principal.
     *
     * @param userId L'identifiant de l'utilisateur.
     */
    private void loadDonations(String userId) {
        new Thread(() -> {
            try {
                String urlStr = LIST_URL + "?userId=" + URLEncoder.encode(userId, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONArray array = new JSONArray(sb.toString());
                donationList.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    donationList.add(new RecurrentDonation(
                            obj.getInt("idDonRec"),
                            obj.getDouble("montant"),
                            obj.getString("debutdate"),
                            obj.optString("enddate"),
                            obj.getInt("actif") == 1,
                            obj.getString("frequency")
                    ));
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MyRecurringDonationsActivity.this, "Erreur lors du chargement des dons", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    /**
     * Désactive un don récurrent en envoyant une requête au service web.
     * Si la requête aboutit, l'activité affiche un message de confirmation et recharge la liste des dons.
     * Sinon, un message d'erreur est affiché.
     *
     * @param idDonRec L'identifiant du don à désactiver.
     */
    private void disableDonation(int idDonRec) {
        new Thread(() -> {
            try {
                String urlStr = DISABLE_URL + "?idDonRec=" + URLEncoder.encode(String.valueOf(idDonRec), "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                conn.disconnect();
                if (responseCode == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(MyRecurringDonationsActivity.this, "Don désactivé", Toast.LENGTH_SHORT).show();
                        loadDonations(userId);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MyRecurringDonationsActivity.this, "Erreur lors de la désactivation", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MyRecurringDonationsActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Adapter personnalisé pour afficher les dons récurrents dans la ListView.
     */
    private class DonationAdapter extends ArrayAdapter<RecurrentDonation> {

        /**
         * Constructeur de l'adaptateur.
         *
         * @param context   Le contexte d'exécution.
         * @param donations La liste des dons récurrents à afficher.
         */
        public DonationAdapter(Context context, ArrayList<RecurrentDonation> donations) {
            super(context, 0, donations);
        }

        /**
         * Retourne la vue pour un élément de la liste.
         * Chaque item affiche les informations du don et un bouton permettant de le désactiver
         * si celui-ci est actif. La couleur de fond change en fonction de l'état du don.
         *
         * @param position    La position de l'élément dans la liste.
         * @param convertView La vue à réutiliser, si possible.
         * @param parent      Le ViewGroup parent.
         * @return La vue de l'élément.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RecurrentDonation donation = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_my_donation, parent, false);
            }
            LinearLayout itemLayout = convertView.findViewById(R.id.itemLayout);
            TextView textInfo = convertView.findViewById(R.id.textInfo);
            Button buttonDesactiver = convertView.findViewById(R.id.buttonDesactiver);

            String info = "Montant: " + donation.getMontant() + " €\n" +
                    "Début: " + donation.getDebutdate() + "\n" +
                    "Fin: " + ((donation.getEnddate() == null || donation.getEnddate().trim().isEmpty() || donation.getEnddate().equalsIgnoreCase("null"))
                    ? "En cours" : donation.getEnddate()) + "\n" +
                    "Fréquence: " + donation.getFrequency() + "\n" +
                    "Actif: " + (donation.isActif() ? "Oui" : "Non");
            textInfo.setText(info);

            if (!donation.isActif()) {
                buttonDesactiver.setEnabled(false);
                buttonDesactiver.setText("Désactivé");
            } else {
                buttonDesactiver.setEnabled(true);
                buttonDesactiver.setText("Désactiver");
                buttonDesactiver.setOnClickListener(v -> disableDonation(donation.getIdDonRec()));
            }

            if(donation.isActif()) {
                convertView.setBackgroundColor(Color.parseColor("#ccffcc")); // Vert clair pour actif
            } else {
                convertView.setBackgroundColor(Color.parseColor("#ffcccc")); // Rouge clair pour inactif
            }
            return convertView;
        }
    }
}
