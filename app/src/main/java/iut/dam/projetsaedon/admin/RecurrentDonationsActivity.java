package iut.dam.projetsaedon.admin;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import iut.dam.projetsaedon.R;

public class RecurrentDonationsActivity extends AppCompatActivity {

    private ListView listViewRecurring;
    private RecurrentDonationAdapter adapter;
    private ArrayList<RecurrentDonation> donationList;
    private Button buttonBack;
    private static final String LIST_URL = "http://donation.out-online.net/donation_app_bdd/list_recurrent_donations.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurrent_donations);

        listViewRecurring = findViewById(R.id.listViewRecurring);
        buttonBack = findViewById(R.id.buttonBack);
        donationList = new ArrayList<>();
        adapter = new RecurrentDonationAdapter(this, donationList);
        listViewRecurring.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String associationId = prefs.getString("associationId", "0");

        String year = getIntent().getStringExtra("year");

        chargerDonations(associationId);

        // Buton retour
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void chargerDonations(String associationId) {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    String params = "?associationId=" + URLEncoder.encode(associationId, "UTF-8");
                    /*if (year != null && !year.isEmpty()){
                        params += "&year=" + URLEncoder.encode(year, "UTF-8");
                    }*/
                    URL url = new URL(LIST_URL + params);
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

                    String response = sb.toString();
                    Log.d("DEBUG_LIST", "Response: " + response);
                    JSONArray jsonArray = new JSONArray(response);

                    donationList.clear();
                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int idDonRec = obj.optInt("idDonRec");
                        double montant = obj.optDouble("montant");
                        String debutdate = obj.optString("debutdate");
                        String enddate = obj.optString("enddate");
                        boolean actif = obj.optInt("actif") == 1;
                        String frequency = obj.optString("frequency");

                        RecurrentDonation donation = new RecurrentDonation(idDonRec, montant, debutdate, enddate, actif, frequency);
                        donationList.add(donation);
                    }
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(RecurrentDonationsActivity.this, "Erreur lors du chargement des dons récurrents", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
