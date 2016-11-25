package com.coderschool.booketplace.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coderschool.booketplace.BaseFragmemt;
import com.coderschool.booketplace.R;
import com.coderschool.booketplace.activities.ChatActivity;
import com.coderschool.booketplace.api.FirebaseApi;
import com.coderschool.booketplace.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dattran on 11/18/16.
 */

public class UserProfileFragment extends BaseFragmemt {
    private static final String UID = "uid";

    public static UserProfileFragment newInstance(String uid) {

        Bundle args = new Bundle();
        args.putString(UID, uid);

        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.ivProfile)
    ImageView ivProfile;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvLocation)
    TextView tvLocation;
    @BindView(R.id.ivProfileBackground)
    ImageView ivProfileBackground;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btnFollow)
    Button btnFollow;

    boolean isFollowing;
    String userFollowingKey;
    DatabaseReference mFollowingDatabaseRef;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setupUser();
    }

    private void setupUser() {
        String uid = getArguments().getString(UID);
        ratingBar.setNumStars(5);
        FirebaseApi.getInstance().getUserDatabaseRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(mActivity).load(user.getAvatar()).into(ivProfile);
                Picasso.with(mActivity).load(user.getAvatar()).into(ivProfileBackground);
                tvName.setText(user.getName());
                tvLocation.setText(user.getLocation());
                ratingBar.setRating(user.getRatingOverall());


                // check following state
                String ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                mFollowingDatabaseRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("users-followings")
                        .child(ownerUid);
                mFollowingDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot pref : dataSnapshot.getChildren()) {
                            String userUid = pref.child("uid").getValue().toString();
                            if (user.getUid().equals(userUid)) {
                                isFollowing = true;
                                userFollowingKey = pref.getKey();
                                btnFollow.setText("Following");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.containder, UserBookStreamFragment.newInstance(uid), UID)
                .commit();
    }




    @OnClick(R.id.btnFollow)
    public void onFollow() {
        Toast.makeText(mActivity, "follow", Toast.LENGTH_SHORT).show();

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String friendUid = getArguments().getString(UID);

        mFollowingDatabaseRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users-followings")
                .child(ownerUid);

        // for following
        if (!isFollowing) {
            userFollowingKey = mFollowingDatabaseRef.push().getKey();
            Map<String, String> newFollowing = new HashMap<>();
            newFollowing.put("uid", friendUid);
            Map<String, Object> childUpdate = new HashMap<>();
            childUpdate.put(userFollowingKey, newFollowing);
            mFollowingDatabaseRef.updateChildren(childUpdate);
            isFollowing = true;
            btnFollow.setText("Following");
        } else {
            mFollowingDatabaseRef.child(userFollowingKey).removeValue();
            isFollowing = false;
            btnFollow.setText("Follow");
        }
    }

    @OnClick(R.id.btnChat)
    public void onChat() {
        String uid = getArguments().getString(UID);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("chat", uid);
        getContext().startActivity(intent);
    }

}
