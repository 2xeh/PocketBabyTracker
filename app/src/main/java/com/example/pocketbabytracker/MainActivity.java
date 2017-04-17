package com.example.pocketbabytracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "Andrea-Main";
    ListView lvMainMenu;
    TextView tvSelectedBaby;
    private static final int MENU_FIRST = Menu.FIRST;
    private DatabaseQuery databaseQuery;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "get the shared preferences");
        sharedPreferences = this.getSharedPreferences("pocketBaby", this.MODE_PRIVATE);

        lvMainMenu = (ListView) findViewById(R.id.lvMainMenu);
        tvSelectedBaby = (TextView) findViewById(R.id.tvSelectedBaby);

        Log.d(TAG, "Getting child from preferences and setting on screen");
        tvSelectedBaby.setText("Selected child: " + sharedPreferences.getString("babyName", ""));

        // set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        // Let's fill that list view
        final ArrayList<MainActivityMenuOptions> menuItems = new ArrayList<MainActivityMenuOptions>();

        menuItems.add(new MainActivityMenuOptions("Baby Info", new Intent(this, BabyInfoActivity.class)));
        menuItems.add(new MainActivityMenuOptions("Feeding", new Intent(this, FeedingActivity.class)));
        menuItems.add(new MainActivityMenuOptions("Export", new Intent(this, ExportActivity.class)));

        // Initialize the adapter
        MainActivityMenuOptionsAdapter adapter = new MainActivityMenuOptionsAdapter(this, R.layout.menu_list, menuItems);
        lvMainMenu.setAdapter(adapter);


        //have listview respond to selected items
        lvMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // use int i to get the item out of the list?
                Intent intent = menuItems.get(i).getMenuIntent();
                startActivity(intent);
            }
        });


    } // end of onCreate()



    private class MainActivityMenuOptionsAdapter extends ArrayAdapter<MainActivityMenuOptions> {
        private ArrayList<MainActivityMenuOptions> items;

        public MainActivityMenuOptionsAdapter(Context context, int textViewResourceId, ArrayList<MainActivityMenuOptions> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        // get the views for each item in the array list of MainActivityMenuOptions
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.menu_list, null);
            }

            MainActivityMenuOptions menuItem = items.get(position);

            if (menuItem != null) {
                TextView header = (TextView) view.findViewById(R.id.tvMenuItem);
                header.setText(menuItem.getMenuDescription());
            }
            return view;
        }
    }

    // class to define properties of menu items
    class MainActivityMenuOptions {
        private String menuDescription;
        private Intent menuIntent;

        public MainActivityMenuOptions(String menuDescription, Intent menuIntent) {
            this.menuDescription = menuDescription;
            this.menuIntent = menuIntent;
        }

        public String getMenuDescription() {return this.menuDescription; }
        public void setMenuDescription(String menuDescription) { this.menuDescription = menuDescription; }
        public Intent getMenuIntent() { return this.menuIntent; }
        public void setMenuIntent(Intent menuIntent) { this.menuIntent = menuIntent; }
    }


    // Part 1 of options menu with baby names
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // let's try to get access to the menu
        int menuCounter = MENU_FIRST;

        List<BabyElements> babies = databaseQuery.getAllBabies();

        // add each baby to the menu
        for(BabyElements baby : babies){
            menu.add(0, menuCounter++, Menu.NONE, baby.getBabyName());
        }

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    // Part 2 of options menu with baby names
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        String babyName = item.getTitle().toString();

        tvSelectedBaby.setText("Selected child: " + babyName);


        // instantiate the SharedPreferences Editor and put in the new values
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("babyName", babyName);

        // commit changes made by the editor
        if (editor.commit()) {
            // update a TextView?
        } else {
            makeToast("Unable to set baby.");
        }

        return super.onOptionsItemSelected(item);
    }

    // A temporary method to reset all table data
    public void deleteAllTableData(View view){
        boolean result = databaseQuery.deleteTableData();
        Log.d(TAG, "Deleted all records from tables: " + result);
    }

    // A helper method for easy long toasts
    private void makeToast(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

}
