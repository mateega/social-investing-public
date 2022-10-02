package com.example.project.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.project.HoldingAdapter;
import com.example.project.MainActivity;
import com.example.project.MemberAdapter;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverviewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "OVERVIEW FRAGMENT";

    private String groupId = "";
    private String groupName = "";
    private String userId = "";
    private String userName = "";

    private String memberCount;
    private String groupAssetCount;
    private String personalAssetsCount;
    private String recentTrade;
    FirebaseFirestore db;

    TextView tvHello;
    TextView tvWelcomeBack;
    TextView tvMembersCount;
    TextView tvGroupAssetsCount;
    TextView tvPersonalAssetsCount;
    TextView tvRecentTradeDate;

    private RecyclerView rvHoldings;
    protected HoldingAdapter holdingAdapter;

    Boolean comingFromNotification;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OverviewFragment newInstance(String param1, String param2) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvHello = view.findViewById(R.id.tvHello);
        tvWelcomeBack = view.findViewById(R.id.tvWelcomeBack);
        tvMembersCount = view.findViewById(R.id.tvMembersCount);
        tvGroupAssetsCount = view.findViewById(R.id.tvGroupAssetsCount);
        tvPersonalAssetsCount = view.findViewById(R.id.tvPersonalAssetsCount);
        tvRecentTradeDate = view.findViewById(R.id.tvRecentTradeDate);

        groupId = getActivity().getIntent().getStringExtra("groupId");
        groupName = getActivity().getIntent().getStringExtra("groupName");
        userId = getActivity().getIntent().getStringExtra("userId");
        userName = getActivity().getIntent().getStringExtra("userName");
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        if (groupName == null) {
            HashMap<String, String> userData = new HashMap<>();
            userData.putAll(((MainActivity)getActivity()).getUserData());
            if (userData.get("groupId") != null) {
                groupId = userData.get("groupId");
                groupName = userData.get("groupName");
                userId = userData.get("userId");
                userName = userData.get("userName");
            }
        }
        if (groupName == null) {
            getGroupName(groupId);
        }
        comingFromNotification = false;
        if (userName == null) {
            comingFromNotification = true;
            getUserName();
        }

        tvHello.setText("Hello, " + userName);
        tvWelcomeBack.setText("Welcome Back to " + groupName + "!");

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        rvHoldings = view.findViewById(R.id.rvHoldings);
        holdingAdapter = ((MainActivity)getActivity()).getHoldingAdapter();
        rvHoldings.setAdapter(holdingAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvHoldings.setLayoutManager(linearLayoutManager);

        HashMap<String, String> groupData = new HashMap<>();
        groupData.putAll(((MainActivity)getActivity()).getGroupData());

        if (groupData.get("memberCount") == null) {
            pullGroupData();
        } else {
            tvMembersCount.setText(groupData.get("memberCount"));
            tvGroupAssetsCount.setText(groupData.get("groupAssets"));
            tvPersonalAssetsCount.setText(groupData.get("personalAssets"));
            tvRecentTradeDate.setText(groupData.get("recentTrade"));
        }
    }

    private void getGroupName(String groupId) {
        db = FirebaseFirestore.getInstance();
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
                        groupName = data.get("name").toString();
                        tvWelcomeBack.setText("Welcome Back to " + groupName + "!");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void pullGroupData() {
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
                        pullPersonalData();
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
        Long groupAssets = (Long) data.get("assets");
        groupAssetCount = String.valueOf(groupAssets);
        double amount = Double.parseDouble(groupAssetCount);
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return "$" + formatter.format(amount);
    }

    private String getMemberCount(Map<String, Object> data) {
        ArrayList<Reference> list = (ArrayList) data.get("members");
        int groupSize = list.size();
        return String.valueOf(groupSize);
    }

    private void getUserName() {
        db = FirebaseFirestore.getInstance();
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
                        Log.d(TAG, "DocumentSnapshot data: " + data);
                        userName = data.get("name").toString();
                        tvHello.setText("Hello, " + userName);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void pullPersonalData() {
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
                        userName = data.get("name").toString();
                        personalAssetsCount = getPersonalAssets(data);
                        tvMembersCount.setText(memberCount);
                        tvGroupAssetsCount.setText(groupAssetCount);
                        tvPersonalAssetsCount.setText(personalAssetsCount);
                        tvRecentTradeDate.setText(recentTrade);
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

}