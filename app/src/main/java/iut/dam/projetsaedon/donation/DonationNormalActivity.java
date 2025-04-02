package iut.dam.projetsaedon.donation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.accueil.AccueilActivity;
import iut.dam.projetsaedon.asso.AssociationDetailsActivity;

public class DonationNormalActivity extends AppCompatActivity {

    private TextView textViewAssociationName;
    private EditText editTextMontant;
    private Button buttonFaireDon, buttonAnnuler;

    // URL du script PHP pour insérer le don
    private static final String DONATION_URL = "http://donation.out-online.net/donation_app_bdd/donation_insert.php";

    private String associationId, associationName, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_normal);

        textViewAssociationName = findViewById(R.id.textViewAssociationName);
        editTextMontant = findViewById(R.id.editTextMontant);
        buttonFaireDon = findViewById(R.id.buttonFaireDon);
        buttonAnnuler = findViewById(R.id.buttonAnnuler);

        // Récupérer les extras transmis par AssociationDetailsActivity
        Intent intent = getIntent();
        associationId = intent.getStringExtra("associationId");
        associationName = intent.getStringExtra("associationName");

        if (associationId == null || associationId.isEmpty()) {
            Toast.makeText(this, "Aucun identifiant d'association fourni", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textViewAssociationName.setText("Donation pour " + associationName);

        // Récupérer l'ID utilisateur depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");

        buttonFaireDon.setOnClickListener(v -> {
            String montantStr = editTextMontant.getText().toString().trim();
            if (montantStr.isEmpty()) {
                Toast.makeText(DonationNormalActivity.this, "Veuillez saisir un montant", Toast.LENGTH_SHORT).show();
                return;
            }
            sendDonation(montantStr);
        });

        buttonAnnuler.setOnClickListener(v -> finish());
    }

    private void sendDonation(String montantStr) {
        new Thread(() -> {
            try {
                String params = "associationId=" + URLEncoder.encode(associationId, "UTF-8")
                        + "&montant=" + URLEncoder.encode(montantStr, "UTF-8")
                        + "&userId=" + URLEncoder.encode(userId, "UTF-8");
                URL url = new URL(DONATION_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes());
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                JSONObject response = new JSONObject(sb.toString());
                boolean success = response.optBoolean("success");
                String message = response.optString("message");

                runOnUiThread(() -> {
                    if (success) {
                        // Afficher la boîte de dialogue de confirmation
                        new AlertDialog.Builder(DonationNormalActivity.this)
                                .setTitle("Confirmation")
                                .setMessage("Votre don a bien été effectué.\nVous allez être redirigé à l'accueil")
                                .setPositiveButton("Confirmer", (dialog, which) -> {
                                    // Retour à la page AssociationDetailsActivity
                                    Intent intent = new Intent(DonationNormalActivity.this, AccueilActivity.class);
                                    intent.putExtra("associationId", associationId);
                                    intent.putExtra("associationName", associationName);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    } else {
                        Toast.makeText(DonationNormalActivity.this, "Erreur lors du don: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(DonationNormalActivity.this, "Erreur lors du don", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
