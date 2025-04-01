package iut.dam.projetsaedon.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class AdminActivity extends AppCompatActivity {

    private TextView textViewYear;
    private TextView textViewOneTime;
    private TextView textViewRecurring;
    private Button buttonRefresh;

    // URL du script PHP de statistiques
    private static final String STATS_URL = "http://donation.out-online.net/donation_app_bdd/admin_stats.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        textViewYear = findViewById(R.id.textViewYear);
        textViewOneTime = findViewById(R.id.textViewOneTime);
        textViewRecurring = findViewById(R.id.textViewRecurring);
        buttonRefresh = findViewById(R.id.buttonRefresh);

        buttonRefresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                chargerStats();
            }
        });

        // Charger les statistiques à l'ouverture
        chargerStats();
    }

    private void chargerStats(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    // On utilise l'année en cours par défaut
                    int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                    String params = "?year=" + URLEncoder.encode(String.valueOf(currentYear), "UTF-8");
                    URL url = new URL(STATS_URL + params);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        sb.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    String reponse = sb.toString();
                    Log.d("DEBUG_ADMIN", "Response: " + reponse);
                    JSONObject json = new JSONObject(reponse);

                    final int year = json.optInt("year", currentYear);
                    final double totalOneTime = json.optDouble("totalOneTime", 0);
                    final double totalRecurring = json.optDouble("totalRecurring", 0);

                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            textViewYear.setText("Année : " + year);
                            textViewOneTime.setText("Total des dons uniques : " + totalOneTime + " €");
                            textViewRecurring.setText("Total des dons récurrents : " + totalRecurring + " €");
                        }
                    });
                } catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(AdminActivity.this, "Erreur lors du chargement des statistiques", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
