package com.example.aljaki.gtd3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import Categorised.Items.Item;
import recycler.view.ItemRecyclerView;

public class TodayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;              //recyclerview for items list
    private ItemRecyclerView adapter;           //adapter for the recycler view
    private RecyclerView.LayoutManager layoutManager;   //layout style that will be used of the recyclerview
    private ArrayList<Item> Items;              //ArrayList for today Items
    private ArrayList<Item> logged;             //ArrayList for logged items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        //set activty title to Todays action
        setTitle("Today's Actions");

        //disable orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //instantiate logged
        logged = new ArrayList<Item>();

        //load items from storage
        loadData();

        //Build the recycler view;
        buildRecyclerView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        //Refresh list
        loadData();
        buildRecyclerView();
    }

    private Intent sendToEditActivity(int position){

        //get the item clicked on
        Item item = Items.get(position);

        //create intent from inasket activity to the edit activity
        Intent intent = new Intent(this, EditItemActivity.class);

        //Add the item to the intent
        intent.putExtra("Item", item);
        return intent;
    }

    private void removeItem(int position){
        Item i = Items.get(position);
        //Send item to logged list if it has been completed else delete the item completely
        if(i.isCompleted()){
            i.setLogged(true);
            logged.add(i);
        }
        Items.remove(i);
        //save and update adaptor
        adapter.notifyItemRemoved(position);
        saveData();
    }

    private void setCompletion(int position, boolean completed){
        //change completed and notify and update adaptor
        Items.get(position).changeCompleted();
        adapter.notifyItemChanged(position);
    }

    private void buildRecyclerView(){
        //Initialise the recycler view, layout manager, adapter
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);     //keep recycler view size the same regardless of the number of items
        layoutManager = new LinearLayoutManager(this);
        adapter = new ItemRecyclerView(Items);

        //set the layout manage and the adapter for the recycler view
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //add listener to items to go to the edit item activity
        adapter.setOnItemClickListener(new ItemRecyclerView.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //send items data to the editActivity
                //Go to EditItem activity with item
                startActivity(sendToEditActivity(position));
            }
            @Override
            public void onDeleteClick(int position) {
                //remove item from list
                removeItem(position);
                //save changes
                saveData();
            }
            @Override
            public void onCheckClick(int position) {
                //set item completed/uncompleted
                CheckBox cb = (CheckBox) findViewById(R.id.rvCompleted);
                setCompletion(position, cb.isChecked());
                //save changes
                saveData();
            }
            @Override
            public void onUndoClick(int position) {
                //do nothing
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate menu
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //menu item click hanlder
        if(id == R.id.optionNewItem)
        {
            //Go to new item activity
            startActivity(new Intent(TodayActivity.this, NewItemActivity.class));
        }

        //menu item click hanlder
        if(id == R.id.optionclear)
        {
            //log the completed items
            logCompleted();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * move completed items to log if user selects clear completed
     */
    private void logCompleted(){
        for(Item i: Items){
            if(i.isCompleted()){
                i.setLogged(true);
            }
        }
        saveData();
        adapter.notifyDataSetChanged();
        loadData();
        buildRecyclerView();
    }

    /**
     * load all the items from shared preferences
     */
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Today List", null);
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        ArrayList<Item> temp = gson.fromJson(json, type);
        Items = new ArrayList<Item>();
        logged = new ArrayList<Item>();

        //Create new array if there is no data
        if (temp == null){
            temp = new ArrayList<Item>();
        }else
        {
            for(Item i: temp){
                if(!i.isLogged()){
                    Items.add(i);
                }else{
                    logged.add(i);
                }
            }
        }
    }

    private void saveData(){

        if(logged.size() > 0){
            for(Item i: logged){
                Items.add(i);
            }
        }

        //save any new changes user has made
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(Items);
        editor.putString("Today List", json);
        editor.apply();

        if(logged.size() > 0){
            for(Item i: logged){
                Items.remove(i);
            }
        }
    }
}
