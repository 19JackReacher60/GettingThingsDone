package com.example.aljaki.gtd3;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.ArrayList;

import Categorised.Items.Item;
import recycler.view.ItemRecyclerView;

public class LogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;              //recyclerview for items list
    private ItemRecyclerView adapter;           //adapter for the recycler view
    private RecyclerView.LayoutManager layoutManager;   //layout style that will be used of the recyclerview
    private ArrayList<Item> Items;              //ArrayList for someday Items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_someday);

        //set activty title to logged items
        setTitle("Logged Items");

        //disable orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

    private void removeFromCategory(int position){

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String cat = "";     //category of the item

        Item item = Items.get(position);

        //get category shared preference name
        if(item.getCategory() == 3)
        {
            cat = "Scheduled List";
        }
        if(item.getCategory() == 0){

            cat = "Inbasket List";
        }
        if(item.getCategory() == 1){
            cat = "Today List";
        }
        if(item.getCategory() == 2){
            cat = "Next List";
        }
        if(item.getCategory() == 4){
            cat = "Someday List";
        }

        //load category items from shared preference
        String json = sharedPreferences.getString(cat, null);
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        ArrayList<Item> temp = gson.fromJson(json, type);

        //remove item from current list
        Items.remove(item);
        //update adaptor
        adapter.notifyItemRemoved(position);
        Item remove = null;


        //find object in temp that needs to be removed
        for(Item s: temp){
            if(s.getTitle().equals(item.getTitle())){
                remove = s;
            }
        }

        //remove item from category
        temp.remove(remove);


        //save cleansed list to shared preferences
        SharedPreferences sharedPreferences2 = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences2.edit();
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(temp);
        editor.putString(cat, json2);
        editor.apply();
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

        //add listener to items to go to the log item activity
        adapter.setOnItemClickListener(new ItemRecyclerView.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //Do nothing
            }
            @Override
            public void onDeleteClick(int position) {
                //remove item from list and category
                removeFromCategory(position);
            }
            @Override
            public void onCheckClick(int position) {
             //Do nothing
            }
            @Override
            public void onUndoClick(int position) {
                undo(position);
            }
        });
    }

    /**
     * undo the deletion of the item from it's category
     * @param position of the item in Items
     */
    private void undo(int position){
        Item item = Items.remove(position);
        ArrayList<Item> temp = loadcategory(getCategory(item));
        Item remove = null;

        //find object in temp that needs to be removed
        for(Item s: temp){
            if(s.getTitle().equals(item.getTitle()) && s.getDescription().equals(item.getDescription())){
                remove = s;
            }
        }

        temp.remove(remove);

        //change items logged and completed
        item.setLogged(false);
        //item.changeCompleted();

        temp.add(item);
        adapter.notifyItemRemoved(position);
        saveData(getCategory(item), temp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate menu
        getMenuInflater().inflate(R.menu.log_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //menu item click hanlder
        if(id == R.id.optionclearall)
        {
            //dialog consequences
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Clear all logged items
                            ClearAll();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //Do nothing
                            break;
                    }
                }
            };

            //Dialog to ensure user wants to exit
            AlertDialog.Builder  exitDialog = new AlertDialog.Builder(LogActivity.this);
            exitDialog.setMessage("Are you sure you want to clear all completed items?").setPositiveButton("Yes!", dialogClickListener).setNegativeButton("No!", dialogClickListener).show();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * load all the items from shared preferences
     */
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<String> categories = new ArrayList<String>();

        //Items to loop through to construct Logged list
        categories.add("Inbasket List");
        categories.add("Today List");
        categories.add("Next List");
        categories.add("Scheduled List");
        categories.add("Someday List");
        Items = new ArrayList<Item>();

        if(categories != null){
            for(String s: categories){
                String json = sharedPreferences.getString(s, null);
                Type type = new TypeToken<ArrayList<Item>>(){}.getType();
                ArrayList<Item> temp = gson.fromJson(json, type);

                if(temp != null)
                {
                    for(Item i: temp){
                        if(i.isLogged()){
                            Items.add(i);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param category the category to save to
     * @param sItems the items that need to be saved
     */
    private void saveData(String category, ArrayList<Item> sItems){
        //save any new changes user has made
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(sItems);
        editor.putString(category, json);
        editor.apply();
    }


    /**
     * Go through each category and completely remove logged items
     */
    private void ClearAll(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<String> categories = new ArrayList<String>();


        //Items to loop through to construct list
        categories.add("Inbasket List");
        categories.add("Today List");
        categories.add("Next List");
        categories.add("Scheduled List");
        categories.add("Someday List");

        for(String s: categories) {
            String json = sharedPreferences.getString(s, null);
            Type type = new TypeToken<ArrayList<Item>>() {}.getType();
            ArrayList<Item> temp = gson.fromJson(json, type);
            //will be used to save lists cleansed of logged items
            ArrayList<Item> cleanlist = new ArrayList<Item>();
            for (Item i : temp) {
                if(!i.isLogged()){
                    cleanlist.add(i);
                }
            }
            saveData(s,cleanlist);
        }
        loadData();
        buildRecyclerView();
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
    private String getCategory(Item item1){
        //get category shared preference name
        if(item1.getCategory() == 3)
        {
            return "Scheduled List";
        }
        if(item1.getCategory() == 0){

            return "Inbasket List";
        }
        if(item1.getCategory() == 1){
            return "Today List";
        }
        if(item1.getCategory() == 2){
            return "Next List";
        }
        if(item1.getCategory() == 4){
            return "Someday List";
        }
        return null;
    }
}
