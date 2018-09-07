package com.example.aljaki.gtd3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import Categorised.Items.Item;

public class NewItemActivity extends AppCompatActivity {

    private ArrayList<Item> itemList;
   // private ArrayList<>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        itemList = new ArrayList<Item>();

        //set activity title
        setTitle("Add New Item");

        //disable orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //set spinner dropdown items
        setSPinner();

        //set date picker
        setdatepicker();
    }

    private void saveData(int category){

        //load stored items into array
        loadData(category);

        //get item information from views
        EditText title = (EditText) findViewById(R.id.txtTitle);
        EditText description = (EditText) findViewById(R.id.txtDescription);

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        if(category == 3)
        {
            //creater format for string to datetime conversion
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");

            //get date holder
            EditText etdate = (EditText) findViewById(R.id.txtDate);

            //Convert string to datetime
            DateTime dt = formatter.parseDateTime(etdate.getText().toString());

            //Add scheduled item information to list
            itemList.add(new Item(title.getText().toString(), description.getText().toString(), category, dt.getMillis(), false, false));
            String json = gson.toJson(itemList);
            editor.putString("Scheduled List", json);
        }
        if(category == 0){

            //add inbasket item to items list
            itemList.add(new Item(title.getText().toString(), description.getText().toString(), category, null,  false, false));
            String json = gson.toJson(itemList);
            editor.putString("Inbasket List", json);
        }
        if(category == 1){
            //add today item to items list
            itemList.add(new Item(title.getText().toString(), description.getText().toString(), category, null,  false, false));
            String json = gson.toJson(itemList);
            editor.putString("Today List", json);
        }
        if(category == 2){
            //add Next item to items list
            itemList.add(new Item(title.getText().toString(), description.getText().toString(), category, null,  false, false));
            String json = gson.toJson(itemList);
            editor.putString("Next List", json);
        }
        if(category == 4){
            //add scheduled item to items list
            itemList.add(new Item(title.getText().toString(), description.getText().toString(), category, null,  false, false));
            String json = gson.toJson(itemList);
            editor.putString("Someday List", json);
        }
        editor.apply();
    }

    private void loadData(int category){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = null;

        //load from inbasket list
        if(category == 0){
            json = sharedPreferences.getString("Inbasket List", null);
        }
        //load from Todays list
        if(category == 1){
            json = sharedPreferences.getString("Today List", null);
        }
        //load from Next list
        if(category == 2){
            json = sharedPreferences.getString("Next List", null);
        }
        //load from Scheduled list
        if(category == 3){
            json = sharedPreferences.getString("Scheduled List", null);
        }
        //load from Someday list
        if(category == 4){
            json = sharedPreferences.getString("Someday List", null);
        }
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        itemList = gson.fromJson(json, type);

        //Create new array if there is no data
        if (itemList == null){
            itemList = new ArrayList<Item>();
        }
    }

    private void setSPinner(){

        //get spinner from the xml.
        final Spinner dropdown = findViewById(R.id.spCategories);
        //create a list of categories for the spinner.
        String[] categories = new String[]{"In Basket", "Today", "Next", "Scheduled", "Someday"};
        //create an adapter to describe how the categories are displayed.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        //Listen for event to display the correct view for category
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int category, long l) {

                //In Basket, Today, Next, Someday category selected
                if(category == 0 || category == 1 || category == 2 || category == 4)
                {
                    //hide unrelated details
                    findViewById(R.id.txtDate).setVisibility(View.INVISIBLE);
                    findViewById(R.id.lbldate).setVisibility(View.INVISIBLE);
                }

                //Scheduled cateogry selected
                if(category == 3)
                {
                    //display additional details
                    findViewById(R.id.txtDate).setVisibility(View.VISIBLE);
                    findViewById(R.id.lbldate).setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void setdatepicker() {
        final TextView date = findViewById(R.id.txtDate);
        final DatePickerDialog.OnDateSetListener mDateSetListener;
        date.setInputType(InputType.TYPE_NULL);                     // disable soft

        Calendar calendar = Calendar.getInstance();

        //get current date to point datepicker to todays date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String strdate = Integer.toString(day) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(year);
        date.setText(strdate);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker picker, int year, int month, int day) {
                //Increment month as the counter starts at zero
                month++;
                //Set textview to selected date
                String strdate = day + "/"+ month + "/" + year;
                date.setText(strdate);
            }
        };

        //check when user clicks date textview
        date.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                //Only activate on press and not release
                if (motionEvent.getAction() == 0) {
                    Calendar calendar = Calendar.getInstance();

                    //get current date to point datepicker to todays date
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    String strdate = Integer.toString(day) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(year);
                    date.setText(strdate);

                            //create and display datepicker
                    DatePickerDialog datepicker = new DatePickerDialog(NewItemActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_MinWidth, mDateSetListener, year, month, day);
                    datepicker.show();
                }
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //inflate menu
        getMenuInflater().inflate(R.menu.edit_add_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Spinner dropdown = findViewById(R.id.spCategories);

        if(id == R.id.optionsave){
            //save added item to the items list
            saveData(dropdown.getSelectedItemPosition());

            //reset values
            ((EditText)findViewById(R.id.txtTitle)).setText("");
            ((EditText)findViewById(R.id.txtDescription)).setText("");
            ((EditText)findViewById(R.id.txtDate)).setText("");

            Toast.makeText(getApplicationContext(), "Item Has Been Added", Toast.LENGTH_LONG).show();
        }

        if(id == R.id.optioncancel){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
