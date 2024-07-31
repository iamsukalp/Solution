package com.appsters.flexx.Fragments;

import android.animation.Animator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.appsters.flexx.Adapters.UsersAdapter;
import com.appsters.flexx.Model.UsersModel;
import com.appsters.flexx.R;
import com.appsters.flexx.databinding.FragmentUsersBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.appsters.flexx.Base.Flexx.userRef;


public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private UsersAdapter adapter;
    private List<UsersModel> userList=new ArrayList<>();
    private List<String> userKey=new ArrayList<>();
    private List<UsersModel>userAll=new ArrayList<>();
    private Query query;
    private LinearLayoutManager linearLayoutManager;
    public static ViewSwitcher viewSwitcher;


    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentUsersBinding.inflate(inflater,container,false);
        init();
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getUserList();

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.usersViewSwitcher.setDisplayedChild(1);
            }
        },1500);



    }

    private void init() {

        // recycler view setup

        viewSwitcher=binding.usersViewSwitcher;

        linearLayoutManager=new LinearLayoutManager(requireActivity());
        linearLayoutManager.isAutoMeasureEnabled();
        linearLayoutManager.setSmoothScrollbarEnabled(true);

        binding.usersList.setHasFixedSize(true);
        binding.usersList.setLayoutManager(linearLayoutManager);

        adapter=new UsersAdapter(userList,userKey,userAll,requireActivity());
        binding.usersList.setAdapter(adapter);

        // query
        userRef.keepSynced(true);
        query=userRef.orderByChild("name");

        customSearchView();
    }
    private void getUserList() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                userKey.clear();
                userList.clear();
                userAll.clear();

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    userKey.add(dataSnapshot.getKey());
                    UsersModel model=dataSnapshot.getValue(UsersModel.class);
                    userList.add(model);
                    userAll.add(model);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    private void customSearchView() {

        ImageView searchViewIcon = (ImageView)binding.usersSearchView.findViewById(R.id.search_mag_icon);

        //Get parent of gathered icon
        ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
        //Remove it from the left...
        linearLayoutSearchView.removeView(searchViewIcon);
        //then put it back (to the right by default)
        linearLayoutSearchView.addView(searchViewIcon);
        EditText searchEditText = binding.usersSearchView.findViewById(R.id.search_src_text);
        searchEditText.setHint("Search");
        searchEditText.setHintTextColor(getResources().getColor(R.color.primary_text,null));
        searchEditText.setTextColor(getResources().getColor(R.color.primary_text,null));
        ImageView imvClose = binding.usersSearchView.findViewById(R.id.search_close_btn);
        imvClose.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_close_color));


        binding.usersSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

    }

}