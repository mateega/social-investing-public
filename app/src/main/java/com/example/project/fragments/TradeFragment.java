package com.example.project.fragments;

import static com.google.common.collect.ComparisonChain.start;

import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.project.BuildConfig;
import com.example.project.Coin;
import com.example.project.CoinsAdapter;
import com.example.project.MainActivity;
import com.example.project.R;
import com.google.common.net.HttpHeaders;
import com.google.firebase.database.core.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TradeFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "TRADE FRAGMENT";

    EditText etSearch;

    private String mParam1;
    private String mParam2;
    private String searchText;

    private RecyclerView rvCoins;
    protected CoinsAdapter adapter;
    protected List<Coin> allCoins;
    protected List<Coin> coinsForRV; //coins before all of their fields are updated and sent to allCoins
    boolean setTopCoins;

    private Coin actualCoin;
    private Gson gson;
    private JsonObject jsonCoin;

    RelativeLayout layCoinSearch;
    TextView tvCoinNameSearch;
    TextView tvCoinTickerSearch;
    TextView tvPriceSearch;
    TextView tvPriceChangeSearch;
    ImageView ivCoinImageSearch;

    TextView tvTopCoins;

    String nameSearched;
    String tickerSearched;
    String priceSearched;
    String priceChangeSearched;
    String rankSearched;
    String imageUrlSearched;

    public TradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TradeFragment newInstance(String param1, String param2) {
        TradeFragment fragment = new TradeFragment();
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
        return inflater.inflate(R.layout.fragment_trade, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCoins = view.findViewById(R.id.rvCoins);
        allCoins = new ArrayList<>();
        adapter = new CoinsAdapter(getContext(), allCoins);
        rvCoins.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvCoins.setLayoutManager(linearLayoutManager);

        layCoinSearch = view.findViewById(R.id.layCoinSearch);
        tvCoinNameSearch = view.findViewById(R.id.tvCoinNameSearch);
        tvCoinTickerSearch = view.findViewById(R.id.tvCoinTickerSearch);
        tvPriceSearch = view.findViewById(R.id.tvPriceSearch);
        tvPriceChangeSearch = view.findViewById(R.id.tvPriceChangeSearch);
        ivCoinImageSearch = view.findViewById(R.id.ivCoinImageSearch);
        tvTopCoins = view.findViewById(R.id.tvTopCoins);
        toggleCoinViews(false);
        coinsForRV = new ArrayList<>();

        setTopCoins = false;
        // list today's top cryptocurrencies
        ArrayList<String> coins = new ArrayList<>();
        coins.add("BTC");
        coins.add("ETH");
        coins.add("USDT");
        coins.add("USDC");
        coins.add("BNB");
        coins.add("BUSD");
        coins.add("XPR");
        coins.add("ADA");
        coins.add("SOL");
        coins.add("DOGE");
        getCoinsText(coins, false);

        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchText = s.toString();
                if (searchText.isEmpty() || searchText.equals("")) {
                    Log.i(TAG, "Search is empty");
                    toggleCoinViews(false);
                } else {
                    try {
                        Log.i(TAG, "Text change");
                        searchForCoin(searchText);
                        toggleCoinViews(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        layCoinSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("name", nameSearched);
                bundle.putString("ticker", tickerSearched);
                bundle.putString("price", priceSearched);
                bundle.putString("priceChange", priceChangeSearched);
                bundle.putString("rank", rankSearched);
                bundle.putString("imageUrl", imageUrlSearched);

                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Fragment coinDetailsFragment = new CoinDetailsFragment();
                coinDetailsFragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_right,
                                R.anim.slide_in_right,
                                R.anim.slide_out_right
                        )
                        .replace(R.id.flContainer, coinDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        rvCoins.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

    }

    private void toggleCoinViews(boolean searching) {
        if (searching) {
            tvTopCoins.setVisibility(View.GONE);
            rvCoins.setVisibility(View.GONE);
            layCoinSearch.setVisibility(View.VISIBLE);
        } else {
            tvTopCoins.setVisibility(View.VISIBLE);
            rvCoins.setVisibility(View.VISIBLE);
            layCoinSearch.setVisibility(View.GONE);
        }
    }

    private void searchForCoin(String searchText) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://rest.coinapi.io/v1/assets?filter_asset_id=" + searchText)
                .get()
                .addHeader(BuildConfig.COINAPI_AUTH_HEADER, BuildConfig.COINAPI_KEY_1)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.i(TAG, "CALLED COINAPI API");
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    String jsonData = responseBody.string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        JSONObject object = jsonArray.getJSONObject(0);
                        String ticker = object.getString("asset_id");
                        ArrayList<String> coins = new ArrayList<>();
                        coins.add(ticker);
                        getCoinsText(coins, true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void getCoinsText(ArrayList<String> coins, boolean search) {
        String coinsStr = "";
        for (int i = 0; i < coins.size(); i++) {
            String coin = coins.get(i);
            coinsStr = coinsStr + coin;
            if (i != coins.size()-1) {
                coinsStr = coinsStr + ",";
            }
        }

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest").newBuilder();
        urlBuilder.addQueryParameter("symbol",coinsStr);
        urlBuilder.addQueryParameter("convert","USD");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader(HttpHeaders.ACCEPT, "application/json")
                .addHeader("X-CMC_PRO_API_KEY", BuildConfig.COINMARKETCAP_KEY)
                .build();

        String finalCoinsStr = coinsStr;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.i(TAG, "CALLED COINMARKETCAP API");
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    String jsonData = responseBody.string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject data = jsonObject.getJSONObject("data");
                        for (int i = 1; i < data.length() + 1; i++) {
                            gson = new Gson();
                            jsonCoin = new JsonObject();
                            HashMap<String, String> variables = getCoinVariables(data, coins, i);
                            if (search) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setSearchViews(variables.get("name"), variables.get("ticker"), variables.get("price"), variables.get("priceChange"), variables.get("rank"));
                                    }
                                });
                            } else {
                                createCoinObject(variables, i);
                            }
                        }
                        if (!search) {
                            allCoins.addAll(coinsForRV);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    setTopCoins = true;
                                    getCoinsImages(client, coins, finalCoinsStr, search);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        if (search) {
            getCoinsImages(client, coins, finalCoinsStr, search);
        }
    }

    private void createCoinObject(HashMap<String, String> variables, int i) {
        jsonCoin.addProperty("name", variables.get("name"));
        jsonCoin.addProperty("ticker", variables.get("ticker"));
        jsonCoin.addProperty("price", variables.get("price"));
        jsonCoin.addProperty("priceChange", variables.get("priceChange"));
        jsonCoin.addProperty("rank", "#" + i);

        // Data points below chart
        // Commented out until I decide which data points should be displayed (e.g. high, low, open, volume)
//        jsonCoin.addProperty("high", variables.get("high"));
//        jsonCoin.addProperty("low", variables.get("low"));
//        jsonCoin.addProperty("open", variables.get("open"));
//        jsonCoin.addProperty("volume", variables.get("volume"));

        actualCoin = gson.fromJson(jsonCoin, Coin.class);
        coinsForRV.add(actualCoin);
    }

    private HashMap<String, String> getCoinVariables(JSONObject data, ArrayList<String> coins, int i) throws JSONException {
        HashMap<String, String> variables = new HashMap<String, String>();

        JSONObject cryptocurrency = data.getJSONObject(coins.get(i-1));
        String name = cryptocurrency.getString("name");
        String ticker = cryptocurrency.getString("symbol");
        JSONObject quote = cryptocurrency.getJSONObject("quote");
        JSONObject usd = quote.getJSONObject("USD");
        String priceStr = usd.getString("price");
        Double priceDbl = Double.parseDouble(priceStr);
        DecimalFormat formatter1 = new DecimalFormat("#,###.00");
        if (priceStr.charAt(0) == '0') {
            priceStr = "$0" + formatter1.format(priceDbl);
        } else {
            priceStr = "$" + formatter1.format(priceDbl);
        }

        String changeStr = usd.getString("percent_change_24h");
        Double changeDbl = Double.parseDouble(changeStr);
        DecimalFormat formatter2 = new DecimalFormat("##.##");
        changeStr = formatter2.format(changeDbl) + "%";

        variables.put("name", name);
        variables.put("ticker", ticker);
        variables.put("price", priceStr);
        variables.put("priceChange", changeStr);

        // Data points below chart
        // Commented out until I decide which data points should be displayed (e.g. high, low, open, volume)
//        variables.put("high", high);
//        variables.put("low", low);
//        variables.put("open", open);
//        variables.put("volume", volume);
        return variables;
    }

    private void getCoinsImages(OkHttpClient client, ArrayList<String> coins, String coinsStr, Boolean search) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://pro-api.coinmarketcap.com/v1/cryptocurrency/info").newBuilder();
        urlBuilder.addQueryParameter("symbol",coinsStr);
        String url = urlBuilder.build().toString();

        Request imageRequest = new Request.Builder()
                .url(url)
                .get()
                .addHeader(HttpHeaders.ACCEPT, "application/json")
                .addHeader("X-CMC_PRO_API_KEY", BuildConfig.COINMARKETCAP_KEY)
                .build();

        client.newCall(imageRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.i(TAG, "CALLED COINMARKETCAP API");
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    String jsonData = responseBody.string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject data = jsonObject.getJSONObject("data");
                        if (search) {
                            JSONObject cryptocurrency = data.getJSONObject(coins.get(0));
                            String logo = cryptocurrency.getString("logo");
                            imageUrlSearched = logo;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setSearchImageViews(logo);
                                }
                            });
                        } else {
                            addImagesCoins(data, coins);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void addImagesCoins(JSONObject data, ArrayList<String> coins) throws JSONException {
        for (int i = 1; i < coinsForRV.size() + 1; i++) {
            gson = new Gson();
            Coin coin = coinsForRV.get(i-1);
            JSONObject cryptocurrency = data.getJSONObject(coins.get(i-1));
            String logo = cryptocurrency.getString("logo");
            coin.setImageUrl(logo);
            coinsForRV.set(i-1, coin);
        }

        allCoins.clear();
        allCoins.addAll(coinsForRV);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setSearchViews(String name, String ticker, String price, String priceChange, String rank) {
        nameSearched = name;
        tickerSearched = ticker;
        priceSearched = price;
        priceChangeSearched = priceChange;
        rankSearched = rank;

        int color = Color.parseColor("#47cd54");
        if (priceChange.charAt(0) == '-') {
            color = Color.parseColor("#fe5857");
        }

        tvCoinNameSearch.setText(name);
        tvCoinTickerSearch.setText(ticker);
        tvPriceSearch.setText(price);
        tvPriceChangeSearch.setText(priceChange);
        tvPriceChangeSearch.setTextColor(color);
    }

    private void setSearchImageViews(String profilePictureUrl) {
        if (profilePictureUrl != null) {
            Glide.with(getActivity().getApplicationContext()).load(profilePictureUrl).into(ivCoinImageSearch);
        }
    }

}