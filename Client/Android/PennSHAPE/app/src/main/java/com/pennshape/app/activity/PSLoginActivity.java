package com.pennshape.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pennshape.app.R;
import com.pennshape.app.model.PSDataStore;

import java.io.IOException;
import java.io.InputStream;


public class PSLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ps_activity_login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ps_menu_login, menu);
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

    public void onClick(View view) {
        //Using sample data
        try {
            AssetManager am = getApplicationContext().getAssets();
            InputStream is = am.open("sample_g4w3.json");
            PSDataStore.getInstance().reloadFromInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent;
        intent = new Intent(this, PSMainActivity.class);
        startActivity(intent);
        this.finish();
    }
}