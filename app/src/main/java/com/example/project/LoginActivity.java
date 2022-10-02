package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN ACTIVITY";

    private final String GROUP_1_ID = "penn_crypto";
    private final String GROUP_2_ID = "columbia_blockchain";
    private final String GROUP_3_ID = "tom's_alt_coin_fund";
    private final String GROUP_1_NAME = "Penn Crypto";
    private final String GROUP_2_NAME = "Columbia Blockchain";
    private final String GROUP_3_NAME = "Tom's Alt Coin Fund";

    EditText etEmail;
    EditText etPassword;

    Button btnLogin;
    Button btnFacebook;
    Button btnGoogle;
    Button btnSignupText;

    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        setContentView(R.layout.activity_login);
        this.getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        //Social login commented out until implemented
        //btnFacebook = findViewById(R.id.btnFacebook);
        //btnGoogle = findViewById(R.id.btnGoogle);
        btnSignupText = findViewById(R.id.btnSignupText);

        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);

        btnSignupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignup();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginUser(email, password);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            userId = mAuth.getCurrentUser().getEmail();
            HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String, Object>>();
            ArrayList<String> groups = new ArrayList<>();
            updateGroupAssetCount(map, GROUP_1_ID);
            updateGroupAssetCount(map, GROUP_2_ID);
            updateGroupAssetCount(map, GROUP_3_ID);
            Log.i(TAG, "USER LOGGED IN");
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "CALLED FIREBASE AUTH -- SIGN IN");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            userId = mAuth.getCurrentUser().getEmail();
                            HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String, Object>>();
                            updateGroupAssetCount(map, GROUP_1_ID);
                            updateGroupAssetCount(map, GROUP_2_ID);
                            updateGroupAssetCount(map, GROUP_3_ID);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToSignup() {
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void goToGroups(HashMap<String, HashMap<String, Object>> groupsMap) {
        Intent i = new Intent(this, GroupsActivity.class);
        i.putExtra("groupsMap", groupsMap);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void updateGroupAssetCount(HashMap<String, HashMap<String, Object>> map, String groupId) {
        DocumentReference docRef = db.collection("groups").document(groupId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- GROUPS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        HashMap<String, Object> groupMap = new HashMap<String, Object>();
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + data);

                        Long groupAssets = (Long) data.get("assets");
                        String groupAssetCount = String.valueOf(groupAssets);
                        double amount = Double.parseDouble(groupAssetCount);
                        DecimalFormat formatter = new DecimalFormat("#,###.00");
                        groupAssetCount = "$" + formatter.format(amount);

                        Boolean memberInGroup = false;
                        ArrayList<String> members = (ArrayList) data.get("members");
                        for (String member:members){
                            if (userId.equals(member)){
                                memberInGroup = true;
                            }
                        }
                        String groupName = "";
                        switch (groupId) {
                            case GROUP_1_ID:
                                groupName = GROUP_1_NAME;
                                break;
                            case GROUP_2_ID:
                                groupName = GROUP_2_NAME;
                                break;
                            case GROUP_3_ID:
                                groupName = GROUP_3_NAME;
                                break;
                            default:
                                groupName = "";
                                break;
                        }

                        groupMap.put("assets", groupAssetCount);
                        groupMap.put("inGroup", memberInGroup);
                        groupMap.put("groupName", groupName);
                        map.put(groupId, groupMap);

                        // if received all group data
                        if (map.size() == 3) {
                            goToGroups(map);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}