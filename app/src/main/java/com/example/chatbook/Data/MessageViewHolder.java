package com.example.chatbook.Data;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatbook.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageViewHolder extends RecyclerView.ViewHolder {


    private final int colorCurrentUser;
    private final int colorRemoteUser;

    public CardView imageCardView;


    TextView messageTextView, dateTextView,userNameTextView;

    ImageView profileImage,senderImageView;

    LinearLayout messageTextContainer,profileContainer,messageContainer;


    private boolean isSender;

    public MessageViewHolder(@NonNull View itemView, boolean isSender) {
        super(itemView);
        this.isSender = isSender;


        RecyclerView recyclerView = itemView.findViewById(R.id.recycler_open_channel_chat);
        messageTextView = itemView.findViewById(R.id.messageTextView);
        userNameTextView = itemView.findViewById(R.id.userNameTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        profileImage = itemView.findViewById(R.id.profileImage);
        senderImageView = itemView.findViewById(R.id.senderImageView);
        messageTextContainer = itemView.findViewById(R.id.messageTextContainer);
        profileContainer = itemView.findViewById(R.id.profileContainer);
        messageContainer = itemView.findViewById(R.id.messageContainer);
        imageCardView = itemView.findViewById(R.id.imageCardView);


        // Setup default colros
        colorCurrentUser = ContextCompat.getColor(itemView.getContext(), R.color.colorAccent);
        colorRemoteUser = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary);
    }

    public void updateWithMessage(ChatMessage message, RequestManager glide) {

        // Update message
        messageTextView.setText(message.getMessage());
        messageTextView.setTextAlignment(isSender ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);

        userNameTextView.setText(message.getSender().getUsername());



        // Update date
        if (message.getTimestamp() != null)
            dateTextView.setText(this.convertDateToHour(message.getTimestamp()));


        // Update profile picture
        if (message.getSender().getProfileImageUrl() != null) {
            glide.load(message.getSender().getProfileImageUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_anon_user_48dp);
        }


        // Update image sent
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            glide.load(message.getImageUrl())
                    .into(senderImageView);
            senderImageView.setVisibility(View.VISIBLE);
        } else {
            senderImageView.setVisibility(View.GONE);
        }


        updateLayoutFromSenderType();
    }

    private void updateLayoutFromSenderType() {

        //Update Message Bubble Color Background
        ((GradientDrawable) messageTextContainer.getBackground()).setColor(isSender ? colorCurrentUser : colorRemoteUser);
        messageTextContainer.requestLayout();

        ConstraintLayout.LayoutParams userNameTextViewParams = (ConstraintLayout.LayoutParams) userNameTextView.getLayoutParams();
        userNameTextView.setLayoutParams(userNameTextViewParams);
        userNameTextViewParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        userNameTextViewParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
        userNameTextViewParams.startToEnd = profileContainer.getId();
        userNameTextViewParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        if (!isSender) {
            updateProfileContainer();
            updateMessageContainer();
        }
    }

    private void updateProfileContainer() {
        // Update the constraint for the profile container (Push it to the left for receiver message)
        ConstraintLayout.LayoutParams profileContainerLayoutParams = (ConstraintLayout.LayoutParams) profileContainer.getLayoutParams();
        profileContainerLayoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
        profileContainerLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        profileContainer.requestLayout();
    }

    private void updateMessageContainer() {
        // Update the constraint for the message container (Push it to the right of the profile container for receiver message)
        ConstraintLayout.LayoutParams messageContainerLayoutParams = (ConstraintLayout.LayoutParams) messageContainer.getLayoutParams();
        messageContainerLayoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
        messageContainerLayoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
        messageContainerLayoutParams.startToEnd = profileContainer.getId();
        messageContainerLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        messageContainerLayoutParams.horizontalBias = 0.0f;
        messageContainer.requestLayout();

        // Update the constraint (gravity) for the text of the message (content + date) (Align it to the left for receiver message)
        LinearLayout.LayoutParams messageTextLayoutParams = (LinearLayout.LayoutParams) messageTextContainer.getLayoutParams();
        messageTextLayoutParams.gravity = Gravity.START;
        messageTextContainer.requestLayout();

        LinearLayout.LayoutParams dateLayoutParams = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        dateLayoutParams.gravity = Gravity.BOTTOM | Gravity.START;
        dateTextView.requestLayout();

    }

    private String convertDateToHour(Date date) {
        DateFormat dfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dfTime.format(date);
    }

}