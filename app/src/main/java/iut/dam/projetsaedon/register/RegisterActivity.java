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
 * RegisterActivity permet à un utilisateur de s'inscrire via le script register.php
 * Les données saisies (prénom, nom, email, mobile, adresse, mot de passe) sont envoyées
 * par requête GET au script PHP
 * Si l'inscription réussit, l'utilisateur est redirigé vers LoginActivity. Sinon, un message
 * d'erreur est affiché.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editPrenom, editNom, editEmail, editMobile, editAdresse, editPassword;
    private Button buttonValiderInscription;
    private ImageView imageRetour;

    /**
     * Méthode appelée lors de la création de l'activité.
     * Elle lie les vues définies dans le layout et configure les actions des boutons
     * Notamment, le bouton de retour qui permet de revenir à LoginActivity
     * et le bouton de validation envoie les informations saisies à la méthode inscrireUtilisateur()
     *
     * @param savedInstanceState sauvgarde d'état
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Liaison des vues
        editPrenom  = findViewById(R.id.editPrenom);
        editNom     = findViewById(R.id.editNom);
        editEmail   = findViewById(R.id.editEmail);
        editMobile  = findViewById(R.id.editMobile);
        editAdresse = findViewById(R.id.editAdresse);
        editPassword= findViewById(R.id.editPassword);
        buttonValiderInscription = findViewById(R.id.buttonValiderInscription);
        imageRetour = findViewById(R.id.imageRetour);

        // Gestion du bouton de retour
        if (imageRetour != null) {
            imageRetour.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            });
        }

        // Gestion du clic sur le bouton de validation de l'inscription
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
     * Envoie les informations d'inscription au script register.php via une requête GET.
     * Les paramètres envoyés sont : mail, mdp, nom, prenom, tel, adr
     * En cas de succès, un message est affiché et l'utilisateur est redirigé vers LoginActivity.
     * En cas d'erreur, le message d'erreur retourné par le serveur est affiché.
     *
     * @param prenom Le prénom de l'utilisateur.
     * @param nom Le nom de l'utilisateur.
     * @param mail L'email de l'utilisateur.
     * @param tel Le numéro de téléphone de l'utilisateur.
     * @param adr L'adresse de l'utilisateur.
     * @param mdp Le mot de passe de l'utilisateur.
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

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder reponseBuilder = new StringBuilder();
                String ligne;
                while ((ligne = reader.readLine()) != null) {
                    reponseBuilder.append(ligne);
                }
                reader.close();
                conn.disconnect();

                String reponseJSON = reponseBuilder.toString();
                JSONObject json = new JSONObject(reponseJSON);

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
