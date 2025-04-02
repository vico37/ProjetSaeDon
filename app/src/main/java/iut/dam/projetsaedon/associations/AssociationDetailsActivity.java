package iut.dam.projetsaedon.associations;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.donation.DonationNormalActivity;
import iut.dam.projetsaedon.donation.RecurringDonationActivity;
import iut.dam.projetsaedon.login.LoginActivity;

public class AssociationDetailsActivity extends AppCompatActivity {

    private TextView textViewNomAsso, textViewDescriptif, textViewMail, textViewTel, textViewAdresse;
    private Button buttonDonSimple, buttonDonRecurrent;
    private static final String ASSO_DETAILS_URL = "http://donation.out-online.net/donation_app_bdd/get_asso_details.php";

    // Variables pour stocker les infos récupérées
    private String associationId;      // L'id numérique récupéré (champ idAsso)
    private String associationName = "Inconnue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association_details);

        textViewNomAsso = findViewById(R.id.title_nom_asso);
        // Pour simplifier, nous utiliserons le CardView contenant le descriptif comme description
        // Vous pouvez adapter selon vos besoins
        TextView descAsso = findViewById(R.id.desc_asso);
        textViewMail = findViewById(R.id.subtitle_contact_asso); // Exemple, vous pouvez utiliser d'autres vues pour le contact
        // Vous pouvez ajouter d'autres TextView pour téléphone, adresse si besoin.

        buttonDonSimple = findViewById(R.id.buttonDonSimple);
        buttonDonRecurrent = findViewById(R.id.buttonDonRecurrent);

        // Récupérer l'identifiant de l'association depuis l'URI (deep link) ou l'extra
        Uri data = getIntent().getData();
        String inputId = null;
        if (data != null) {
            if (data.getQueryParameter("qr_code") != null) {
                inputId = data.getQueryParameter("qr_code");
            } else if (data.getQueryParameter("id") != null) {
                inputId = data.getQueryParameter("id");
            }
        }
        if (inputId == null || inputId.isEmpty()) {
            inputId = getIntent().getStringExtra("associationId");
        }
        if (inputId == null || inputId.isEmpty()) {
            Toast.makeText(this, "Aucun identifiant d'association fourni", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Charger les détails de l'association depuis le serveur
        loadAssociationDetails(inputId);

        // Bouton "Don simple" : lancer DonationNormalActivity
        buttonDonSimple.setOnClickListener(v -> {
            if (associationId == null || associationId.isEmpty()) {
                Toast.makeText(AssociationDetailsActivity.this, "Les détails de l'association ne sont pas encore chargés", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(AssociationDetailsActivity.this, DonationNormalActivity.class);
            intent.putExtra("associationId", associationId);
            intent.putExtra("associationName", associationName);
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String userId = prefs.getString("userId", "");
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        // Bouton "Don récurrent" : vérifier si l'utilisateur est connecté
        buttonDonRecurrent.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String userId = prefs.getString("userId", "");
            if (userId.isEmpty()) {
                // L'utilisateur n'est pas connecté, proposer de se connecter
                new AlertDialog.Builder(AssociationDetailsActivity.this)
                        .setTitle("Connexion requise")
                        .setMessage("Pour effectuer un don récurrent, vous devez être connecté. Voulez-vous vous connecter ?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            Intent intent = new Intent(AssociationDetailsActivity.this, LoginActivity.class);
                            intent.putExtra("redirectTo", "RecurringDonation");
                            intent.putExtra("associationId", associationId);
                            intent.putExtra("associationName", associationName);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Non", (dialog, which) -> {
                            dialog.dismiss();
                            Toast.makeText(AssociationDetailsActivity.this, "Vous restez sur la page", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                // L'utilisateur est connecté, lancer RecurringDonationActivity
                Intent intent = new Intent(AssociationDetailsActivity.this, RecurringDonationActivity.class);
                intent.putExtra("associationId", associationId);
                intent.putExtra("associationName", associationName);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }



    private void loadAssociationDetails(String inputId) {
        new Thread(() -> {
            try {
                String paramKey;
                try {
                    Integer.parseInt(inputId);
                    paramKey = "associationId";
                } catch (NumberFormatException e) {
                    paramKey = "qr_code";
                }
                String params = "?" + paramKey + "=" + URLEncoder.encode(inputId, "UTF-8");
                URL url = new URL(ASSO_DETAILS_URL + params);
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

                JSONObject json = new JSONObject(sb.toString());
                final String idAsso = json.optString("idAsso", "");
                if (idAsso == null || idAsso.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(AssociationDetailsActivity.this, "Association non trouvée", Toast.LENGTH_LONG).show());
                    return;
                }
                // Mettre à jour associationId avec l'id numérique
                associationId = idAsso;
                final String nomAsso = json.optString("nomAsso", "Inconnu");
                associationName = nomAsso;
                final String descriptif = json.optString("descriptif", "Aucun descriptif");
                final String mailAsso = json.optString("mailAsso", "Non défini");
                final String telAsso = json.optString("telAsso", "Non défini");
                final String adresseAsso = json.optString("adresseAsso", "Non défini");

                runOnUiThread(() -> {
                    textViewNomAsso.setText(nomAsso);
                    // Pour simplifier, on affiche le descriptif dans le CardView
                    ((TextView)findViewById(R.id.desc_asso)).setText(descriptif);
                    // Utiliser le subtitle pour le contact
                    textViewMail.setText("Mail : " + mailAsso + " | Tel : " + telAsso + " | Adresse : " + adresseAsso);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(AssociationDetailsActivity.this, "Erreur lors du chargement des détails", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
