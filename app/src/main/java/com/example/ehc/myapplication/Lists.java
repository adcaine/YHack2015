package com.example.ehc.myapplication;

import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ListActivity;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;



public class Lists extends ListActivity {

    DBHandler dbHandler;
    private static ArrayList<String> listItems= new ArrayList<>();
    ArrayAdapter<String> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new DBHandler(this, null, null, 1);
        setContentView(R.layout.activity_lists);
        getListView().setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int i, long l) {


            }
        });



        listItems = dbHandler.toArray();
        Toast.makeText(this.getApplicationContext(), listItems.get(0).toString(),Toast.LENGTH_SHORT).show();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();


    }
    @Override
    public void onResume(){
        super.onResume();

    }

    public static ArrayList getListItems(){
        return listItems;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
//    public void sendMessage(View view){
//        Intent intent = new Intent(this, Add.class);
//        startActivity(intent);
//
//    }


}
