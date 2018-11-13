package com.example.scott.timesheets_capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateUserWithEmailActivity extends AppCompatActivity {

    private final String TAG = "TAG";

    @BindView(R.id.create_user_button) CardView mCreateUserButton;
    @BindView(R.id.email_edit) EditText emailEdit;
    @BindView(R.id.password_edit) EditText passwordEdit;
    @BindView(R.id.first_name_edit) EditText nameEdit;
    @BindView(R.id.sign_in_error_reason) TextView mSignInErrorTv;

    private String email;
    private String password;
    private String name;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseRef;

    private Context mContext;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_with_email);

        mContext = this;

        ButterKnife.bind(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mCreateUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(mContext, "Please complete all the fields", Toast.LENGTH_LONG).show();
                } else {
                    email = emailEdit.getText().toString();
                    password = passwordEdit.getText().toString();
                    name = nameEdit.getText().toString();
                    createUser(email, password);
                }
            }
        });
    }

    private Boolean fieldsAreEmpty() {
        if (nameEdit.getText().toString().matches("") ||
                emailEdit.getText().toString().matches("") ||
                passwordEdit.getText().toString().matches("")) {
            return true;
        } else {
            return false;
        }
    }

    private void createUser (String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            com.example.scott.timesheets_capstone.model.UserInfo mUserInfo =
                                    new com.example.scott.timesheets_capstone.model.UserInfo(user.getEmail(), name, null);
                            mUsersDatabaseRef = mFirebaseDatabase.getReference().child("users").child(user.getUid());
                            mUsersDatabaseRef.child("userInfo").setValue(mUserInfo);
                            Intent startContractListActivity = new Intent(mContext, ContractListActivity.class);
                            startActivity(startContractListActivity);
                        } else {
                            // If sign in fails, display a message to the user.
                            mSignInErrorTv.setText(Objects.requireNonNull(task.getException()).getMessage());
                            Log.e(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //This method is called on clicking the "sign in" text
    public void launchSignInActivity (View v){
        finish();
    }

}

