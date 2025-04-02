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

public class AdminTotalDonationsActivity extends AppCompatActivity {

    private EditText editTextYear;
    private Button buttonRefresh, buttonViewRecurring, buttonLogout;
    private TextView textViewAssociationName, textViewTotalUnique, textViewTotalRecurring, textViewTotalOverall;
    private static final String TOTAL_URL = "http://donation.out-online.net/donation_app_bdd/admin_total_donations.php";
    private String associationId;

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

        // On recupere les données stocké au login
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        associationId = prefs.getString("associationId", "0");

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chargerTotaux();
            }
        });

        buttonViewRecurring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminTotalDonationsActivity.this, RecurrentDonationsActivity.class);
                intent.putExtra("year", editTextYear.getText().toString().trim());
                intent.putExtra("associationId", associationId);
                startActivity(intent);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        chargerTotaux();
    }

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
