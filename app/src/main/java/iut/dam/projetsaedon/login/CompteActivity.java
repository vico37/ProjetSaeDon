package iut.dam.projetsaedon.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.admin.RecurrentDonationsActivity;
import iut.dam.projetsaedon.mesdonsrecurents.MyRecurringDonationsActivity;

public class CompteActivity extends AppCompatActivity {
    private static final String URL = "http://donation.out-online.net/donation_app_bdd/getUserInfo.php";
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_moncompte);

        nom = findViewById(R.id.txtNomCompte);
        prenom = findViewById(R.id.txtPrenomCompte);
        email = findViewById(R.id.txtEmailCompte);
        mobile = findViewById(R.id.txtTelCompte);
        adresse = findViewById(R.id.txtAdresseCompte);
        editProfil = findViewById(R.id.btnEditProfil);
        logout = findViewById(R.id.btnLogout);
        donRec = findViewById(R.id.btn_voir_dons);

        // Modifier son profil
        editProfil.setOnClickListener(v -> {
            //Intent intent = new Intent(this, EditProfilActivity.class);
            //startActivity(intent);
        });
        // Voir ses dons récurrents
        donRec.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRecurringDonationsActivity.class);
            startActivity(intent);
        });
        // Se déconnecter
        logout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("userId");
            editor.apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Récupération des informations de l'utilisateur
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        // Vérification de la connexion
        if (userId == null) { // Utilisateur non connecté
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            try {
                String params = "?id="+ URLEncoder.encode(userId, "UTF-8");

                URL url = new URL(URL + params);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                // Lecture
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null){
                    sb.append(line);
                }
                br.close();
                conn.disconnect();
                // Gestion Json
                String reponse = sb.toString();
                JSONObject json = new JSONObject(reponse);
                // Erreur BD
                if(json.has("error")) {
                    runOnUiThread(() -> {
                        Toast.makeText(CompteActivity.this, json.optString("error"), Toast.LENGTH_LONG).show();
                    });
                } else {
                    // Remplir les champs
                    nom.setText(json.optString("nom"));
                    prenom.setText(json.optString("prenom"));
                    email.setText(json.optString("mail"));
                    mobile.setText(json.optString("telUser"));
                    adresse.setText(json.optString("adresseUser"));
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(CompteActivity.this,
                            "Erreur réseau : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
