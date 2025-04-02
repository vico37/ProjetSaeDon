package iut.dam.projetsaedon.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import iut.dam.projetsaedon.R;
import iut.dam.projetsaedon.login.LoginActivity;

/**
 * SplashActivity est l'activité de démarrage qui affiche un écran de chargement pendant une durée fixe
 *
 * Après la durée définie l'activité lance {@link LoginActivity} et se termine
 */
public class SplashActivity extends AppCompatActivity {

    /**
     * Durée d'affichage de l'écran de splash en millisecondes
     */
    private static final int SPLASH_DURATION = 2000; // 2 secondes

    /**
     * Méthode appelée à la création de l'activité.
     *
     * Affiche le layout de l'écran de splash puis lance l'activité de connexion {@link LoginActivity}
     *
     * @param savedInstanceState sauvegarde de l'état
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Démarrage d'un délai avant de lancer LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
