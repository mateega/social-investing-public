package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.ref.Reference;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {

    private static final String TAG = "GROUPS ACTIVITY";
    private final String GROUP_1_ID = "penn_crypto";
    private final String GROUP_2_ID = "columbia_blockchain";
    private final String GROUP_3_ID = "tom's_alt_coin_fund";

    private final String GROUP_1_NAME = "Penn Crypto";
    private final String GROUP_2_NAME = "Columbia Blockchain";
    private final String GROUP_3_NAME = "Tom's Alt Coin Fund";

    LinearLayout layGroup1;
    LinearLayout layGroup2;
    LinearLayout layGroup3;
    TextView tvGroupAssets1;
    TextView tvGroupAssets2;
    TextView tvGroupAssets3;
    ImageView ivCheck1;
    ImageView ivCheck2;
    ImageView ivCheck3;

    FirebaseFirestore db;
    FirebaseUser user;
    String userId;
    String userName;

    protected MemberAdapter memberAdapter;
    protected InvestorAdapter investorAdapter;
    protected List<String> members;
    protected List<String> investors;

    protected String personalAssetCount;
    protected String memberCount;
    protected String groupAssetCount;
    protected String recentTrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        db = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_groups);
        this.getSupportActionBar().hide();

        layGroup1 = findViewById(R.id.layGroup1);
        layGroup2 = findViewById(R.id.layGroup2);
        layGroup3 = findViewById(R.id.layGroup3);

        tvGroupAssets1 = findViewById(R.id.tvGroupAssets1);
        tvGroupAssets2 = findViewById(R.id.tvGroupAssets2);
        tvGroupAssets3 = findViewById(R.id.tvGroupAssets3);

        ivCheck1 = findViewById(R.id.ivCheck1);
        ivCheck2 = findViewById(R.id.ivCheck2);
        ivCheck3 = findViewById(R.id.ivCheck3);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getEmail();

        members = new ArrayList<>();
        investors = new ArrayList<>();
        memberAdapter = new MemberAdapter(this, members);
        investorAdapter = new InvestorAdapter(this, investors);

        HashMap<String, HashMap<String, Object>> map = (HashMap<String, HashMap<String, Object>>)getIntent().getSerializableExtra("groupsMap");

        tvGroupAssets1.setText(map.get(GROUP_1_ID).get("assets").toString());
        tvGroupAssets2.setText(map.get(GROUP_2_ID).get("assets").toString());
        tvGroupAssets3.setText(map.get(GROUP_3_ID).get("assets").toString());

        if (!(Boolean)map.get(GROUP_1_ID).get("inGroup")) {
            ivCheck1.setVisibility(View.GONE);
        }
        if (!(Boolean)map.get(GROUP_2_ID).get("inGroup")) {
            ivCheck2.setVisibility(View.GONE);
        }
        if (!(Boolean)map.get(GROUP_3_ID).get("inGroup")) {
            ivCheck3.setVisibility(View.GONE);
        }

        layGroup1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String group = GROUP_1_ID;
                String groupName = GROUP_1_NAME;
                if (!(Boolean)map.get(group).get("inGroup")){
                    addUserToGroup(group);
                }
                pullGroupData(group, groupName);
            }
        });
        layGroup2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String group = GROUP_2_ID;
                String groupName = GROUP_2_NAME;
                if (!(Boolean)map.get(group).get("inGroup")){
                    addUserToGroup(group);
                }
                pullGroupData(group, groupName);
            }
        });
        layGroup3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String group = GROUP_3_ID;
                String groupName = GROUP_3_NAME;
                if (!(Boolean)map.get(group).get("inGroup")){
                    addUserToGroup(group);
                }
                pullGroupData(group, groupName);
            }
        });
    }

    private void addUserToGroup(String groupId) {
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
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + data);
                        members.addAll((ArrayList)data.get("members"));
                        members.add(userId);
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("members", members);
                        db.collection("groups").document(groupId).set(updatedData, SetOptions.merge());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void goToGroup(String groupId, String groupName) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.putExtra("groupId", groupId);
        i.putExtra("groupName", groupName);
        i.putExtra("userName", userName);
        i.putExtra("userId", user.getEmail());
        i.putExtra("memberCount", memberCount);
        i.putExtra("groupAssetCount", groupAssetCount);
        i.putExtra("personalAssetCount", personalAssetCount);
        i.putExtra("recentTrade", recentTrade);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void pullPersonalData(String groupId, String groupName) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- USERS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        personalAssetCount = getPersonalAssets(data);
                        userName = data.get("name").toString();
                        goToGroup(groupId, groupName);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private String getPersonalAssets(Map<String, Object> data) {
        String personalAssetsCount = "0";
        if (data.containsKey("assets")) {
            personalAssetsCount = String.valueOf(data.get("assets"));
        }
        if (personalAssetsCount.equals("0")) {
            personalAssetsCount = "$0.00";
        } else {
            double amount = Double.parseDouble(personalAssetsCount);
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            personalAssetsCount = "$" + formatter.format(amount);
        }
        return personalAssetsCount;
    }

    public void pullGroupData(String groupId, String groupName) {
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
                        Map<String, Object> data = document.getData();
                        memberCount = getMemberCount(data);
                        groupAssetCount = getGroupAssets(data);
                        recentTrade = getRecentTrade(data);
                        pullPersonalData(groupId, groupName);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private String getRecentTrade(Map<String, Object> data) {
        ArrayList<Map<String, Object>> trades = (ArrayList) data.get("trades");
        int numTrades = trades.size();
        Map<String, Object> trade = trades.get(numTrades - 1);
        Timestamp time = (Timestamp) trade.get("time");
        Date javaDate = time.toDate();
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return dateFormat.format(javaDate);
    }

    private String getGroupAssets(Map<String, Object> data) {
        groupAssetCount = data.get("assets").toString();
        double amount = Double.parseDouble(groupAssetCount);
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return "$" + formatter.format(amount);
    }

    private String getMemberCount(Map<String, Object> data) {
        ArrayList<Reference> list = (ArrayList) data.get("members");
        int groupSize = list.size();
        return String.valueOf(groupSize);
    }
}