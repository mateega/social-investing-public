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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SIGN UP ACTIVITY";

    private final String GROUP_1_ID = "penn_crypto";
    private final String GROUP_2_ID = "columbia_blockchain";
    private final String GROUP_3_ID = "tom's_alt_coin_fund";
    private final String GROUP_1_NAME = "Penn Crypto";
    private final String GROUP_2_NAME = "Columbia Blockchain";
    private final String GROUP_3_NAME = "Tom's Alt Coin Fund";

    EditText etName;
    EditText etEmail;
    EditText etPassword;

    Button btnSignup;
    //Social login commented out until implemented
    //Button btnFacebook;
    //Button btnGoogle;
    Button btnLoginText;

    private FirebaseAuth mAuth;
    public static final String USERS = "users";
    FirebaseFirestore db;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        setContentView(R.layout.activity_signup);
        this.getSupportActionBar().hide();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSignup = findViewById(R.id.btnSignup);
//        btnFacebook = findViewById(R.id.btnFacebook);
//        btnGoogle = findViewById(R.id.btnGoogle);
        btnLoginText = findViewById(R.id.btnLoginText);

        btnLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter name, email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User(name, email, password);
                signupUser(name, email, password);
            }
        });

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.i(TAG, "USER SIGNED IN");
            userId = mAuth.getCurrentUser().getEmail();
            HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String, Object>>();
            updateGroupAssetCount(map, GROUP_1_ID);
            updateGroupAssetCount(map, GROUP_2_ID);
            updateGroupAssetCount(map, GROUP_3_ID);
        }
    }

    private void signupUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "CALLED FIREBASE AUTH -- SIGN UP");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            addUserToFirestore(name, email, password);
                            HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String, Object>>();
                            updateGroupAssetCount(map, GROUP_1_ID);
                            updateGroupAssetCount(map, GROUP_2_ID);
                            updateGroupAssetCount(map, GROUP_3_ID);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void addUserToFirestore(String name, String email, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("password", password);
        user.put("assets", 0);
        String defaultProfilePic = "https://www.theparentingplace.net/wp-content/uploads/2021/02/BlankImage.jpg";
        user.put("profilePicture", defaultProfilePic);

        db.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void goToGroups(HashMap<String, HashMap<String, Object>> groupsMap) {
        Intent i = new Intent(this, GroupsActivity.class);
        i.putExtra("groupsMap", groupsMap);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void goToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
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

                        String groupAssetCount = data.get("assets").toString();
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