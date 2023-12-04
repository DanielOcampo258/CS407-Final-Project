package com.cs407.landmarkapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsFragment extends Fragment {
    private List<User> userFriends = new ArrayList<>();
    AppDatabase appDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        appDatabase = AppDatabase.getInstance(this.getContext());

        /* Since the LiveData Object that is returned, gets returned Asynchronously, we need an observer
        to watch for when it does eventually resolve into the User object.
         */
        appDatabase.userDao().getUserById(getUserIdFromPrefs()).observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if(user == null) return;

                if ((user.getFriends() == null || user.getFriends().size() == 0) && userFriends.isEmpty()) {
                    generateTestFriends();
                } else {
                    for (int friendId : user.getFriends()) {
                        appDatabase.userDao().getUserById(friendId).observe(getViewLifecycleOwner(), new Observer<User>() {
                            @Override
                            public void onChanged(User friend) {
                                userFriends.add(friend);
                            }
                        });
                    }
                }

                displayFriends(userFriends);
            }
        });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    /**
     * Method to display the list of friends passed as arguments. Then inserted into the friends list
     * view.
     * @param friendsToDisplay
     */
    private void displayFriends(List<User> friendsToDisplay) {

        ArrayAdapter arrayAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1,
                friendsToDisplay.stream().map(friend -> friend.getUsername()).collect(Collectors.toList()));

        ListView friendsListView = getView().findViewById(R.id.friendsListView);

        friendsListView.setAdapter(arrayAdapter);

    }

    private int getUserIdFromPrefs() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1);
    }

    private void generateTestFriends() {
        String[] testFriendUserNames = {"James", "David", "Tara", "Lee", "Sam"};

        for (int i = 0; i < testFriendUserNames.length; i++) {
            User userFriend = new User(testFriendUserNames[i],
                    "123", testFriendUserNames[i] + i + "@gmail.com",
                    new ArrayList<>(), new ArrayList<>());
            userFriends.add(userFriend);
        }

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SearchView friendSearchView = view.findViewById(R.id.searchView);

        /*
         Handles search logic
         Everytime input changes, adjust the friends the display by filtering by search input change
         */
        friendSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                displayFriends(userFriends.stream()
                        .filter(friend -> friend.getUsername().toLowerCase().contains(newText.toLowerCase()))
                        .collect(Collectors.toList()));
                return true;
            }
        });

    }
}