package iut.dam.projetsaedon.associations;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import iut.dam.projetsaedon.R;

public class Association {
    private int id;
    private String nomAsso;
    private String descAsso;

    public Association(int id, String nomAsso, String descAsso) {
        this.id = id;
        this.nomAsso = nomAsso;
        this.descAsso = descAsso;
    }

    public int getId() {
        return id;
    }

    public String getNomAsso() {
        return nomAsso;
    }

    public String getDescAsso() {
        return descAsso;
    }

    public static List<Association> getAssociationsFromJson(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Association>>(){}.getType();
        List<Association> appliances = gson.fromJson(json, type);

        return appliances;
    }


}
