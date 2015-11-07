package com.pennshape.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pennshape.app.R;
import com.pennshape.app.model.PSDataStore;
import com.pennshape.app.request.PSHttpTaskRequest;
import com.pennshape.app.request.PSLoginTaskRequest;
import com.pennshape.app.request.PSUserDataTaskRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class PSLoginActivity extends Activity implements PSHttpTaskRequest.PSHttpTaskRequestHandler{
    EditText userName;
    EditText userPIN;
    Button loginButton;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ps_activity_login);
        userName = (EditText)findViewById(R.id.loginUserNameEditText);
        userPIN = (EditText)findViewById(R.id.loginUserPINEditText);
        loginButton = (Button)findViewById(R.id.button);
        checkUser();
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
        if(userName.getText().toString().equals("4Test")){
            showSampleData();
        }else{
            login();
            progress = ProgressDialog.show(this, "Loading...", "Please wait", true);
        }
    }

    private void checkUser(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.ps_pref_name), Context.MODE_PRIVATE);
        String userID = sharedPref.getString(getString(R.string.ps_pref_key_id), null);
        if(userID!=null && userID.length()>0) {
            progress = ProgressDialog.show(this, "Loading...", "Please wait", true);
            pullUserData(userID);
        }
    }

    private void persistUserID(String userID) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.ps_pref_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.ps_pref_key_id), userID);
        editor.apply();
    }

    private void showSampleData() {
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

    private void displayError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void login(){
        PSLoginTaskRequest request = new PSLoginTaskRequest();
        request.setUserName(userName.getText().toString());
        request.handler = this;
        request.execute();
    }

    private void pullUserData(String userID) {
        PSUserDataTaskRequest request = new PSUserDataTaskRequest();
        request.setUserID(userID);
        request.handler = this;
        request.execute();
    }

    @Override
    public void onSuccess(PSHttpTaskRequest request, Object result) {
        if(request instanceof PSLoginTaskRequest) {
            String userID = (String)result;
            if(userID.length()==0){
                displayError("Invalid user ID");
                if(progress!=null) progress.dismiss();
            }else{
                persistUserID(userID);
                pullUserData(userID);
            }
        }else if(request instanceof PSUserDataTaskRequest) {
            if(progress!=null) progress.dismiss();
            JSONObject userData = (JSONObject)result;
            try {
                PSDataStore.getInstance().reloadFronJson(userData);
            } catch (JSONException e) {
                displayError("User data parse failure");
                return;
            }
            Intent intent;
            intent = new Intent(this, PSMainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    public void onFailure(PSHttpTaskRequest request, String error) {
        if(progress!=null) progress.dismiss();
        displayError(error);
    }
}
