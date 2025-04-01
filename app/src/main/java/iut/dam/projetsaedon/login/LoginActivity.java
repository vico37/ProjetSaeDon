package iut.dam.projetsaedon.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import iut.dam.projetsaedon.AdminActivity;
import iut.dam.projetsaedon.MainActivity;
import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.register.RegisterActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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

        // Gestion click sur se connecter
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

        // Gestion click sur connexion en tant qu'invité
        buttonConnexionInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this,
                        "Connexion en tant qu'invité (à implémenter)",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Gestion click sur s'inscrire
        buttonInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Gestion click sur mot de passe oublie
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this,
                        "Aller vers la récupération de mot de passe (à implémenter)",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Méthode pour vérifier le mail et le mdp avec le script login.php
     */
    private void verifierIdentifiants(String mail, String mdp) {
        new Thread(() -> {
            try {
                String urlParams = "?mail=" + URLEncoder.encode(mail, "UTF-8") + "&mdp=" + URLEncoder.encode(mdp, "UTF-8");

                URL url = new URL(LOGIN_URL + urlParams);
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

                if(json.has("error")) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, json.optString("error"), Toast.LENGTH_LONG).show();
                    });
                } else {
                    String userId   = json.optString("idUser");
                    String userRole = json.optString("isAdmin"); //admin ou doneur

                    // Stocker des fonctionalite plus loin
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userId", userId);
                    editor.putString("userRole", userRole);
                    editor.apply();

                    // Redirection en fonction du role
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this,
                                "Votre ID = " + userId + ", Votre Role = " + userRole,
                                Toast.LENGTH_LONG).show();

                        if("admin".equalsIgnoreCase(userRole)) {
                            // Redirection adminstration
                            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Redirection utilisateur basique
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Erreur réseau : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
