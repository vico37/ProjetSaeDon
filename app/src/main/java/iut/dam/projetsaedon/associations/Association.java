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

    private String themesAsso;
    public Association(int id, String nomAsso, String descAsso) {
        this.id = id;
        this.nomAsso = nomAsso;
        this.descAsso = descAsso;
    }

    public void setThemesAsso(String themesAsso) {
        this.themesAsso = themesAsso;
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

    public String getThemesAsso() {
        return themesAsso;
    }

    public static String getAssociationThemesFromJsonWithId(String json, int id) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<AssoTheme>>(){}.getType();
        List<AssoTheme> assoc = gson.fromJson(json, type);

        String themes = "Theme : ";

        for(AssoTheme assoTheme : assoc) {
            if (assoTheme.getIdAsso() == id) {
                themes += assoTheme.getThemeNom() + " ";
            }
        }

        return themes;
    }

    public static List<Association> getAssociationsFromJson(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Association>>(){}.getType();
        List<Association> associations = gson.fromJson(json, type);

        return associations;
    }


}
