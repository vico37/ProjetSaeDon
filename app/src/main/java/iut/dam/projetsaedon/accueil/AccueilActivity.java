package iut.dam.projetsaedon.accueil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.associations.AssociationListActivity;
import iut.dam.projetsaedon.login.CompteActivity;
import iut.dam.projetsaedon.login.LoginActivity;

/**
 * Activité d'accueil de l'application.
 * Cette activité propose trois actions principales :
 *   Accéder à la liste des associations (btnRechAsso).
 *   Accéder aux informations de son compte (btnMonCompte), accessible uniquement si l'utilisateur est connecté.
 *   Se déconnecter (btnDeconnexion) et revenir à l'écran de connexion.
 * Les informations de session de l'utilisateur sont gérées via les SharedPreferences.
 */
public class AccueilActivity extends AppCompatActivity {

    private Button btnRechAsso, btnMonCompte, btnDeconnexion;
    private SharedPreferences prefs;

    /**
     * Méthode appelée lors de la création de l'activité.
     * Elle initialise les composants de l'interface utilisateur, configure les listeners
     * pour les boutons et récupère les informations de session depuis les SharedPreferences.
     *
     * @param savedInstanceState sauvegarde d'état
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        btnRechAsso = findViewById(R.id.btn_rech_asso);
        btnMonCompte = findViewById(R.id.btn_mon_compte);
        btnDeconnexion = findViewById(R.id.btn_deconnexion);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        btnRechAsso.setOnClickListener(v ->
                startActivity(new Intent(AccueilActivity.this, AssociationListActivity.class)));

        btnMonCompte.setOnClickListener(v -> {
            if (isUserConnected()) {
                startActivity(new Intent(AccueilActivity.this, CompteActivity.class));
            } else {
                Toast.makeText(this, "Vous devez être connecté pour accéder à votre compte.", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeconnexion.setOnClickListener(v -> logoutUser());
    }

    /**
     * Vérifie si un utilisateur est connecté.
     * L'utilisateur est considéré connecté si la clé "userId" dans les SharedPreferences n'est pas vide.
     *
     * @return {@code true} si l'utilisateur est connecté, sinon {@code false}.
     */
    private boolean isUserConnected() {
        return !prefs.getString("userId", "").isEmpty();
    }

    /**
     * Procède à la déconnexion de l'utilisateur.
     * Cette méthode supprime les informations de session stockées dans les SharedPreferences
     * et redirige l'utilisateur vers l'écran de connexion en réinitialisant la pile d'activités.
     * Un message Toast confirme la déconnexion.
     */
    private void logoutUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("userId");
        editor.remove("userRole");
        editor.remove("associationId");
        editor.apply();

        Toast.makeText(this, "Vous avez été déconnecté.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AccueilActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
