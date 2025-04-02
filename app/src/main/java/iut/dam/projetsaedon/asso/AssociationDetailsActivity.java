package iut.dam.projetsaedon.asso;

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

public class AssociationDetailsActivity extends AppCompatActivity {

    private TextView textViewNomAsso, textViewDescriptif, textViewMail, textViewTel, textViewAdresse;
    private Button buttonDonSimple, buttonDonRecurrent;

    // URL du script PHP pour récupérer les détails de l'association
    private static final String ASSO_DETAILS_URL = "http://donation.out-online.net/donation_app_bdd/get_asso_details.php";

    // Ces variables stockeront les infos récupérées
    private String associationId;      // l'id numérique récupéré de la BDD (champ idAsso)
    private String associationName = "Inconnue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association_details);

        textViewNomAsso = findViewById(R.id.textViewNomAsso);
        textViewDescriptif = findViewById(R.id.textViewDescriptif);
        textViewMail = findViewById(R.id.textViewMail);
        textViewTel = findViewById(R.id.textViewTel);
        textViewAdresse = findViewById(R.id.textViewAdresse);
        buttonDonSimple = findViewById(R.id.buttonDonSimple);
        buttonDonRecurrent = findViewById(R.id.buttonDonRecurrent);

        // Récupérer l'identifiant de l'association :
        // 1. Vérifier s'il y a un deep link (URI)
        Uri data = getIntent().getData();
        String inputId = null;
        if (data != null) {
            // On vérifie d'abord le paramètre "qr_code" (qui est le code QR stocké dans la BDD)
            if (data.getQueryParameter("qr_code") != null) {
                inputId = data.getQueryParameter("qr_code");
            } else if (data.getQueryParameter("id") != null) {
                inputId = data.getQueryParameter("id");
            }
        }
        // 2. Si non présent, récupérer l'extra "associationId"
        if (inputId == null || inputId.isEmpty()) {
            inputId = getIntent().getStringExtra("associationId");
        }
        if (inputId == null || inputId.isEmpty()) {
            Toast.makeText(this, "Aucun identifiant d'association fourni", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Appeler le serveur pour charger les détails.
        loadAssociationDetails(inputId);

        // Bouton "Don simple" : il lancera DonationNormalActivity avec les infos de l'association et l'userId
        buttonDonSimple.setOnClickListener(v -> {
            // On utilise les variables associationId et associationName, qui doivent avoir été mises à jour par loadAssociationDetails
            if (associationId == null || associationId.isEmpty()) {
                Toast.makeText(AssociationDetailsActivity.this, "Les détails de l'association ne sont pas encore chargés", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(AssociationDetailsActivity.this, DonationNormalActivity.class);
            intent.putExtra("associationId", associationId);
            intent.putExtra("associationName", associationName);
            // Récupérer l'userId depuis SharedPreferences (si l'utilisateur est connecté)
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String userId = prefs.getString("userId", "");
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        buttonDonRecurrent.setOnClickListener(v ->
                Toast.makeText(AssociationDetailsActivity.this, "Don récurrent (à implémenter)", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadAssociationDetails(String inputId) {
        new Thread(() -> {
            try {
                // Déterminer si inputId est numérique ou s'il s'agit d'un code QR
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
                // Ici, on récupère l'id numérique de l'association (champ idAsso)
                final String idAsso = json.optString("idAsso", "");
                if (idAsso == null || idAsso.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(AssociationDetailsActivity.this, "Association non trouvée", Toast.LENGTH_LONG).show());
                    return;
                }
                // Mettre à jour associationId avec l'id numérique
                associationId = idAsso;
                final String nomAsso = json.optString("nomAsso", "Inconnu");
                associationName = nomAsso; // stocker pour transmettre à DonationNormalActivity
                final String descriptif = json.optString("descriptif", "Aucun descriptif");
                final String mailAsso = json.optString("mailAsso", "Non défini");
                final String telAsso = json.optString("telAsso", "Non défini");
                final String adresseAsso = json.optString("adresseAsso", "Non défini");

                runOnUiThread(() -> {
                    textViewNomAsso.setText("Nom : " + nomAsso);
                    textViewDescriptif.setText("Descriptif : " + descriptif);
                    textViewMail.setText("Mail : " + mailAsso);
                    textViewTel.setText("Téléphone : " + telAsso);
                    textViewAdresse.setText("Adresse : " + adresseAsso);
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
