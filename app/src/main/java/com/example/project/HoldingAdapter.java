package com.example.project;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project.fragments.CoinDetailsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldingAdapter extends RecyclerView.Adapter<HoldingAdapter.ViewHolder> {

    private Context context;
    private List<ArrayList<String>> holdings;
    private String groupId;
    private String TAG = "HOLDING ADAPTER";
    private FirebaseFirestore db;
    private Gson gson;
    private JsonObject jsonCoin;
    Bundle bundle;

    public interface ClickListener {

        void onPositionClicked(int position);

        void onLongClicked(int position);
    }

    public HoldingAdapter(Context context, List<ArrayList<String>> holdings, String groupId) {
        this.context = context;
        this.holdings = holdings;
        this.groupId = groupId;
    }

    public void clear() {
        holdings.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<String> holdings) {
        holdings.addAll(holdings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_holding, parent, false);
        db = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoldingAdapter.ViewHolder holder, int position) {
        ArrayList<String> holding = holdings.get(position);
        holder.bind(holding);
    }

    @Override
    public int getItemCount() {
        return holdings.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvCoinName;
        private TextView tvDollarCount;
        private TextView tvCryptoCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCoinName = itemView.findViewById(R.id.tvCoinName);
            tvDollarCount = itemView.findViewById(R.id.tvDollarCount);
            tvCryptoCount = itemView.findViewById(R.id.tvCryptoCount);

            itemView.setOnClickListener((View.OnClickListener) this);
        }

        public void bind(ArrayList<String> holding) {
            if (holding.get(0).equals("")) {
                return;
            }
            String ticker = holding.get(0);
            tvCoinName.setText(ticker); // UPDATE TO COIN NAME NOT TICKER
            double holdingDollarCountDbl = Double.parseDouble(holding.get(2));
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            String holdingDollarCountStr = "$" + formatter.format(holdingDollarCountDbl);
            tvDollarCount.setText(holdingDollarCountStr);
            Double holdingCyrptoAmountDbl = Math.floor(Double.valueOf(holding.get(1)) * 100000) / 100000;
            String holdingCryptoAmountStr = holdingCyrptoAmountDbl.toString() + " " + holding.get(0);
            tvCryptoCount.setText(holdingCryptoAmountStr);

            // SET COIN IMAGE
            // commented out before functionality is implemented
            // getCoinImage(ticker);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Log.i(TAG, String.valueOf(position));
            bundle = new Bundle();
            String coin = holdings.get(position).get(0);
            getCoinData(coin);
        }
    }

    private void getCoinData(String coin) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest").newBuilder();
        urlBuilder.addQueryParameter("symbol",coin);
        urlBuilder.addQueryParameter("convert","USD");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader(HttpHeaders.ACCEPT, "application/json")
                .addHeader("X-CMC_PRO_API_KEY", BuildConfig.COINMARKETCAP_KEY)
                .build();

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

                        gson = new Gson();
                        jsonCoin = new JsonObject();
                        HashMap<String, String> variables = getCoinVariables(data, coin);

                        getCoinImageForDetails(client, coin);

                        bundle.putString("name", variables.get("name"));
                        bundle.putString("ticker", variables.get("ticker"));
                        bundle.putString("price", variables.get("price"));
                        bundle.putString("priceChange", variables.get("priceChange"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void getCoinImageForDetails(OkHttpClient client, String coin) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://pro-api.coinmarketcap.com/v1/cryptocurrency/info").newBuilder();
        urlBuilder.addQueryParameter("symbol",coin);
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
                        JSONObject cryptocurrency = data.getJSONObject(coin);
                        String logo = cryptocurrency.getString("logo");
                        bundle.putString("imageUrl",logo);

                        AppCompatActivity activity = (AppCompatActivity) context;
                        Fragment coinDetailsFragment = new CoinDetailsFragment();
                        coinDetailsFragment.setArguments(bundle);
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, coinDetailsFragment).addToBackStack(null).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private HashMap<String, String> getCoinVariables(JSONObject data, String coin) throws JSONException {
        HashMap<String, String> variables = new HashMap<String, String>();

        JSONObject cryptocurrency = data.getJSONObject(coin);
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

        return variables;
    }

    private void getCoinImage(String ticker) {
        final String[] imageUrl = {null};
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://pro-api.coinmarketcap.com/v1/cryptocurrency/info").newBuilder();
        urlBuilder.addQueryParameter("symbol",ticker);
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
                        JSONObject cryptocurrency = data.getJSONObject(ticker);
                        String logo = cryptocurrency.getString("logo");
                        imageUrl[0] = logo;
                        if (imageUrl[0] != null) {
                            // set coin image
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}