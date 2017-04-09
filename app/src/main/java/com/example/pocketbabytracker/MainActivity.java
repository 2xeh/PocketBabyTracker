package com.example.pocketbabytracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class MainActivity extends AppCompatActivity {

    ListView lvMainMenu;
    private static final int MENU_FIRST = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvMainMenu = (ListView) findViewById(R.id.lvMainMenu);

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

    public void startHtmlPdfRocketActivity(View view){
        startActivity(new Intent(this, HtmlPdfRocketActivity.class));
    }

    // An ArrayAdapter Blogs to use in the ListView
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

                // I feel like I should be doing something with intent here???
            }
            return view;
        }
    } // end of MainActivityMenuOptionsAdapter nested class


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
    } // end of MainActivityMenuOptions nested class


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // fill in options menu with items
//        <menu xmlns:android="http://schemas.android.com/apk/res/android"
//        xmlns:tools="http://schemas.android.com/tools"
//        xmlns:app="http://schemas.android.com/apk/res-auto"
//        tools:context=".MainActivity">
//
//    <item android:id="@+id/itBaby1"
//        android:title="baby1"
//        android:orderInCategory="100"
//        app:showAsAction="never"
//                />

        // let's try to get access to the menu
        // these don't have id's
        int menuCounter = MENU_FIRST;
        menu.add(0, menuCounter++, Menu.NONE, "baby 4");
        menu.add(0, menuCounter++, Menu.NONE, "baby 5");


        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        makeToast("clicked item id:" + id);


//        switch (id) {
//            case R.id.itSettings:
//                // some toast
//                //Toast.makeText(MainActivity.this, "Settings clicked. Coming in part b.", Toast.LENGTH_LONG).show();
//                startSettingsActivity();
//                break;
//
//            case R.id.itRefresh:
//                updateSax();
//                break;
//
//            case R.id.itCbcWhiteCoat:
//                tvRssTitle.setText(RSSFeedEnum.getTitle(1));
//                selectedFeed = CBC_WHITE_COAT_URL;
//                updateSax();
//                break;
//
//            case R.id.itCbcUnderInfluence:
//                tvRssTitle.setText(RSSFeedEnum.getTitle(2));
//                selectedFeed = CBC_UNDER_INFLUENCE_URL;
//                updateSax();
//                break;
//
//            default:
//                break;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void makeToast(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

}
