package com.example.project.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
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
import android.widget.Button;

import com.example.project.GroupsActivity;
import com.example.project.InvestorAdapter;
import com.example.project.LoginActivity;
import com.example.project.MainActivity;
import com.example.project.MemberAdapter;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "SETTINGS FRAGMENT";

    private final String GROUP_1_ID = "penn_crypto";
    private final String GROUP_2_ID = "columbia_blockchain";
    private final String GROUP_3_ID = "tom's_alt_coin_fund";
    private final String GROUP_1_NAME = "Penn Crypto";
    private final String GROUP_2_NAME = "Columbia Blockchain";
    private final String GROUP_3_NAME = "Tom's Alt Coin Fund";

    private Button btnLeaveGroup;
    private Button btnSignout;
    private FirebaseFirestore db;
    String groupId;
    String userId;

    private RecyclerView rvInvestors;
    protected InvestorAdapter investorAdapter;
    LinearLayoutManager investorLinearLayoutManager;

    private RecyclerView rvMembers;
    protected MemberAdapter memberAdapter;
    LinearLayoutManager memberLinearLayoutManager;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLeaveGroup = view.findViewById(R.id.btnLeaveGroup);
        btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUserFromGroup();
            }
        });

        btnSignout = view.findViewById(R.id.btnSignout);
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(i,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        rvMembers = view.findViewById(R.id.rvMembers);
        memberAdapter = ((MainActivity)getActivity()).getMemberAdapter();
        rvMembers.setAdapter(memberAdapter);
        memberLinearLayoutManager = new LinearLayoutManager(getContext());
        rvMembers.setLayoutManager(memberLinearLayoutManager);

        rvInvestors = view.findViewById(R.id.rvInvestors);
        investorAdapter = ((MainActivity)getActivity()).getInvestorAdapter();
        rvInvestors.setAdapter(investorAdapter);
        investorLinearLayoutManager = new LinearLayoutManager(getContext());
        rvInvestors.setLayoutManager(investorLinearLayoutManager);

        db = FirebaseFirestore.getInstance();
        groupId = getActivity().getIntent().getStringExtra("groupId");
        userId = getActivity().getIntent().getStringExtra("userId");

        if (userId == null) {
            HashMap<String, String> userData = new HashMap<>();
            userData.putAll(((MainActivity)getActivity()).getUserData());
            groupId = userData.get("groupId");
            userId = userData.get("userId");
        }
    }

    private void removeUserFromGroup() {
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

                        ArrayList<String> members = (ArrayList) data.get("members");
                        members.remove(userId);

                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("members", members);
                        db.collection("groups").document(groupId).set(updatedData, SetOptions.merge());

                        HashMap<String, HashMap<String, Object>> map = new HashMap<String, HashMap<String, Object>>();
                        updateGroupAssetCount(map, GROUP_1_ID);
                        updateGroupAssetCount(map, GROUP_2_ID);
                        updateGroupAssetCount(map, GROUP_3_ID);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void goToGroups(HashMap<String, HashMap<String, Object>> groupsMap) {
        Intent i = new Intent(getActivity().getApplicationContext(), GroupsActivity.class);
        i.putExtra("groupsMap", groupsMap);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
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