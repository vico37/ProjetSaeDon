package iut.dam.projetsaedon.admin;

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
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.login.LoginActivity;

/**
 * Activité permettant à l'administrateur de visualiser le total des dons d'une association.
 * Cette activité récupère et affiche les totaux des dons uniques, des dons récurrents ainsi que le total général
 * pour une année donnée. Les données sont récupérées via une requête HTTP GET vers le serveur.
 */
public class AdminTotalDonationsActivity extends AppCompatActivity {

    private EditText editTextYear;
    private Button buttonRefresh, buttonViewRecurring, buttonLogout;
    private TextView textViewAssociationName, textViewTotalUnique, textViewTotalRecurring, textViewTotalOverall;
    private static final String TOTAL_URL = "http://donation.out-online.net/donation_app_bdd/admin_total_donations.php";
    private String associationId;

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les composants de l'interface utilisateur, récupère les données via les SharedPreferences
     * et définit les actions sur les boutons (rafraîchir, visualiser les dons récurrents et déconnexion).
     *
     * @param savedInstanceState Bundle contenant l'état précédemment sauvegardé de l'activité, ou null si c'est une création initiale.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_total_donations);

        editTextYear = findViewById(R.id.editTextYear);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        buttonViewRecurring = findViewById(R.id.buttonViewRecurring);
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewAssociationName = findViewById(R.id.textViewAssociationName);
        textViewTotalUnique = findViewById(R.id.textViewTotalUnique);
        textViewTotalRecurring = findViewById(R.id.textViewTotalRecurring);
        textViewTotalOverall = findViewById(R.id.textViewTotalOverall);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        associationId = prefs.getString("associationId", "0");

        // Bouton permettant de rafraîchir les totaux
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chargerTotaux();
            }
        });

        // Bouton permettant de visualiser les dons récurrents
        buttonViewRecurring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminTotalDonationsActivity.this, RecurrentDonationsActivity.class);
                intent.putExtra("year", editTextYear.getText().toString().trim());
                intent.putExtra("associationId", associationId);
                startActivity(intent);
            }
        });

        // Bouton de déconnexion
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Suppression des données de session
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("userId");
                editor.remove("userRole");
                editor.remove("associationId");
                editor.apply();

                Intent intent = new Intent(AdminTotalDonationsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Chargement initial des totaux dès l'ouverture de l'activité
        chargerTotaux();
    }

    /**
     * Charge les totaux des dons à partir du serveur via une requête HTTP GET.
     * La méthode récupère l'année spécifiée dans l'interface (ou l'année courante si aucun champ n'est renseigné)
     * et envoie une requête avec l'identifiant de l'association. La réponse JSON est ensuite traitée pour extraire
     * le nom de l'association et les totaux des dons uniques, récurrents et le total général. L'interface utilisateur
     * est mise à jour sur le thread principal.
     * En cas d'erreur (par exemple, problème de connexion ou de parsing), un message Toast est affiché.
     */
    private void chargerTotaux() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String yearStr = editTextYear.getText().toString().trim();
                    if (yearStr.isEmpty()) {
                        yearStr = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR));
                    }
                    String params = "?year=" + URLEncoder.encode(yearStr, "UTF-8")
                            + "&associationId=" + URLEncoder.encode(associationId, "UTF-8");
                    URL url = new URL(TOTAL_URL + params);
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

                    String response = sb.toString();
                    Log.d("DEBUG_TOTAL", "Response: " + response);
                    JSONObject json = new JSONObject(response);

                    final int year = json.optInt("year", Integer.parseInt(yearStr));
                    final double totalUnique = json.optDouble("totalUnique", 0);
                    final double totalRecurring = json.optDouble("totalRecurring", 0);
                    final double totalOverall = json.optDouble("totalOverall", 0);
                    final String associationName = json.optString("associationName", "Inconnue");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewAssociationName.setText("Association : " + associationName);
                            textViewTotalUnique.setText("Total dons uniques : " + totalUnique + " €");
                            textViewTotalRecurring.setText("Total dons récurrents : " + totalRecurring + " €");
                            textViewTotalOverall.setText("Total général : " + totalOverall + " €");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AdminTotalDonationsActivity.this, "Erreur lors du chargement des totaux", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
