package com.muathaj_salahj.boxingking.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.muathaj_salahj.boxingking.Objects.User;
import com.muathaj_salahj.boxingking.R;

import java.util.ArrayList;

public class FollowsListAdapter extends ArrayAdapter<User> {

    // constructor for creating the customAdapter and sending the info to the super
    public FollowsListAdapter(Context context, ArrayList<User> users) {
        super(context, R.layout.follows_list_item, users);
    }

    // function for filling the listview items with the data according the the structure of the custom view/item
    @SuppressLint("ViewHolder")
    public View getView(int position, View view, ViewGroup parent) {

        User user = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View followItemView = inflater.inflate(R.layout.follows_list_item, parent, false);

        ((TextView) followItemView.findViewById(R.id.nameTVId)).setText(user.getName());
        ((TextView) followItemView.findViewById(R.id.emailTVId)).setText(user.getEmail());
        ((TextView) followItemView.findViewById(R.id.scoreTVId)).setText(String.format("%.2f", user.getScore()));

        return followItemView;
    }
}
