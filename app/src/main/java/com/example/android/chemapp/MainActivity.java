package com.example.android.chemapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.chemapp.data.ChemElement;
import com.example.android.chemapp.data.Status;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ElementSearchAdapter.OnSearchResultClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mSearchResultsRV;
    private EditText mSearchBoxET;
    private ProgressBar mLoadingIndicatorPB;
    private TextView mErrorMessageTV;
    private DrawerLayout mDrawerLayout;

    private ElementSearchAdapter mElementSearchAdapter;

    private ChemAppViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_nav_menu);

        mSearchBoxET = findViewById(R.id.et_search_box);
        mSearchResultsRV = findViewById(R.id.rv_search_results);

        mSearchResultsRV.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultsRV.setHasFixedSize(true);

        mElementSearchAdapter = new ElementSearchAdapter(this);
        mSearchResultsRV.setAdapter(mElementSearchAdapter);

        mLoadingIndicatorPB = findViewById(R.id.pb_loading_indicator);
        mErrorMessageTV = findViewById(R.id.tv_error_message);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mViewModel = new ViewModelProvider(this).get(ChemAppViewModel.class);

        mViewModel.getSearchResults().observe(this, new Observer<List<ChemElement>>() {
            @Override
            public void onChanged(List<ChemElement> ChemElements) {
                mElementSearchAdapter.updateSearchResults(ChemElements);
            }
        });

        mViewModel.getLoadingStatus().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(Status status) {
                if (status == Status.LOADING) {
                    mLoadingIndicatorPB.setVisibility(View.VISIBLE);
                } else if (status == Status.SUCCESS) {
                    mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                    mSearchResultsRV.setVisibility(View.VISIBLE);
                    mErrorMessageTV.setVisibility(View.INVISIBLE);
                } else {
                    mLoadingIndicatorPB.setVisibility(View.INVISIBLE);
                    mSearchResultsRV.setVisibility(View.INVISIBLE);
                    mErrorMessageTV.setVisibility(View.VISIBLE);
                }
            }
        });

        Button searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = mSearchBoxET.getText().toString();
                if (!TextUtils.isEmpty(searchQuery)) {
                    getPeriodicTable(searchQuery);
                }
            }
        });

        NavigationView navigationView = findViewById(R.id.nv_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_search:
                return true;
            case R.id.nav_saved_repos:
                Intent savedReposIntent = new Intent(this, SavedReposActivity.class);
                startActivity(savedReposIntent);
                return true;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSearchResultClicked(ChemElement repo) {
        Intent intent = new Intent(this, ElementDetailActivity.class);
        intent.putExtra(ElementDetailActivity.EXTRA_CHEM_ELEMENT, repo);
        startActivity(intent);
    }

    private void getPeriodicTable(String searchQuery) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mViewModel.loadSearchResults(searchQuery);
    }
}
