package iut.dam.projetsaedon.associations;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;
import java.util.List;

import iut.dam.projetsaedon.R;

public class AssociationListActivity extends AppCompatActivity {

    private List<Association> associationsList = new ArrayList<>();
    private SearchView searchView;

    private AssociationListAdapter adapterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_association_list);
        getRemoteAssociation(this);


        searchView = findViewById(R.id.association_list_search_view);

        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        ListView assoListView = findViewById(R.id.association_list_view);

        assoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AssociationListActivity.this, AssociationDetailsActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("associationId", ""+associationsList.get(position).getId());

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void filterList(String text) {
        List<Association> filteredList = new ArrayList<>();
        for (Association association : associationsList) {
            if (association.getNomAsso().toLowerCase().contains(text.toLowerCase())
                    || association.getThemesAsso().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(association);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Pas d'association trouvée", Toast.LENGTH_LONG).show();
        }
        else {
            adapterView.setFilteredList(filteredList);
        }
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
//                            ListView associationListView = findViewById(R.id.association_list_view);
//                            adapterView = new AssociationListAdapter(context, associationsList);
//                            associationListView.setAdapter(adapterView);

                            getRemoteAssociationTheme(context);
                        } else {
                            Log.d(TAG, "! : " + (e == null ? "oops" : e.getMessage()));
                        }
                    }
                });
    }

    public void getRemoteAssociationTheme(Context context) {
        String urlString = "http://donation.out-online.net/donation_app_bdd/getAssosTheme.php";
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

                            for (Association association : associationsList) {
                                association.setThemesAsso(Association.getAssociationThemesFromJsonWithId(response.getResult(), association.getId()));
                            }

                            // Mise à jour de la liste visuelle
                            ListView associationListView = findViewById(R.id.association_list_view);
                            adapterView = new AssociationListAdapter(context, associationsList);
                            associationListView.setAdapter(adapterView);
                        } else {
                            Log.d(TAG, "! : " + (e == null ? "oops" : e.getMessage()));
                        }
                    }
                });
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void setSearchView(SearchView searchView) {
        this.searchView = searchView;
    }
}