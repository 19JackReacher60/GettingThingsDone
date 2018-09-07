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

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;

import Categorised.Items.Item;
import recycler.view.ItemRecyclerView;

public class ScheduledActivity extends AppCompatActivity {

    private RecyclerView recyclerView;              //recyclerview for items list
    private ItemRecyclerView adapter;           //adapter for the recycler view
    private RecyclerView.LayoutManager layoutManager;   //layout style that will be used of the recyclerview
    private ArrayList<Item> Items;              //ArrayList for Items
    private ArrayList<Item> logged;             //ArrayList for logged items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled);

        //set title of the activity
        setTitle("Scheduled Actions");

        //disable orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //instantiate logged
        logged = new ArrayList<Item>();

        //load items from storage
        loadData();

        //check if any items in scheduled need to be moved to today
        checkScheduled();

        //Build the recycler view;
        buildRecyclerView();


    }

    @Override
    public void onResume()
    {
        super.onResume();

        //Refresh list
        loadData();

        //check if any items in scheduled need to be moved to today
        checkScheduled();

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
            startActivity(new Intent(ScheduledActivity.this, NewItemActivity.class));
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
        String json = sharedPreferences.getString("Scheduled List", null);
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
        editor.putString("Scheduled List", json);
        editor.apply();

        if(logged.size() > 0){
            for(Item i: logged){
                Items.remove(i);
            }
        }
    }

    /**
     * check if any items in the scheduled category need to be moved  to the today category
     */
    private void checkScheduled(){
        ArrayList<Item> scheduled = loadcategory("Scheduled List");
        ArrayList<Item> scheduled2 = new ArrayList<Item>();

        ArrayList<Item> Today = loadcategory("Today List");

        if(scheduled != null){
            for(Item i: scheduled){
                if((new DateTime(i.getDate()).isBeforeNow()) || (new DateTime(i.getDate()).isEqualNow())){
                    Today.add(i);
                }else
                {
                    scheduled2.add(i);
                }
            }
            saveCategory("Scheduled List", scheduled2);
            saveCategory("Today List", Today);
        }
    }

    /**
     * load a categorys items from shared preferences
     * @param cat to be loaded
     * @return category list
     */
    private ArrayList<Item> loadcategory(String cat){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();

        //load category items from shared preference
        String json = sharedPreferences.getString(cat, null);
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        ArrayList<Item> temp = gson.fromJson(json, type);
        return temp;
    }

    /**
     * Used to save changes of the item
     * @param cat   category of item
     * @param sList New list
     */
    private void saveCategory(String cat, ArrayList<Item> sList){
        SharedPreferences sharedPreferences2 = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences2.edit();
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(sList);
        editor.putString(cat, json2);
        editor.apply();

    }
}
