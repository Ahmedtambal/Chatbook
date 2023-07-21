package com.example.chatbook.Data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatbook.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private ArrayList<ChatMessage> chatMessages;
    private String currentUsername;

    private Context context;


    public MessageAdapter(ArrayList<ChatMessage> chatMessages, Context context, String currentUsername) {
        this.chatMessages = chatMessages;
        this.context = context;
        this.currentUsername = currentUsername;



    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        ChatMessage chatMessage = chatMessages.get(viewType);


        boolean isSender = false;
        if (currentUsername != null && chatMessage.getSender() != null) {
            isSender = currentUsername.equals(chatMessage.getSender().getUsername());
        }
        return new MessageViewHolder(view, isSender);
    }


    public MessageAdapter(ArrayList<ChatMessage> chatMessages, Context context, AppUser appUser) {
        this.chatMessages = chatMessages;
        this.context = context;

        if (appUser != null) {
            this.currentUsername = appUser.getUsername();
        } else {
            // Handle the case where appUser is null, e.g., set username to an empty string
            this.currentUsername = "";
        }
    }



    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);

        if (chatMessage.getSender() != null) {
            // set user name and message timestamp
            holder.userNameTextView.setText(chatMessage.getSender().getUsername());
            // Check if the current user is the sender
            boolean isSender = isSender(chatMessage);
            Date date = chatMessage.getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            String formattedDate = sdf.format(date);
            holder.dateTextView.setText(formattedDate);

            // set profile image
            if (chatMessage.getSender().getProfileImageUrl() != null) {
                Glide.with(context)
                        .load(chatMessage.getSender().getProfileImageUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.profileImage);
                // Update image sent
                if (chatMessage.getImageUrl() != null) {
                    Glide.with(context)
                            .load(chatMessage.getImageUrl())
                            .into(holder.senderImageView);
                    holder.senderImageView.setVisibility(View.VISIBLE);
                } else {
                    holder.senderImageView.setVisibility(View.GONE);
                }
            }
        }

        // set message content
        if (chatMessage.getImageUrl() != null && !chatMessage.getImageUrl().isEmpty()) {
            holder.messageTextView.setVisibility(View.GONE);
            holder.messageTextContainer.setVisibility(View.GONE);
            Glide.with(context).load(chatMessage.getImageUrl()).into(holder.senderImageView);
            holder.imageCardView.setVisibility(View.VISIBLE);
        } else {
            holder.messageTextView.setText(chatMessage.getMessage());
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.messageTextContainer.setVisibility(View.VISIBLE);
            holder.senderImageView.setVisibility(View.GONE); // Add this line
            holder.imageCardView.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }
    private boolean isSender(ChatMessage chatMessage) {
        if (chatMessage == null || chatMessage.getSender() == null) {
            return false;
        }
        return currentUsername.equals(chatMessage.getSender().getUsername());
    }

}
