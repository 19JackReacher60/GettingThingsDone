package com.example.aljaki.gtd3;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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

public class EditItemActivity extends AppCompatActivity {

    private Item item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        //disable orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //set spinner
        setSPinner();

        //set activty title to Edit Item
        setTitle("Edit Item");

        //set the datepicker
        setdatepicker();

        //set views values based on intent data
        setFields();

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

        //menu item click hanlder
        if(id == R.id.optionsave)
        {
            savechanges();
            this.finish();
        }

        //menu item click hanlder
        if(id == R.id.optioncancel)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void setFields(){

        //get transferred item on intent
        Intent intent = getIntent();
        item = (Item) intent.getParcelableExtra("Item");

        //get views
        EditText etTitle = findViewById(R.id.txtTitle);
        EditText etDescription = findViewById(R.id.txtDescription);
        Spinner spCategory = findViewById(R.id.spCategories);

        //set view values
        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());
        spCategory.setSelection(item.getCategory());

        //Add date if item is scheduled and date is not null
        if(item.getCategory() == 3 && item.getDate() != null){
            TextView txtDate = findViewById(R.id.txtDate);

            //get scheduled date time of item and set it in the view
            DateTime d = new DateTime(item.getDate());
            txtDate.setText(d.getDayOfMonth() + "/" + d.getMonthOfYear() + "/" + d.getYear());
        }
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

    /**
     * Load stored category lists
     * @param cat category of editted item
     * @return list of the old item
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

    private void savechanges(){

        String cat = getCategory(item);     // get string category of the item

        ArrayList<Item> temp = loadcategory(cat);
        Item editted = null;    //used to keep track of the editted item
        Item remove = null;     //used to keep track of the old item

        //get views
        EditText etTitle = findViewById(R.id.txtTitle);
        EditText etDescription = findViewById(R.id.txtDescription);
        Spinner spCategory = findViewById(R.id.spCategories);

        //apply changes
        for(Item s: temp){
            if(s.getTitle().equals(item.getTitle())){
                remove = s;
                s.setTitle(etTitle.getText().toString());
                s.setDescription(etDescription.getText().toString());
                s.setCategory(spCategory.getSelectedItemPosition());
                if(s.getCategory() == 3){

                    //creater format for string to datetime conversion
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");

                    //get date holder
                    EditText etdate = (EditText) findViewById(R.id.txtDate);

                    //Convert string to datetime
                    DateTime dt = formatter.parseDateTime(etdate.getText().toString());

                    s.setDate(dt.getMillis());
                }else{
                    s.setDate(null);
                }
                editted = s;
            }
        }

        //check if the user selected today's date or a date before today and send it to the today category
        if(editted.getCategory() == 3 && (new DateTime(editted.getDate()).isEqualNow() || new DateTime(editted.getDate()).isBeforeNow())){
            editted.setCategory(1);
        }


        //save to the current list if the user didn't change the category or else add the item to the new category and save
        if(editted.getCategory() == item.getCategory()){
            saveCategory(getCategory(item), temp);
        }else{
            ArrayList<Item> newCatList = loadcategory(getCategory(editted));
            if(newCatList == null)
            {
                newCatList = new ArrayList<>();
            }
                newCatList.add(editted);
                saveCategory(getCategory(editted), newCatList);
                temp.remove(remove);
                saveCategory(getCategory(item), temp);
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
                    //do nothing
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

                    //creater format for string to datetime conversion
                    DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");

                    //Convert string to datetime
                    DateTime dt = new DateTime(item.getDate());

                    //get integer values of date
                    int year = dt.getYear();
                    int month = dt.getMonthOfYear();
                    int day = dt.getDayOfMonth();

                    String strdate = Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year);
                    date.setText(strdate);

                    //create and display datepicker
                    DatePickerDialog datepicker = new DatePickerDialog(EditItemActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_MinWidth, mDateSetListener, year, month- 1, day);
                    datepicker.show();
                }
                return true;
            }
        });
    }
}
