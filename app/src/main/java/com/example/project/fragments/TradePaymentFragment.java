package com.example.project.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.MainActivity;
import com.example.project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TradePaymentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TradePaymentFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final String TAG = "TRADE PAYMENT FRAGMENT";
    private FirebaseFirestore db;
    String userId;
    String groupId;

    String name;
    String ticker;
    String price;
    String priceChange;
    String rank;
    String imageUrl;
    String direction;
    Boolean buy;

    ImageButton ibBack;

    TextView tvCurrentAccountAssets;
    TextView tvBuySellAmount;

    TextView tvBuySell;

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
    Button btnTrade;

    Double buyAmount;
    String buyAmountStr;
    int numLocation;
    String currentAssets;
    Double assets;

    Double sellAssets;

    Bundle bundle;

    public TradePaymentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TradePaymentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TradePaymentFragment newInstance(String param1, String param2) {
        TradePaymentFragment fragment = new TradePaymentFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        name = getArguments().getString("name");
        ticker = getArguments().getString("ticker");
        price = getArguments().getString("price");
        priceChange = getArguments().getString("priceChange");
        rank = getArguments().getString("rank");
        imageUrl = getArguments().getString("imageUrl");
        direction = getArguments().getString("direction");
        if (direction.equals("buy")) {
            buy = true;
        } else {
            buy = false;
        }

        bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("ticker", ticker);
        bundle.putString("price", price);
        bundle.putString("priceChange", price);
        bundle.putString("rank", rank);
        bundle.putString("imageUrl", imageUrl);
        bundle.putString("direction", direction);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trade_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        currentAssets = "";

        ibBack = view.findViewById(R.id.ibBack);

        tvCurrentAccountAssets = view.findViewById(R.id.tvCurrentAccountAssets);
        getCurrentBuyAssets();

        tvBuySellAmount = view.findViewById(R.id.tvBuySellAmount);
        tvBuySellAmount.setText(buyAmountStr);

        buyAmount = 0.00;
        setBuySellAmount(buyAmount);
        numLocation = 0;

        tvBuySell = view.findViewById(R.id.tvBuySell);
        if (buy) {
            tvBuySell.setText("Buy " + name);
        } else {
            tvBuySell.setText("Sell " + name);
        }

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
        btnTrade = view.findViewById(R.id.btnTrade);

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
                buyAmount = 0.00;
                setBuySellAmount(buyAmount);
            }
        });
        btnTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trade();
            }
        });
        if (!buy) {
            btnTrade.setText("Confirm Sell");
        }
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Fragment coinDetailsFragment = new CoinDetailsFragment();
                coinDetailsFragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_left,
                                R.anim.slide_out_left,
                                R.anim.slide_in_left,
                                R.anim.slide_out_left
                        )
                        .replace(R.id.flContainer, coinDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        groupId = getActivity().getIntent().getStringExtra("groupId");
    }

    private void trade() {
        numLocation = 0;

        if (buy) {
            if (buyAmount > assets) {
                Toast.makeText(getActivity().getApplicationContext(), "Account has insufficient funds", Toast.LENGTH_SHORT).show();
            } else {
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

                                ArrayList<Object> trades = (ArrayList) data.get("trades");
                                HashMap<String, Object> trade = new HashMap<>();

                                String tradeDirection = "buy";
                                NumberFormat format = NumberFormat.getCurrencyInstance();
                                Number priceNumber = null;
                                try {
                                    priceNumber = format.parse(price);
                                    System.out.println(priceNumber);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Double tradeLot = buyAmount / (Double.valueOf((priceNumber.toString())));
                                Timestamp tradeTimestamp = Timestamp.now();

                                trade.put("direction", tradeDirection);
                                trade.put ("lot", tradeLot);
                                trade.put("price", priceNumber);
                                trade.put("ticker", ticker);
                                trade.put("time", tradeTimestamp);
                                trade.put("trader", userId);
                                trades.add(trade);

                                String groupAssetsStr = data.get("assets").toString();
                                Double groupAssetsDbl = Double.valueOf(groupAssetsStr);
                                Double tradeAmount = Double.valueOf(priceNumber.toString()) * tradeLot;
                                groupAssetsDbl += tradeAmount;
                                Number groupAssetsNum = (Number)groupAssetsDbl;
                                ((MainActivity)getActivity()).setGroupAssets(groupAssetsDbl.toString());
                                Map<String, Object> updatedData = new HashMap<>();
                                updatedData.put("trades", trades);
                                updatedData.put("assets", groupAssetsNum);
                                db.enableNetwork()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            }
                                        });
                                db.collection("groups").document(groupId).set(updatedData, SetOptions.merge());
                                ((MainActivity)getActivity()).getNewHoldings();
                                updateUserBuyBalance(buyAmount);
                                buyAmount = 0.00;
                                setBuySellAmount(buyAmount);
                                Toast.makeText(getActivity().getApplicationContext(), "Trade confirmed", Toast.LENGTH_SHORT).show();
                                sendTradeToChat(trade, groupId);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        } else {
            // selling
            if (buyAmount > sellAssets) {
                Toast.makeText(getActivity().getApplicationContext(), "Account has insufficient holdings", Toast.LENGTH_SHORT).show();
            } else {
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

                                ArrayList<Object> trades = (ArrayList) data.get("trades");
                                HashMap<String, Object> trade = new HashMap<>();

                                String tradeDirection = "sell";
                                NumberFormat format = NumberFormat.getCurrencyInstance();
                                Number priceNumber = null;
                                try {
                                    priceNumber = format.parse(price);
                                    System.out.println(priceNumber);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                Double tradeLot = buyAmount / (Double.valueOf(priceNumber.toString()));
                                Timestamp tradeTimestamp = Timestamp.now();

                                trade.put("direction", tradeDirection);
                                trade.put ("lot", tradeLot);
                                trade.put("price", priceNumber);
                                trade.put("ticker", ticker);
                                trade.put("time", tradeTimestamp);
                                trade.put("trader", userId);
                                trades.add(trade);

                                String groupAssetsStr = data.get("assets").toString();
                                Double groupAssetsDbl = Double.valueOf(groupAssetsStr);
                                Double tradeAmount = Double.valueOf(priceNumber.toString()) * tradeLot;
                                groupAssetsDbl -= tradeAmount;
                                Number groupAssetsNum = (Number)groupAssetsDbl;
                                ((MainActivity)getActivity()).setGroupAssets(groupAssetsDbl.toString());
                                Map<String, Object> updatedData = new HashMap<>();
                                updatedData.put("trades", trades);
                                updatedData.put("assets", groupAssetsNum);
                                db.collection("groups").document(groupId).set(updatedData, SetOptions.merge());
                                ((MainActivity)getActivity()).getNewHoldings();
                                updateUserBuyBalance(buyAmount);
                                buyAmount = 0.00;
                                setBuySellAmount(buyAmount);
                                Toast.makeText(getActivity().getApplicationContext(), "Trade confirmed", Toast.LENGTH_SHORT).show();
                                sendTradeToChat(trade, groupId);
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
    }

    private void sendTradeToChat(HashMap<String, Object> trade, String chatId) {
        DocumentReference docRef = db.collection("chats").document(chatId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().getMetadata().isFromCache()) {
                    Log.i(TAG, "CALLED DATA FROM CACHE");
                } else {
                    Log.i(TAG, "CALLED FIREBASE DATABASE -- CHATS");
                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        ArrayList<Map<String, Object>> messages = (ArrayList<Map<String, Object>>) data.get("messages");

                        HashMap<String, Object> message = new HashMap<String, Object>();
                        message.put("direction", trade.get("direction"));
                        message.put("lot", trade.get("lot"));
                        message.put("price", trade.get("price"));
                        message.put("ticker", trade.get("ticker"));
                        message.put("time", trade.get("time"));
                        message.put("user", userId);
                        message.put("type", "trade");
                        message.put("groupId", chatId);

                        ((MainActivity)getActivity()).setNewUserTrade(message);

                        messages.add(message);
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("messages", messages);
                        db.collection("chats").document(chatId).set(updatedData, SetOptions.merge());

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void updateUserBuyBalance(Double buyAmount) {
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
                        if (buy) {
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, "DocumentSnapshot data: " + data);

                            Double assets = (Double) data.get("assets");
                            assets = assets - buyAmount;

                            Map<String, Object> updatedData = new HashMap<>();
                            updatedData.put("assets", assets);
                            db.collection("users").document(userId).set(updatedData, SetOptions.merge());
                            ((MainActivity)getActivity()).setPersonalAssets(assets.toString());
                            DecimalFormat formatter = new DecimalFormat("#,###.00");
                            currentAssets = "You have $" + formatter.format(assets) + " available";
                            tvCurrentAccountAssets.setText(currentAssets);
                        } else {
                            getSellAssets();
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

    private void clickDelete() {
        numLocation--;
        buyAmount = buyAmount*0.1;
        if (buyAmount < 0.01) {
            buyAmount = 0.00;
        }
        setBuySellAmount(buyAmount);
    }

    private void clickNumber(int i) {
        numLocation++;
        if (numLocation > 11) {
            return;
        }
        buyAmount = buyAmount*10 + (i * 0.01);
        setBuySellAmount(buyAmount);
    }

    private void getCurrentBuyAssets() {
        if (buy) {
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
                            assets = Double.valueOf(data.get("assets").toString());
                            if (assets == 0.00) {
                                currentAssets = "You have $0.00 available";
                                tvCurrentAccountAssets.setText(currentAssets);
                            } else {
                                DecimalFormat formatter = new DecimalFormat("#,###.00");
                                currentAssets = "You have $" + formatter.format(assets) + " available";
                                tvCurrentAccountAssets.setText(currentAssets);
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        } else {
            getSellAssets();
        }
    }

    private void getSellAssets() {
        final Double[] holdingInUSD = {0.00};
        groupId = getActivity().getIntent().getStringExtra("groupId");
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

                        ArrayList<Map<String, Object>> trades = (ArrayList) data.get("trades");

                        for (int i = 0; i < trades.size(); i++) {
                            Map<String, Object> trade = trades.get(i);
                            String tradeTicker = trade.get("ticker").toString();

                            if (tradeTicker.equals(ticker)) {
                                String tradeDirection = trade.get("direction").toString();
                                Double tradeLot = Double.valueOf(trade.get("lot").toString());
                                Double tradePrice = Double.valueOf(trade.get("price").toString());

                                Double tradeSizeUSD = tradeLot * tradePrice;
                                if (tradeDirection.equals("buy")) {
                                    holdingInUSD[0] += tradeSizeUSD;
                                } else {
                                    holdingInUSD[0] -= tradeSizeUSD;
                                }
                            }
                        }
                        sellAssets = holdingInUSD[0];
                        if (holdingInUSD[0] == 0.00) {
                            currentAssets = "You have $0.00 in " + ticker;
                        } else {
                            DecimalFormat formatter = new DecimalFormat("#,###.00");
                            currentAssets = "You have $" + formatter.format(holdingInUSD[0]) + " in " + ticker;
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

    private void setBuySellAmount(Double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        if (amount < 1) {
            buyAmountStr = "$0" + formatter.format(amount);
        } else {
            buyAmountStr = "$" + formatter.format(amount);
        }
        tvBuySellAmount.setText(buyAmountStr);
    }
}