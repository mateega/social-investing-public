package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import com.example.project.fragments.ChatFragment;
import com.example.project.fragments.DepositFragment;
import com.example.project.fragments.OverviewFragment;
import com.example.project.fragments.SettingsFragment;
import com.example.project.fragments.TradeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.ref.Reference;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN ACTIVITY";
    private BottomNavigationView bottomNavigationView;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    private FirebaseFirestore db;
    String groupId;
    String groupName;

    protected List<String> members;
    protected MemberAdapter memberAdapter;
    protected List<String> investors;
    protected InvestorAdapter investorAdapter;
    protected ArrayList<Map<String, Object>> messages;
    protected ChatAdapter chatAdapter;
    protected List<ArrayList<String>> holdings;
    protected HoldingAdapter holdingAdapter;
    HashMap<String, String> groupData;

    public String memberCount;
    public String groupAssetCount;
    public String personalAssetCount;
    public String recentTrade;

    public String userId;
    public String userName;

    HashMap<String, Object> newUserTrade;

    int currentFragPosition;
    int newFragPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        setContentView(R.layout.activity_main);
        this.getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        if (groupName == null) {
            getGroupName(groupId);
        }
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        memberCount = getIntent().getStringExtra("memberCount");
        groupAssetCount = getIntent().getStringExtra("groupAssetCount");
        personalAssetCount = getIntent().getStringExtra("personalAssetCount");
        recentTrade = getIntent().getStringExtra("recentTrade");
        if (memberCount == null) {
            pullGroupData();
        }

        members = new ArrayList<>();
        investors = new ArrayList<>();
        memberAdapter = new MemberAdapter(this, members);
        investorAdapter = new InvestorAdapter(this, investors);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages, groupId);

        holdings = new ArrayList<>();
        holdingAdapter = new HoldingAdapter(this, holdings, groupId);

        groupData = new HashMap<>();

        newUserTrade = new HashMap<String, Object>();

        currentFragPosition = 1;
        newFragPosition = 1;

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.actionGroupOverview:
                        newFragPosition = 1;
                        fragment = new OverviewFragment();
                        break;
                    case R.id.actionDeposit:
                        newFragPosition = 2;
                        fragment = new DepositFragment();
                        break;
                    case R.id.actionChat:
                        newFragPosition = 3;
                        fragment = new ChatFragment();
                        break;
                    case R.id.actionTrade:
                        newFragPosition = 4;
                        fragment = new TradeFragment();
                        break;
                    case R.id.actionSettings:
                        newFragPosition = 5;
                        fragment = new SettingsFragment();
                        break;
                    default:
                        newFragPosition = 1;
                        fragment = new OverviewFragment();
                        break;
                }
                if (currentFragPosition == newFragPosition) {
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(
                                    R.anim.no_transition,
                                    R.anim.no_transition,
                                    R.anim.no_transition,
                                    R.anim.no_transition
                            )
                            .replace(R.id.flContainer, fragment)
                            .commit();
                } else if (currentFragPosition > newFragPosition) {
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_left,
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_left
                            )
                            .replace(R.id.flContainer, fragment)
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_right,
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_right
                            )
                            .replace(R.id.flContainer, fragment)
                            .commit();
                }
                currentFragPosition = newFragPosition;
                return true;
            }
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.actionGroupOverview);

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.subscribeToTopic("notification_topic");
        Log.i(TAG, "user joined notification topic");

        String deepLink = getIntent().getStringExtra("deepLink");
        if (deepLink !=null) {
            Fragment fragment;
            switch (deepLink) {
                case "www.metau.page.link/chat":
                    fragment = new ChatFragment();
                    break;
                case "www.metau.page.link/overview":
                    fragment = new OverviewFragment();
                    break;
                default:
                    fragment = new OverviewFragment();
                    break;
            }
            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
        }

        pullMemberList();
        pullChat();
        pullHoldings();
    }

    private void getGroupName(String groupId) {
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
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    ///////////////////////////////////////////
    //   FUNCTIONS BELOW FOR CHAT FRAGMENT   //
    ///////////////////////////////////////////

    public ArrayList<Map<String, Object>> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Map<String, Object>> messages) {
        this.messages = messages;
        chatAdapter.notifyDataSetChanged();
    }

    public ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public void setChatAdapter(ChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }

    public void pullChat() {
        db.enableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
        DocumentReference docRef = db.collection("chats").document(groupId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.getResult().getMetadata().isFromCache()) {
//                    Log.i(TAG, "CALLED DATA FROM CACHE");
//                } else {
//                    Log.i(TAG, "CALLED FIREBASE DATABASE -- CHATS");
//                }
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, "DocumentSnapshot data: " + data);
                        if (data.containsKey("messages")) {
                            data.getClass();
                            Object messagesObj = data.get("messages").getClass();
                            if (messagesObj == HashMap.class) {
                                return;
                            }
                            ArrayList<Map<String, Object>> messagesMap = (ArrayList) data.get("messages");
                            messages.addAll(messagesMap);
                            chatAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        db.disableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    ///////////////////////////////////////////
    // FUNCTIONS BELOW FOR SETTINGS FRAGMENT //
    ///////////////////////////////////////////

    public MemberAdapter getMemberAdapter() {
        return memberAdapter;
    }

    public void setMemberAdapter(MemberAdapter memberAdapter) {
        this.memberAdapter = memberAdapter;
    }

    public InvestorAdapter getInvestorAdapter() {
        return investorAdapter;
    }

    public void setInvestorAdapter(InvestorAdapter investorAdapter) {
        this.investorAdapter = investorAdapter;
    }

    public void pullMemberList() {
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
                        ArrayList dbMembers = (ArrayList) data.get("members");
                        ArrayList dbInvestors = (ArrayList) data.get("traders");

                        if (dbMembers != null || dbMembers.get(0).equals("")) {
                            members.addAll(dbMembers);
                            memberAdapter.notifyDataSetChanged();
                        }
                        if (dbInvestors != null || dbInvestors.get(0).equals("")) {
                            investors.addAll(dbInvestors);
                            investorAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        db.disableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    ///////////////////////////////////////////
    // FUNCTIONS BELOW FOR DEPOSIT FRAGMENT  //
    ///////////////////////////////////////////

    public void setPersonalAssets(String personalAssets) {
        personalAssetCount = personalAssets;
        if (personalAssets.charAt(0) != '$') {
            double amount = Double.parseDouble(personalAssetCount);
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            this.personalAssetCount = "$" + formatter.format(amount);
        }
    }

    ///////////////////////////////////////////
    //  FUNCTIONS BELOW FOR TRADE FRAGMENT   //
    ///////////////////////////////////////////

    public HashMap<String, Object> getNewUserTrade() {
        return newUserTrade;
    }

    public void setNewUserTrade(HashMap<String, Object> newUserTrade) {
        this.newUserTrade = newUserTrade;
    }


    ///////////////////////////////////////////
    // FUNCTIONS BELOW FOR OVERVIEW FRAGMENT //
    ///////////////////////////////////////////

    public HashMap<String, String> getGroupData() {
        HashMap<String,String> groupData = new HashMap<String, String>();
        groupData.put("memberCount", memberCount);
        groupData.put("groupAssets", groupAssetCount);
        groupData.put("personalAssets", personalAssetCount);
        groupData.put("recentTrade", recentTrade);
        return groupData;
    }

    public List<ArrayList<String>> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<ArrayList<String>> holdings) {
        this.holdings = holdings;
    }

    public HoldingAdapter getHoldingAdapter() {
        return holdingAdapter;
    }

    public void setHoldingAdapter(HoldingAdapter holdingAdapter) {
        this.holdingAdapter = holdingAdapter;
    }

    public void pullHoldings() {
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
                        setHoldingsFromActivity(data);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setHoldingsFromActivity(Map<String, Object> data) {
        ArrayList<Map<String, Object>> trades = (ArrayList) data.get("trades");
        HashMap<String, ArrayList<String>> tradesMap = new HashMap<String, ArrayList<String>>();
        // trade array list format: [ticker, qnt. in crypto, qnt. in USD]

        for (int i = 0; i < trades.size(); i++) {
            Map<String, Object> trade = trades.get(i);
            String tradeDirection = trade.get("direction").toString();
            Double tradeLot = Double.valueOf(trade.get("lot").toString());
            Double tradePrice = Double.valueOf(trade.get("price").toString());
            String tradeTicker = trade.get("ticker").toString();

            if (tradesMap.containsKey(tradeTicker)) {
                ArrayList<String> oldTrade = tradesMap.get(tradeTicker);
                String oldLot = oldTrade.get(1);
                String oldSizeUSD = oldTrade.get(2);

                String newTradeLot;
                String newTradeSizeUSD;
                ArrayList<String> tradeList = new ArrayList<String>();
                if (tradeDirection.equals("buy")) {
                    newTradeLot = String.valueOf(Double.parseDouble(oldLot) + tradeLot);
                    newTradeSizeUSD = String.valueOf(Double.parseDouble(oldSizeUSD) + (tradeLot * tradePrice));
                } else {
                    newTradeLot = String.valueOf(Double.parseDouble(oldLot) - tradeLot);
                    newTradeSizeUSD = String.valueOf(Double.parseDouble(oldSizeUSD) - (tradeLot * tradePrice));
                }

                tradeList.add(tradeTicker);
                tradeList.add(newTradeLot);
                tradeList.add(newTradeSizeUSD);
                tradesMap.replace(tradeTicker, tradeList);

            } else {
                if (tradeDirection.equals("buy")) {
                    ArrayList<String> tradeList = new ArrayList<String>();
                    String tradeSizeUSD = String.valueOf(tradeLot * tradePrice);
                    tradeList.add(tradeTicker);
                    tradeList.add(String.valueOf(tradeLot));
                    tradeList.add(tradeSizeUSD);
                    tradesMap.put(tradeTicker, tradeList);
                }
                // I don't believe an else statement is needed here since a group must buy an asset
                // before they sell it. This buy will come earlier in the trade list
            }
        }
        holdings.addAll(tradesMap.values());
        holdingAdapter.notifyDataSetChanged();
    }

    //////////////////////////////////////////////////
    // FUNCTIONS BELOW FOR START FROM NOTIFICATION  //
    //////////////////////////////////////////////////

    public HashMap<String, String> getUserData() {
        HashMap<String,String> userData = new HashMap<String, String>();
        userData.put("userId", userId);
        userData.put("userName", userName);
        userData.put("groupId", groupId);
        userData.put("groupName", groupName);
        return userData;
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
                        personalAssetCount = getPersonalAssets(data);
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

    public void pullGroupData() {
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

    public void setGroupAssets(String groupAssetCount) {
        this.groupAssetCount = groupAssetCount;
        double amount = Double.parseDouble(this.groupAssetCount);
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        this.groupAssetCount = "$" + formatter.format(amount);
    }

    public void getNewHoldings() {
        holdings.clear();
        db.enableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
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
                        setHoldingsFromActivity(data);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        db.disableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }
}