package iut.dam.projetsaedon.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import iut.dam.projetsaedon.R;

/**
 * ForgotPasswordActivity permet à l'utilisateur de saisir son email et de définir un nouveau mot de passe.
 *
 * Cette activité simule l'envoi d'un email de réinitialisation.
 * Lorsqu'un utilisateur valide le formulaire (avec email, nouveau mot de passe et confirmation identiques),
 * un message de confirmation est affiché. Un bouton "Retour" permet de fermer l'activité
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private Button buttonValide;
    private Button buttonRetour;

    /**
     * Méthode onCreate qui initialise l'activité
     * Configure également les actions des boutons pour valider le formulaire ou revenir en arrière
     *
     * @param savedInstanceState sauvegarde d'état
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        buttonValide = findViewById(R.id.buttonValide);
        buttonRetour = findViewById(R.id.buttonRetour);

        // Gestion du clic sur le bouton de validation
        buttonValide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String newPassword = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextPasswordConfirm.getText().toString().trim();

                if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Veuillez remplir tous les champs.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Les mots de passe ne correspondent pas.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(ForgotPasswordActivity.this,
                        "Un email de réinitialisation a été envoyé à " + email,
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
