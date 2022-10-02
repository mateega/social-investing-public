package com.example.project.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.project.MainActivity;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DepositFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DepositFragment extends Fragment {

    private static final String TAG = "DEPOSIT FRAGMENT";
    private FirebaseFirestore db;
    String userId;

    TextView tvCurrentAccountAssets;
    TextView tvDepositAmount;

    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    Button btn5;
    Button btn6;
    Button btn7;
    Button btn8;
    Button btn9;
    Button btn0;

    Button btnDot;
    ImageButton btnDelete;

    Button btnCancel;
    Button btnDeposit;

    Double depositAmount;
    String depositAmountStr;
    int numLocation;
    String currentAssets;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DepositFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DepositFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DepositFragment newInstance(String param1, String param2) {
        DepositFragment fragment = new DepositFragment();
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
        return inflater.inflate(R.layout.fragment_deposit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        currentAssets = "";

        tvCurrentAccountAssets = view.findViewById(R.id.tvCurrentAccountAssets);

        HashMap<String, String> groupData = new HashMap<>();
        groupData.putAll(((MainActivity) getActivity()).getGroupData());
        String personalAssets = groupData.get("personalAssets");
        if (personalAssets != null) {
            tvCurrentAccountAssets.setText("Current account assets: " + groupData.get("personalAssets"));
        }

        tvDepositAmount = view.findViewById(R.id.tvDepositAmount);
        tvDepositAmount.setText(depositAmountStr);

        depositAmount = 0.00;
        setDepositAmount(depositAmount);
        numLocation = 0;

        btn1 = view.findViewById(R.id.btn1);
        btn2 = view.findViewById(R.id.btn2);
        btn3 = view.findViewById(R.id.btn3);
        btn4 = view.findViewById(R.id.btn4);
        btn5 = view.findViewById(R.id.btn5);
        btn6 = view.findViewById(R.id.btn6);
        btn7 = view.findViewById(R.id.btn7);
        btn8 = view.findViewById(R.id.btn8);
        btn9 = view.findViewById(R.id.btn9);
        btn0 = view.findViewById(R.id.btn0);

        btnDot = view.findViewById(R.id.btnDot);
        btnDelete = view.findViewById(R.id.btnDelete);

        btnCancel = view.findViewById(R.id.btnCancel);
        btnDeposit = view.findViewById(R.id.btnDeposit);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(1);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(2);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(3);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(4);
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(5);
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(6);
            }
        });
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(7);
            }
        });
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(8);
            }
        });
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(9);
            }
        });
        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNumber(0);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickDelete();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depositAmount = 0.00;
                setDepositAmount(depositAmount);
            }
        });
        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deposit();
            }
        });

    }

    private void deposit() {
        numLocation = 0;
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
                        Double assets = 0.00;
                        if (data.containsKey("assets")) {
                            assets = Double.valueOf(data.get("assets").toString());
                        }
                        assets = assets + depositAmount;

                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("assets", assets);
                        db.collection("users").document(userId).set(updatedData, SetOptions.merge());

                        depositAmount = 0.00;
                        setDepositAmount(depositAmount);

                        DecimalFormat formatter = new DecimalFormat("#,###.00");
                        currentAssets = "Current account assets: $" + formatter.format(assets);
                        if (assets != null) {
                            ((MainActivity) getActivity()).setPersonalAssets("$" + formatter.format(assets));
                        }
                        tvCurrentAccountAssets.setText(currentAssets);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void clickNumber(int i) {
        numLocation++;
        if (numLocation > 11) {
            return;
        }

        depositAmount = depositAmount*10 + (i * 0.01);
        setDepositAmount(depositAmount);

    }

    public void clickDelete() {
        numLocation--;
        depositAmount = depositAmount*0.1;
        if (depositAmount < 0.01) {
            depositAmount = 0.00;
        }
        setDepositAmount(depositAmount);
    }

    private void setDepositAmount(Double amount){
        if (amount < 1) {
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            depositAmountStr = "$0" + formatter.format(amount);
        } else {
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            depositAmountStr = "$" + formatter.format(amount);
        }
        tvDepositAmount.setText(depositAmountStr);
    }

}