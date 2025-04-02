package iut.dam.projetsaedon.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.accueil.AccueilActivity;
import iut.dam.projetsaedon.admin.AdminTotalDonationsActivity;
import iut.dam.projetsaedon.mesdonsrecurents.MyRecurringDonationsActivity;
import iut.dam.projetsaedon.register.RegisterActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * LoginActivity permet à l'utilisateur de se connecter à l'application
 * Cette activité offre plusieurs options :
 *   Connexion classique via email et mot de passe
 *   Connexion en tant qu'invité
 *   Inscription (redirection vers RegisterActivity)
 *   Récupération du mot de passe oublié (redirection vers ForgotPasswordActivity)
 * Après la validation des identifiants via le PHP on stock
 * userId, userRole et associationId dans les SharedPreferences en fonction du rôle,de l'utilisateur
 * on redirigé vers AdminTotalDonationsActivity ou vers AccueilActivity
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSeConnecter;
    private Button buttonConnexionInvite;
    private Button buttonInscription;
    private TextView textViewForgotPassword;
    private static final String LOGIN_URL = "http://donation.out-online.net/donation_app_bdd/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSeConnecter = findViewById(R.id.buttonSeConnecter);
        buttonConnexionInvite = findViewById(R.id.buttonConnexionInvite);
        buttonInscription = findViewById(R.id.buttonInscription);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Gestion du clic sur Se connecter
        buttonSeConnecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email    = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                            "Veuillez saisir un email et un mot de passe.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    verifierIdentifiants(email, password);
                }
            }
        });

        // Gestion du clic sur Connexion en tant qu'invité
        buttonConnexionInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, AccueilActivity.class));
            }
        });

        // Gestion du clic sur S'inscrire
        buttonInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Gestion du clic sur Mot de passe oublié
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    /**
     * Vérifie les identifiants de l'utilisateur en utilisant login.php
     *
     * Si on a une erreur on l'affiche
     * Sinon, idUser, isAdmin, association_id sont enregistrées dans les SharedPreferences
     * puis on redirige vers l'activité correspondante en fonction du role
     *
     * @param mail L'adresse email saisie par l'utilisateur.
     * @param mdp  Le mot de passe saisi par l'utilisateur.
     */
    private void verifierIdentifiants(String mail, String mdp) {
        new Thread(() -> {
            try {
                // Creation url pour le php
                String urlParams = "?mail=" + URLEncoder.encode(mail, "UTF-8")
                        + "&mdp=" + URLEncoder.encode(mdp, "UTF-8");

                URL url = new URL(LOGIN_URL + urlParams);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // Lecture réponse
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                conn.disconnect();

                // Traitement du JSON
                String reponse = sb.toString();
                JSONObject json = new JSONObject(reponse);

                if(json.has("error")) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, json.optString("error"), Toast.LENGTH_LONG).show();
                    });
                } else {
                    String userId = json.optString("idUser");
                    String userRole = json.optString("isAdmin"); // "admin" ou "doneur"
                    String associationId = json.optString("association_id");

                    // Stocker dans le SharedPreference
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userId", userId);
                    editor.putString("userRole", userRole);
                    editor.putString("associationId", associationId);
                    editor.apply();

                    // Redirection en fonction du rrole
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this,
                                "Votre ID = " + userId + ", Votre Role = " + userRole,
                                Toast.LENGTH_LONG).show();

                        if("admin".equalsIgnoreCase(userRole)) {
                            // Redirection vers l'administration
                            Intent intent = new Intent(LoginActivity.this, AdminTotalDonationsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Redirection vers l'accueil
                            Intent intent = new Intent(LoginActivity.this, AccueilActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Erreur réseau : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
