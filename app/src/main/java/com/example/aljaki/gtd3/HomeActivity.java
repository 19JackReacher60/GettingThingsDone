package com.example.aljaki.gtd3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import Categorised.Items.Item;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //set activity title
        setTitle("Getting Things Done");
        SetRows();

        //disable orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //creater format for string to datetime conversion
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");

        //Convert string to datetime
        DateTime dt = formatter.parseDateTime("24/01/2019");

        DateTime d = new DateTime(dt.getMillis());

        //check which items from scheduled need to be moved to today
        checkScheduled();
    }

    @Override
    public void onResume()
    {
        //check which items from scheduled need to be moved to today
        checkScheduled();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate menu
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //menu item click hanlder
        if(id == R.id.optionNewItem)
        {
            //Go to new item activity
            startActivity(new Intent(HomeActivity.this, NewItemActivity.class));
        }

        //menu item click hanlder
        if(id == R.id.optionexit)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //exit application
                            System.exit(0);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //Do nothing
                            break;
                    }
                }
            };

            //Dialog to ensure user wants to exit
            AlertDialog.Builder  exitDialog = new AlertDialog.Builder(HomeActivity.this);
            exitDialog.setMessage("Do you want to exit?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        }
        return super.onOptionsItemSelected(item);
    }

    //Add listeners to the table rows to direct them to the appropriate pages
    private void SetRows(){

        //get inbasket row
        TableRow trInbasket = (TableRow) findViewById(R.id.trBasket);
        trInbasket.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to InBasket activity
                startActivity(new Intent(HomeActivity.this, InBasketActivity.class));
            }
        });

        //get today row
        TableRow trToday = (TableRow) findViewById(R.id.trToday);
        trToday.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to Today activity
                startActivity(new Intent(HomeActivity.this, TodayActivity.class));
            }
        });

        TableRow trNext = (TableRow) findViewById(R.id.trNext);
        trNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to Next activity
                startActivity(new Intent(HomeActivity.this, NextActivity.class));
            }
        });
        TableRow trScheduled = (TableRow) findViewById(R.id.trScheduled);
        trScheduled.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to Scheduled activity
                startActivity(new Intent(HomeActivity.this, ScheduledActivity.class));
            }
        });
        TableRow trSomeday = (TableRow) findViewById(R.id.trSomeday);
        trSomeday.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to Someday activity
                startActivity(new Intent(HomeActivity.this, SomedayActivity.class));
            }
        });
        TableRow trlog = (TableRow) findViewById(R.id.trDone);
        trlog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to Someday activity
                startActivity(new Intent(HomeActivity.this, LogActivity.class));
            }
        });
        TableRow trinfo = (TableRow) findViewById(R.id.trInfo);
        trinfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Go to Info activity
                startActivity(new Intent(HomeActivity.this, InfoActivity.class));
            }
        });
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
     * @param cat   category of editted item
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
