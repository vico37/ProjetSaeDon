package iut.dam.projetsaedon.register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;

import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.login.LoginActivity;

/**
 *  RegisterActivity : permet à un utilisateur de s'inscrire via le script register.php
 *  en envoyant les données par une requête GET
 *  URL: http://donation.out-online.net/donation_app_bdd/register.php
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editPrenom, editNom, editEmail, editMobile, editAdresse, editPassword;
    private Button buttonValiderInscription;
    private ImageView imageRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editPrenom  = findViewById(R.id.editPrenom);
        editNom     = findViewById(R.id.editNom);
        editEmail   = findViewById(R.id.editEmail);
        editMobile  = findViewById(R.id.editMobile);
        editAdresse = findViewById(R.id.editAdresse);
        editPassword= findViewById(R.id.editPassword);
        buttonValiderInscription = findViewById(R.id.buttonValiderInscription);
        imageRetour = findViewById(R.id.imageRetour);

        // Gestion retour
        if(imageRetour != null) {
            imageRetour.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            });
        }

        // Geston de l'inscription via le bouton
        buttonValiderInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prenom   = editPrenom.getText().toString().trim();
                String nom      = editNom.getText().toString().trim();
                String email    = editEmail.getText().toString().trim();
                String mobile   = editMobile.getText().toString().trim();
                String adresse  = editAdresse.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()
                        || mobile.isEmpty() || adresse.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "Veuillez remplir tous les champs.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                inscrireUtilisateur(prenom, nom, email, mobile, adresse, password);
            }
        });
    }

    /**
     * Méthode pour envoyer les infos au script register.php via GET
     */
    private void inscrireUtilisateur(String prenom, String nom, String mail, String tel, String adr, String mdp) {
        new Thread(() -> {
            try {
                String baseUrl = "http://donation.out-online.net/donation_app_bdd/register.php";

                String params = "?mail=" + URLEncoder.encode(mail, "UTF-8")
                        + "&mdp=" + URLEncoder.encode(mdp, "UTF-8")
                        + "&nom=" + URLEncoder.encode(nom, "UTF-8")
                        + "&prenom=" + URLEncoder.encode(prenom, "UTF-8")
                        + "&tel=" + URLEncoder.encode(tel, "UTF-8")
                        + "&adr=" + URLEncoder.encode(adr, "UTF-8")
                        + "&isa=" + URLEncoder.encode("doneur", "UTF-8")
                        + "&asso_id=";  // vide

                URL url = new URL(baseUrl + params);

                // Connexion
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Lecture reponse
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder reponseBuilder = new StringBuilder();
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    reponseBuilder.append(ligne);
                }
                reader.close();
                conn.disconnect();

                // Conversion Json
                String reponseJSON = reponseBuilder.toString();
                JSONObject json = new JSONObject(reponseJSON);

                // Traitement du Json
                if (json.has("success")) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,
                                json.optString("success"),
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
                } else if (json.has("error")) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this,
                                json.optString("error"),
                                Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this,
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
