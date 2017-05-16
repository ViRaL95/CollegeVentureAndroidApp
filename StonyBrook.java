package com.example.varun.finalproject;
//create an array of fragments, create xml file and for each fragment
//
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
//The following class just simply allows multiple fragments to be created and for it to be attached to the
//Stony Brook class.
public class StonyBrook extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private DrawerLayout mDrawerLayout;
    private static String username;
    private TextView username_info;
    private Fragment [] fragmentArray;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stony_brook);
        username_info=(TextView) findViewById(R.id.userName);

        Intent wherefromintent=getIntent();
        Bundle wherefrombundle=wherefromintent.getExtras();

        fragmentArray= new Fragment[3];

        if(wherefrombundle!=null){
            username=(String) wherefrombundle.get("username");
            username_info.setText(username);
        }
        else{
            username_info.setText(username);
        }

        mNavItems.add(new NavItem("Projects", "Projects at SBU", R.drawable.logout));
        mNavItems.add(new NavItem("About Us", "Get To Know About Us", R.drawable.logout));
        mNavItems.add(new NavItem("Log Out","Log Out of Account",R.drawable.logout));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
            private void selectItemFromDrawer(int position) {
                //create a fragment array and then index into the fragment array and retrieve the
                //fragment
                if(position==0) {
                    fragmentArray[position] = new PersonalProjectsFragment();
                }
                if(position==1){
                    fragmentArray[position]=new AboutUsFragment();
                }
                if(position==2){
                    Intent intent= new Intent(StonyBrook.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }

                //if the position=2 then we are directed to logout of the application. Therefore we should
                //not generate another fragment if the logout button is clicked
                if(position!=2) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.mainContent, fragmentArray[position])
                            .commit();

                    mDrawerList.setItemChecked(position, true);
                    setTitle(mNavItems.get(position).mTitle);

                    // Close the drawer
                    mDrawerLayout.closeDrawer(mDrawerPane);
                }
            }

        });

        fragmentArray[0]= new PersonalProjectsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragmentArray[0])
                .commit();

        mDrawerList.setItemChecked(0, true);
        setTitle(mNavItems.get(0).mTitle);

        mDrawerLayout.closeDrawer(mDrawerPane);



    }
    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }
    class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }
public String getUsername(){
    return username;
}
public void setUsername(String username){
    this.username=username;
}
}
