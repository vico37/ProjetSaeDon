package iut.dam.projetsaedon.asso;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
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

public class AssociationDetailsActivity extends AppCompatActivity {

    private TextView textViewNomAsso, textViewDescriptif, textViewMail, textViewTel, textViewAdresse;
    private Button buttonDonSimple, buttonDonRecurrent;
    private static final String ASSO_DETAILS_URL = "http://donation.out-online.net/donation_app_bdd/get_asso_details.php";

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

        String associationId = null;
        Uri data = getIntent().getData();
        if (data != null) {
            if (data.getQueryParameter("qr_code") != null) {
                associationId = data.getQueryParameter("qr_code");
            } else if (data.getQueryParameter("id") != null) {
                associationId = data.getQueryParameter("id");
            }
        }
        if (associationId == null || associationId.isEmpty()) {
            associationId = getIntent().getStringExtra("associationId");
        }

        if (associationId == null || associationId.isEmpty()) {
            Toast.makeText(this, "Aucun identifiant d'association fourni", Toast.LENGTH_LONG).show();
            finish();
        } else {
            loadAssociationDetails(associationId);
        }

        buttonDonSimple.setOnClickListener(v ->
                Toast.makeText(AssociationDetailsActivity.this, "Don simple (à implémenter)", Toast.LENGTH_SHORT).show()
        );
        buttonDonRecurrent.setOnClickListener(v ->
                Toast.makeText(AssociationDetailsActivity.this, "Don récurrent (à implémenter)", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadAssociationDetails(String associationId) {
        new Thread(() -> {
            try {
                String paramKey;
                try {
                    Integer.parseInt(associationId);
                    paramKey = "associationId";
                } catch (NumberFormatException e) {
                    paramKey = "qr_code";
                }
                String params = "?" + paramKey + "=" + URLEncoder.encode(associationId, "UTF-8");
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
                final String nomAsso = json.optString("nomAsso", "Inconnu");
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
