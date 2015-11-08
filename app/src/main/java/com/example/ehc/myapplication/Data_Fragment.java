package com.example.ehc.myapplication;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Data_Fragment extends ListFragment {
    DBHandler dbHandler;
    private static ArrayList<String> listItems= new ArrayList<>();
    ArrayAdapter<String> adapter;


//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        listItems = Lists.getListItems();
//        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listItems );
//        setListAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        getActivity().setContentView(R.layout.data_fragment);
//
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHandler = new DBHandler(this.getActivity(), null, null, 1);
        View v = inflater.inflate(R.layout.data_fragment,container,false);
//        v.findViewById(R.id.listView);
        listItems = dbHandler.toArray();
        adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, listItems );
        setListAdapter(adapter);
//        adapter.notifyDataSetChanged();
//        v.setAdapter(adapter);
        return v;
    }
}




