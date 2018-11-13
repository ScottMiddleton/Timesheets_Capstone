package com.example.scott.timesheets_capstone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import java.util.Objects;

import static com.example.scott.timesheets_capstone.ContractListActivity.FIREBASE_CHILD_USERS;

public class SignInActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    private FirebaseDatabase mFirebaseDatabase;

    private final String FIREBASE_CHILD_USER_INFO = "userInfo";

    private Context mContext;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final String TAG = "SignInActivity";

    private EditText mEmailEdit;
    private EditText mPasswordEdit;
    private String email;
    private String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mContext = this;
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mEmailEdit = findViewById(R.id.email_edit_text);
        mPasswordEdit = findViewById(R.id.password_edit_text);
        email = mEmailEdit.getText().toString();
        password = mPasswordEdit.getText().toString();

        GoogleSignInButton signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.google_sign_in_button:
                        signIn();
                        break;
                    // ...
                }
            }
        });

        Button emailSignInButton = findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startCreateUserWithEmailActivity = new Intent(mContext, CreateUserWithEmailActivity.class);
                startActivity(startCreateUserWithEmailActivity);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            addUserInfoToDatabase(user);
                            Intent startContractListActivity = new Intent(mContext, ContractListActivity.class);
                            startActivity(startContractListActivity);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(mContext, mContext.getString(R.string.sign_in_failed), Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (resultCode == Activity.RESULT_OK) {
                firebaseAuthWithGoogle(Objects.requireNonNull(task.getResult()));
            }
        }
    }

    private void addUserInfoToDatabase(FirebaseUser user) {
        com.example.scott.timesheets_capstone.model.UserInfo mUserInfo =
                new com.example.scott.timesheets_capstone.model.UserInfo(user.getEmail(), user.getDisplayName(), null);
        DatabaseReference mUsersDatabaseRef = mFirebaseDatabase.getReference().child(FIREBASE_CHILD_USERS).child(user.getUid());
        mUsersDatabaseRef.child(FIREBASE_CHILD_USER_INFO).setValue(mUserInfo);
    }

    public void signInWithEmailAndPassword(View view) {
        if (fieldsAreEmpty()) {
            Toast.makeText(mContext, mContext.getString(R.string.enter_details), Toast.LENGTH_LONG).show();
        } else {
            email = mEmailEdit.getText().toString();
            password = mPasswordEdit.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                Intent startContractListActivity = new Intent(mContext, ContractListActivity.class);
                                startActivity(startContractListActivity);
                                startActivity(startContractListActivity);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(mContext, mContext.getString(R.string.sign_in_failed),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent startContractsListActivty = new Intent(mContext, ContractListActivity.class);
            startActivity(startContractsListActivty);
        }
    }

    private Boolean fieldsAreEmpty() {
        return mEmailEdit.getText().toString().matches("") ||
                mPasswordEdit.getText().toString().matches("");
    }

}
