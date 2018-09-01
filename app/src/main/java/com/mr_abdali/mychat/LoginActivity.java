package com.mr_abdali.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    // TODO: 8/6/2018 Variables section....
    private static final String TAG = "LoginActivity";
    public static final String EXTRA_MESSAGE = "UserId";
    public static final String pKey = "Pid";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String ParentKey, ChildKey;

    @BindView(R.id.toolBar) Toolbar toolbar;
    @BindView(R.id.input_email) EditText mEmail;
    @BindView(R.id.input_password) EditText mPassword;
    @BindView(R.id.forgot_link) TextView mForgotPassword;
    @BindView(R.id.btn_login) Button mLoginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // TODO: 8/7/2018 Start Services....
        startService(new Intent(this, UploadDataService.class));

        // TODO: 8/6/2018 toolbar setting...
        toolbar.setTitle("My Chat");
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Now you can Chat with your Parent's");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: 8/6/2018 Database Reference...

        mDatabase = FirebaseDatabase.getInstance().getReference().child("ChildList");
        mAuth = FirebaseAuth.getInstance();


        // TODO: 8/6/2018 LogIn Button Clicked...
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    // TODO: 8/6/2018 Login() Method implementation...
    private void login() {
        Log.d(TAG ,"Login");

        if (!validate()){
            onLoginFailed();
            return;
        }

        mLoginButton.setEnabled(false);
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        onLoginSuccess();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        progressDialog.dismiss();
                    }
                }, 1000
        );
    }

    // TODO: 8/6/2018  Edittext Validation method...
    private boolean validate() {
        boolean valid = true;
        String email = mEmail.getText().toString().trim();
        String pass  = mPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmail.setError("Enter a Valid Email Address");
            valid = false;
        }else {
            mEmail.setError(null);
        }

        if (pass.isEmpty() || pass.length() < 6 || pass.length() > 15){
            mPassword.setError("Password Must be Between 6 and 15 Alphanumeric Characters");
            valid = false;
        }else {
            mPassword.setError(null);
        }
        return valid;
    }


    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login Failed! Try Again...", Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
    }

    // TODO: 8/6/2018 OnLoginSuccess implementation ...
    private void onLoginSuccess() {
        mLoginButton.setEnabled(true);

        String emailText = mEmail.getText().toString().trim();
        String passText = mPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(emailText) && !TextUtils.isEmpty(passText)){
            mAuth.signInWithEmailAndPassword(emailText,passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        ChildKey = mAuth.getCurrentUser().getUid();
                        MyPrefrences myPrefrences = new MyPrefrences(LoginActivity.this);
                        myPrefrences.setID(ChildKey);

                        retrivedID(ChildKey);
                        ParentKey = retrivedID(ChildKey);
                    }

                    if (!task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "No internet Connection!",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    // TODO: 8/7/2018 Retrived Id method implementation...
    private String retrivedID(final String childKey) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ChildList");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()){
                    if (dsp.hasChild(childKey) == true ){
                        ParentKey = dsp.getKey().toString();
                    }
                }

                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                intent.putExtra(EXTRA_MESSAGE, childKey);
                intent.putExtra(pKey,ParentKey);
                finish();
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return ParentKey;
    }
    @Override
    protected void onResume() {
        super.onResume();
        //  progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed() {
        // Disable going back to the LoginActivity
        moveTaskToBack(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
