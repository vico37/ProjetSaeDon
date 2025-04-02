package iut.dam.projetsaedon.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.admin.RecurrentDonationsActivity;
import iut.dam.projetsaedon.mesdonsrecurents.MyRecurringDonationsActivity;
import iut.dam.projetsaedon.login.LoginActivity;

public class CompteActivity extends AppCompatActivity {

    private static final String API_URL = "http://donation.out-online.net/donation_app_bdd/getUserInfo.php";

    private Button editProfil;
    private Button logout;
    private Button donRec;
    private TextView nom;
    private TextView prenom;
    private TextView email;
    private TextView mobile;
    private TextView adresse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moncompte);

        // Liaison des vues
        nom = findViewById(R.id.txtNomCompte);
        prenom = findViewById(R.id.txtPrenomCompte);
        email = findViewById(R.id.txtEmailCompte);
        mobile = findViewById(R.id.txtTelCompte);
        adresse = findViewById(R.id.txtAdresseCompte);
        editProfil = findViewById(R.id.btnEditProfil);
        logout = findViewById(R.id.btnLogout);
        donRec = findViewById(R.id.btn_voir_dons);

        // Bouton Modifier Profil (fonctionnalité à implémenter)
        editProfil.setOnClickListener(v -> {
            // Par exemple, ouvrir EditProfilActivity
            // Intent intent = new Intent(this, EditProfilActivity.class);
            // startActivity(intent);
        });

        // Bouton Voir Dons récurrents
        donRec.setOnClickListener(v -> {
            Intent intent = new Intent(CompteActivity.this, MyRecurringDonationsActivity.class);
            startActivity(intent);
        });

        // Bouton Déconnexion
        logout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("userId");
            editor.remove("userRole");
            editor.remove("associationId");
            editor.apply();
            Intent intent = new Intent(CompteActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Récupération de l'id utilisateur depuis SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            // Si non connecté, rediriger vers LoginActivity
            Intent intent = new Intent(CompteActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Lancer l'opération réseau dans un thread séparé
            new Thread(() -> {
                try {
                    // Construire l'URL avec l'id utilisateur
                    String params = "?id=" + URLEncoder.encode(userId, "UTF-8");
                    URL url = new URL(API_URL + params);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    // Lecture de la réponse
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    String response = sb.toString();
                    JSONObject json = new JSONObject(response);

                    // Si le JSON contient une erreur
                    if (json.has("error")) {
                        runOnUiThread(() ->
                                Toast.makeText(CompteActivity.this, json.optString("error"), Toast.LENGTH_LONG).show()
                        );
                    } else {
                        // Mise à jour des vues sur le thread principal
                        runOnUiThread(() -> {
                            nom.setText(json.optString("nom"));
                            prenom.setText(json.optString("prenom"));
                            email.setText(json.optString("mail"));
                            mobile.setText(json.optString("telUser"));
                            adresse.setText(json.optString("adresseUser"));
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(CompteActivity.this, "Erreur réseau : " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }).start();
        }
    }
}
