package iut.dam.projetsaedon.associations;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;
import java.util.List;

import iut.dam.projetsaedon.R;

public class AssociationListActivity extends AppCompatActivity {

    List<Association> associationsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_association_list);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    public void getRemoteAssociation(Context context) {
        String urlString = "http://donation.out-online.net/donation_app_bdd/getAssosForListAsso.php";
        Ion.with(context)
                .load(urlString)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {
                        if (e == null && response != null) {
                            Log.d(TAG, "Http code: " + response.getHeaders().code());
                            Log.d(TAG, "Result resp appliances: " + response.getResult());

                            associationsList = Association.getAssociationsFromJson(response.getResult());

                            // Mise à jour de la liste visuelle
                            ListView associationListView = findViewById(R.id.association_list_view);
                            associationListView.setAdapter(new AssociationListAdapter(context, associationsList));
                        } else {
                            Log.d(TAG, "! : " + (e == null ? "oops" : e.getMessage()));
                        }
                    }
                });
    }
}