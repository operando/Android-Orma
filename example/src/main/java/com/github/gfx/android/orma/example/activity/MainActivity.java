package com.github.gfx.android.orma.example.activity;

import com.github.gfx.android.orma.example.BuildConfig;
import com.github.gfx.android.orma.example.R;
import com.github.gfx.android.orma.example.databinding.ActivityMainBinding;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;
import com.github.gfx.android.orma.example.orma.Todo;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.List;

import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final String ORMA_SITE = "https://github.com/gfx/Android-Orma/";

    ActivityMainBinding activityMain;

    OrmaDatabase orma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMain = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = activityMain.appBarMain.toolbar;
        setSupportActionBar(toolbar);

        FloatingActionButton fab = activityMain.appBarMain.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, ORMA_SITE, Snackbar.LENGTH_LONG)
                        .setAction("Open", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ORMA_SITE));
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        });

        DrawerLayout drawer = activityMain.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = activityMain.navView;
        navigationView.setNavigationItemSelectedListener(this);

        activityMain.appBarMain.contentMain.textInfo.setText(
                "This is an example app for Orma v" + BuildConfig.VERSION_NAME + ".");

        final ArrayAdapter<String> logsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        activityMain.appBarMain.contentMain.listLogs.setAdapter(logsAdapter);

        orma = OrmaDatabase.builder(this)
                .migrationEngine(new SchemaDiffMigration(this) {
                    @Override
                    public void executeStatements(SQLiteDatabase db, List<String> statements) {
                        logsAdapter.addAll(statements);

                        super.executeStatements(db, statements);
                    }
                })
                .build();

        Schedulers.io()
                .createWorker()
                .schedule(new Action0() {
                    @Override
                    public void call() {
                        simpleCRUD();
                    }
                });
    }

    /**
     * Demonstrates simple CRUD operations, which makes no sense though.
     */
    void simpleCRUD() {
        // create
        Todo todo = new Todo();
        todo.title = "buy";
        todo.content = "milk banana apple";
        todo.createdTimeMillis = System.currentTimeMillis();
        orma.insertIntoTodo(todo);

        // read
        todo = orma.selectFromTodo()
                .where("title = ?", "buy")
                .value();

        // update
        orma.updateTodo()
                .where("title = ?", "buy")
                .done(true)
                .execute();

        // delete
        orma.deleteFromTodo()
                .where("done = ?", true)
                .execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = activityMain.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_todo) {
            startActivity(TodoActivity.createIntent(this));
        } else if (id == R.id.nav_benchmark) {
            startActivity(BenchmarkActivity.createIntent(this));
        } else if (id == R.id.nav_share) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_send) {
            Toast.makeText(this, "Not yet implemented", Toast.LENGTH_LONG).show();
        }

        activityMain.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
