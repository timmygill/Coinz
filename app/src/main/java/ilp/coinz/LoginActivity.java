package ilp.coinz;

import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;


        import android.content.Intent;
        import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.support.v7.app.AppCompatActivity;
        import android.text.TextUtils;
import android.util.Log;
import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ProgressBar;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private String tag = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.sign_in_button);
        btnSignUp = findViewById(R.id.sign_up_button);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);



        btnSignIn.setOnClickListener(v -> {

            String email = Objects.requireNonNull(inputEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(inputPassword.getText()).toString().trim();

            if (TextUtils.isEmpty(email)) {
                Snackbar.make(findViewById(R.id.loginLayout), "Enter email!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Snackbar.make(findViewById(R.id.loginLayout), "Enter password!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(tag, "Sign in: success");
                            FirebaseUser user = auth.getCurrentUser();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        } else {
                            Log.d(tag, "Sign in: fail");
                            Snackbar.make(findViewById(R.id.loginLayout), "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        });


        btnSignUp.setOnClickListener(v -> {

            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Snackbar.make(findViewById(R.id.loginLayout), "Enter email!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Snackbar.make(findViewById(R.id.loginLayout), "Enter password!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Snackbar.make(findViewById(R.id.loginLayout), "Password too short, enter minimum 6 characters!", Snackbar.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            //create user
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Snackbar.make(findViewById(R.id.loginLayout), "Authentication failed.", Snackbar.LENGTH_SHORT).show();
                        } else {

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            Player player = new Player(email,0, "", 1,0, 1, 25, 0, 0, 0);

                            db.collection("user").document(email).collection("Player").document(email).set(player);

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    });

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}