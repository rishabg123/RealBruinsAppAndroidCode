package com.rishab.schs.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.rishab.schs.R;
import com.rishab.schs.Upload;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.List;

import static com.rishab.schs.Activities.SplashActivity.uploads;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private SwipeRefreshLayout swipeView;

    private ProgressBar mProgressCircle;

    boolean firstStart;
    boolean firstStartFromActivity = false;
    public static String version;

    private Handler handler;

    private DrawerLayout drawer;

    private boolean touching;
    private boolean scrolling;

    private int i = 2;

    private List<Upload> mUploads = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstStartFromActivity = getIntent().getBooleanExtra("First Launch", false);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        firstStart = prefs.getBoolean("firstStart", true);
        if (firstStartFromActivity) {
            firstStart = true;
        }
        if (firstStart) {
            showStartDialog();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                version();
                return true;
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        mProgressCircle = findViewById(R.id.progress_circle);
        swipeView = findViewById(R.id.swipe);

        swipeView.setOnRefreshListener(this);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                swipeView.postDelayed(() -> startActivity(new Intent(getApplicationContext(), SplashActivity.class)), 500);
            }
        };

        if(uploads.size() == 1){
            mUploads.add(uploads.get(0));
        }

        if (uploads.size() >= 2){
            mUploads.add(uploads.get(0));
            mUploads.add(uploads.get(1));
        }





        mAdapter = new ImageAdapter(MainActivity.this, mUploads);
        mRecyclerView.setAdapter(mAdapter);
        mProgressCircle.setVisibility(View.INVISIBLE);

//        mRecyclerView.setOnScrollListener(new EndlessRecyclerViewScrollListener() {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                for (int i = 2; i <= 30; i++) {
//                    Upload upload = uploads.get(i);
//                    mUploads.add(upload);
//                }
//                mAdapter.notifyDataSetChanged();
//            }
//        });

        NestedScrollView scrollView = findViewById(R.id.scrollView);

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            Log.d("TOUCHING", "TRUE");
            touching = true;
            addDataToList();
        });




       mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener() {
           @Override
           public void onLoadMore() {
               Log.d("SCROLLING", "TRUE");


           }
       });



        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void addDataToList(){

        int length = uploads.size();

        Log.d("SIZE", String.valueOf(uploads.size()));
        Log.d("I", String.valueOf(i));

        if (i < length) {
            mUploads.add(uploads.get(i));
            mAdapter.notifyDataSetChanged();
            i = i +1;
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_calendar) {
           // Snackbar.make(drawer, "Coming Soon!", Snackbar.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, Calendar.class);
            startActivity(intent);
        } else if (id == R.id.nav_bellSchedule) {
            Intent intent = new Intent(MainActivity.this, MainSchedule.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_announcements) {
            Intent intent = new Intent(MainActivity.this, Announcements.class);
            startActivity(intent);
        }else if (id == R.id.nav_contact) {
            Intent intent = new Intent(MainActivity.this, Contact.class);
            startActivity(intent);
        }else if (id == R.id.nav_clubs) {
            Intent clubsIntent = new Intent(getApplicationContext(), ClubsActivity.class);
            startActivity(clubsIntent);
            mProgressCircle.setVisibility(View.GONE);
        } else if (id == R.id.nav_council) {
            Snackbar.make(drawer, "Available When Elections Begin!", Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.nav_staffDirectory){
            Intent intent = new Intent(MainActivity.this, StaffActivity.class);
            if (firstStart || firstStartFromActivity) intent.putExtra("First Launch", true);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void showStartDialog() {
        if (!firstStartFromActivity) {

            new AlertDialog.Builder(this)
                    .setTitle("Welcome to the Official SCHS Bruins App!")
                    .setMessage("With this app, you will be able to receive school updates, email teachers," +
                            " view the bell schedule which includes a live countdown of when the next class starts, and more! If you are a " +
                            "club president, make sure to apply for an account to post updates on the home screen by emailing bruinsappteam@gmail.com!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstStart", false);
            editor.apply();
        }
    }

    private void version() {
//        try {
//            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
//            version = pInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        new AlertDialog.Builder(this)
//                .setTitle("Version")
//                .setMessage(String.valueOf(version))
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .create().show();
//        Log.d("DIALOG", "SHOWED");
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Generate");
        intentIntegrator.initiateScan();
    }

    @Override
    public void onRefresh() {
        swipeView.postDelayed(new Runnable() {

            @Override
            public void run() {
                swipeView.setRefreshing(true);
                handler.sendEmptyMessage(0);
            }
        }, 1000);
    }
}