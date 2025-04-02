package iut.dam.projetsaedon.donation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import iut.dam.projetsaedon.MainActivity;
import iut.dam.projetsaedon.accueil.AccueilActivity;

public class RecurringDonationActivity extends AppCompatActivity {

    private TextView textViewAssociationName;
    private EditText editTextMontant;
    private RadioGroup radioGroupFrequency;
    private Button buttonValider, buttonAnnuler;

    // URL du script PHP pour insérer le don récurrent
    private static final String RECURRING_DONATION_URL = "http://donation.out-online.net/donation_app_bdd/recurring_donation_insert.php";

    // Données reçues
    private String associationId, associationName, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_donation);

        textViewAssociationName = findViewById(R.id.textViewAssociationName);
        editTextMontant = findViewById(R.id.editTextMontantRecurring);
        radioGroupFrequency = findViewById(R.id.radioGroupFrequency);
        buttonValider = findViewById(R.id.buttonValiderRecurring);
        buttonAnnuler = findViewById(R.id.buttonAnnulerRecurring);

        // Récupérer les extras de l'Intent
        Intent intent = getIntent();
        associationId = intent.getStringExtra("associationId");
        associationName = intent.getStringExtra("associationName");
        if (associationId == null || associationId.isEmpty()) {
            Toast.makeText(this, "Aucun identifiant d'association fourni", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        textViewAssociationName.setText("Donation récurrente pour " + associationName);

        // Vérifier que l'utilisateur est connecté (userId stocké dans SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "Vous devez être connecté pour faire un don récurrent", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        buttonValider.setOnClickListener(v -> {
            String montantStr = editTextMontant.getText().toString().trim();
            if (montantStr.isEmpty()) {
                Toast.makeText(RecurringDonationActivity.this, "Veuillez saisir un montant", Toast.LENGTH_SHORT).show();
                return;
            }
            double montant = Double.parseDouble(montantStr);
            // Récupérer la fréquence sélectionnée
            String frequency = "mensuel"; // valeur par défaut
            int selectedId = radioGroupFrequency.getCheckedRadioButtonId();
            RadioButton selectedRadio = findViewById(selectedId);
            if (selectedRadio != null) {
                frequency = selectedRadio.getText().toString().toLowerCase(); // attend "mensuel" ou "annuel"
            }
            // Pour cet exemple, on prend le jour courant comme date de début
            String debutDate = java.time.LocalDate.now().toString();

            sendRecurringDonation(montant, frequency, debutDate);
        });

        buttonAnnuler.setOnClickListener(v -> finish());
    }

    private void sendRecurringDonation(double montant, String frequency, String debutDate) {
        new Thread(() -> {
            try {
                String params = "associationId=" + URLEncoder.encode(associationId, "UTF-8")
                        + "&montant=" + URLEncoder.encode(String.valueOf(montant), "UTF-8")
                        + "&userId=" + URLEncoder.encode(userId, "UTF-8")
                        + "&frequency=" + URLEncoder.encode(frequency, "UTF-8")
                        + "&debutdate=" + URLEncoder.encode(debutDate, "UTF-8");
                URL url = new URL(RECURRING_DONATION_URL);
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
                        new AlertDialog.Builder(RecurringDonationActivity.this)
                                .setTitle("Confirmation")
                                .setMessage("Votre don récurrent a bien été effectué.\nVous allez être redirigé vers la page d'accueil.")
                                .setPositiveButton("OK", (dialog, which) -> {
                                    Intent intent = new Intent(RecurringDonationActivity.this, AccueilActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(RecurringDonationActivity.this, "Erreur : " + message, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(RecurringDonationActivity.this, "Erreur lors de l'envoi du don", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
