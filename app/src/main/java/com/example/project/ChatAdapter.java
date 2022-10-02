package com.example.project;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Map<String, Object>> messages;
    private String chatId;
    private String TAG = "CHAT ADAPTER";
    private FirebaseFirestore db;

    public ChatAdapter(Context context, ArrayList<Map<String, Object>> messages, String groupId) {
        this.context = context;
        this.messages = messages;
        this.chatId = groupId;
    }

    public void clear(){
        messages.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Map<String, Object>> messages) {
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        db = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CardView cvProfileImage;
        private ImageView ivProfileImage;
        private TextView tvName;
        private TextView tvBody;
        private TextView tvTime;

        private TextView tvTradeExecuted;
        private TextView tvTradeTime;
        private TextView tvTradeLine1;
        private TextView tvTradeLine2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvProfileImage = itemView.findViewById(R.id.cvProfileImage);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvTime = itemView.findViewById(R.id.tvTime);

            tvTradeExecuted = itemView.findViewById(R.id.tvTradeExecuted);
            tvTradeTime = itemView.findViewById(R.id.tvTradeTime);
            tvTradeLine1 = itemView.findViewById(R.id.tvTradeLine1);
            tvTradeLine2 = itemView.findViewById(R.id.tvTradeLine2);
        }

        public void bind(Map<String, Object> message) {
            String userId = message.get("user").toString();
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
                            String messageType = message.get("type").toString();
                            setViews(messageType);
                            String name = data.get("name").toString();
                            switch (messageType) {
                                case "message":
                                    tvName.setText(name);
                                    tvBody.setText(message.get("body").toString());
                                    Timestamp time = (Timestamp) message.get("time");
                                    Date javaDate = time.toDate();
                                    DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
                                    tvTime.setText(dateFormat.format(javaDate));
                                    String profilePictureUrl = data.get("profilePicture").toString();
                                    if (profilePictureUrl != null) {
                                        Glide.with(context).load(profilePictureUrl).into(ivProfileImage);
                                    }
                                    break;
                                case "trade":
                                    Timestamp tradeTime = (Timestamp) message.get("time");
                                    Date tradeJavaDate = tradeTime.toDate();
                                    DateFormat tradeDateFormat = new SimpleDateFormat("HH:mm a");
                                    tvTradeTime.setText(tradeDateFormat.format(tradeJavaDate));

                                    String direction = message.get("direction").toString();
                                    Double lotDble = Double.valueOf(message.get("lot").toString());
                                    String lotStr = String.valueOf(Math.floor(lotDble * 100000) / 100000);
                                    String ticker = message.get("ticker").toString();

                                    Double price = Double.valueOf(message.get("price").toString());
                                    DecimalFormat formatter = new DecimalFormat("#,###.00");
                                    String priceStr = "$" + formatter.format(price);
                                    String tradeLine1 = direction + " " + lotStr + " " + ticker + " @" + priceStr;

                                    tvTradeLine1.setText(tradeLine1);
                                    tvTradeLine2.setText("Trade by @" + name);
                                    break;
                                default:
                                    Log.e(TAG, "error reading message");
                                    break;
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

        private void setViews(String messageType) {
            switch (messageType) {
                case "message":
                    cvProfileImage.setVisibility(View.VISIBLE);
                    ivProfileImage.setVisibility(View.VISIBLE);
                    tvName.setVisibility(View.VISIBLE);
                    tvBody.setVisibility(View.VISIBLE);
                    tvTime.setVisibility(View.VISIBLE);
                    tvTradeExecuted.setVisibility(View.GONE);
                    tvTradeTime.setVisibility(View.GONE);
                    tvTradeLine1.setVisibility(View.GONE);
                    tvTradeLine2.setVisibility(View.GONE);
                    break;
                case "trade":
                    cvProfileImage.setVisibility(View.GONE);
                    ivProfileImage.setVisibility(View.GONE);
                    tvName.setVisibility(View.GONE);
                    tvBody.setVisibility(View.GONE);
                    tvTime.setVisibility(View.GONE);
                    tvTradeExecuted.setVisibility(View.VISIBLE);
                    tvTradeTime.setVisibility(View.VISIBLE);
                    tvTradeLine1.setVisibility(View.VISIBLE);
                    tvTradeLine2.setVisibility(View.VISIBLE);
                    break;
                default:
                    Log.e(TAG, "error reading message");
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Log.i(TAG, String.valueOf(position));
        }
    }
}
