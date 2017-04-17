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
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BabyInfoActivity extends AppCompatActivity {

    private final String TAG = "Andrea-BabyInfo";
    private DatabaseQuery databaseQuery;
    private ListView lvBabyInfoBabies, lvBabyInfoControls;
    private TextView tvBabyInfoSelected;
    private SharedPreferences sharedPreferences;
    BabyInfoMenuOptionsAdapter babyAdapter;
    private static final int MENU_FIRST = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "creating....");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_info);

        Log.d(TAG, "get the shared preferences");
        sharedPreferences = this.getSharedPreferences("pocketBaby", this.MODE_PRIVATE);

        Log.d(TAG, "getting list views");
        lvBabyInfoBabies = (ListView) findViewById(R.id.lvBabyInfoBabies);
        lvBabyInfoControls = (ListView) findViewById(R.id.lvBabyInfoControls);
        tvBabyInfoSelected = (TextView) findViewById(R.id.tvBabyInfoSelected);

        Log.d(TAG, "Getting child from preferences and setting on screen");
        tvBabyInfoSelected.setText("Selected child: " + sharedPreferences.getString("babyName", ""));

        Log.d(TAG, "set the databaseQuery");
        databaseQuery = new DatabaseQuery(this);



        // CONTROLS LIST
        // Let's fill that list view
        final ArrayList<ControlsMenuOptions> menuItems = new ArrayList<ControlsMenuOptions>();

        menuItems.add(new ControlsMenuOptions("Add Child", new Intent(this, AddChildActivity.class)));
        menuItems.add(new ControlsMenuOptions("Back", new Intent(this, MainActivity.class)));

        // Initialize the adapter
        ControlsMenuOptionsAdapter adapter = new ControlsMenuOptionsAdapter(this, R.layout.menu_list, menuItems);
        lvBabyInfoControls.setAdapter(adapter);

        //have listview respond to selected items
        lvBabyInfoControls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // use int i to get the item out of the list?
                Intent intent = menuItems.get(i).getMenuIntent();
                startActivity(intent);
            }
        });


        // BABIES LIST
        // Let's fill that list view
        // TODO: wrap this in a try catch. on catch, just hide this listview.
        final ArrayList<BabyElements> babyItems = (ArrayList<BabyElements>) databaseQuery.getAllBabies();

        // Initialize the adapter for Babies List
        babyAdapter = new BabyInfoMenuOptionsAdapter(this, R.layout.babies_menu_list, babyItems);
        lvBabyInfoBabies.setAdapter(babyAdapter);


        //have listview respond to selected items
        lvBabyInfoBabies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedBaby = babyItems.get(i).getBabyName();
                tvBabyInfoSelected.setText("Selected child: " + selectedBaby);
                saveToSharedPreferences(selectedBaby);
            }
        });

        Log.d(TAG, "completed setting lists");

        // and set the Dynamic Height of the two lists
        ListUtils.setDynamicHeight(lvBabyInfoControls);
        ListUtils.setDynamicHeight(lvBabyInfoBabies);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // refresh the list of babies
        babyAdapter.notifyDataSetChanged();

    }

    private void saveToSharedPreferences(String babyName) {

        // instantiate the SharedPreferences Editor and put in the new values
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("babyName", babyName);

        // commit changes made by the editor
        if (editor.commit()) {
            // update a TextView?
        } else {
            makeToast("Unable to set baby.");
        }

    }


    // ListUtils thanks to StackOverflow user Hiren Patel
    // http://stackoverflow.com/questions/17693578/android-how-to-display-2-listviews-in-one-activity-one-after-the-other
    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = MeasureSpec.makeMeasureSpec(mListView.getWidth(), MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }


    // class to define properties of menu items
    class ControlsMenuOptions {
        private String menuDescription;
        private Intent menuIntent;

        public ControlsMenuOptions(String menuDescription, Intent menuIntent) {
            this.menuDescription = menuDescription;
            this.menuIntent = menuIntent;
        }

        public String getMenuDescription() {return this.menuDescription; }
        public void setMenuDescription(String menuDescription) { this.menuDescription = menuDescription; }
        public Intent getMenuIntent() { return this.menuIntent; }
        public void setMenuIntent(Intent menuIntent) { this.menuIntent = menuIntent; }
    }


    // ADAPTERS
    // adapter for babies list
    private class BabyInfoMenuOptionsAdapter extends ArrayAdapter<BabyElements> {
        private ArrayList<BabyElements> items;

        public BabyInfoMenuOptionsAdapter(Context context, int textViewResourceId, ArrayList<BabyElements> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        // get the views for each item in the array list of MainActivityMenuOptions
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.babies_menu_list, null);
            }

            BabyElements menuItem = items.get(position);

            if (menuItem != null) {
                TextView header = (TextView) view.findViewById(R.id.tvBabiesMenuItem);

                // TODO: need to get a summary of baby info on the list from here, refer to RSS
                header.setText(menuItem.getBabyName());

                // TODO: can probably style the list item based on whether or not this is the selected baby


            }
            return view;
        }
    }

    // adapter for controls list
    private class ControlsMenuOptionsAdapter extends ArrayAdapter<ControlsMenuOptions> {
        private ArrayList<ControlsMenuOptions> items;

        public ControlsMenuOptionsAdapter(Context context, int textViewResourceId, ArrayList<ControlsMenuOptions> items) {
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

            ControlsMenuOptions menuItem = items.get(position);

            if (menuItem != null) {
                TextView header = (TextView) view.findViewById(R.id.tvMenuItem);
                header.setText(menuItem.getMenuDescription());
            }
            return view;
        }
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

        tvBabyInfoSelected.setText("Selected child: " + babyName);


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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void makeToast(String message){
        Toast.makeText(BabyInfoActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
