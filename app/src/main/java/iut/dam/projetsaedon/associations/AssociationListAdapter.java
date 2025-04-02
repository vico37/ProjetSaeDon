package iut.dam.projetsaedon.associations;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.List;

import iut.dam.projetsaedon.R;

public class AssociationListAdapter extends BaseAdapter {
    private List<Association> associationsList;
    private final LayoutInflater inflater;

    public AssociationListAdapter (Context context, List<Association> associationsList) {
        this.associationsList = associationsList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return associationsList.size();
    }

    @Override
    public Object getItem(int position) {
        return associationsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setFilteredList(List<Association> filteredList) {
        this.associationsList = filteredList;
        notifyDataSetChanged();
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.activity_association_list_adapter, null);

        Association currentAssociation = (Association) getItem(position);
        String associationNom = currentAssociation.getNomAsso();
        String associationDesc = currentAssociation.getDescAsso();

        TextView associationNomTextView = convertView.findViewById(R.id.association_nom);
        associationNomTextView.setText(associationNom);

        TextView associationDescTextView = convertView.findViewById(R.id.association_desc);
        associationDescTextView.setText(associationDesc);

        return convertView;
    }

    public List<Association> getAssociationsList() {
        return associationsList;
    }


}
