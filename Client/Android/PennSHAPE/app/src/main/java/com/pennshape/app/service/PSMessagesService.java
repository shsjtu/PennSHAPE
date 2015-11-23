package com.pennshape.app.service;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pennshape.app.R;
import com.pennshape.app.activity.PSLoginActivity;
import com.pennshape.app.activity.PSMainActivity;
import com.pennshape.app.model.PSDataStore;
import com.pennshape.app.request.PSHttpTaskRequest;
import com.pennshape.app.request.PSMessagePullTaskRequest;

import org.json.JSONArray;


public class PSMessagesService extends Service implements PSHttpTaskRequest.PSHttpTaskRequestHandler{
    private final IBinder binder = new MessagesBinder();
    public static int notificationID  = 24066; //SHAPE

    public PSMessagesService() {
    }

    public void onCreate() {
        PSDataStore.getInstance().initDatabase(PSMessagesService.this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PSMessagesService", "onStartCommand~");
        String groupID = canPullMessage();
        if(groupID!=null){
            pullMessages(groupID);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void notifyNewMessage(String message){
        //Builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("Penn Shape");
        mBuilder.setContentText(message);
        mBuilder.setVibrate(new long[] { 300, 300, 300, 300, 300 });
        mBuilder.setAutoCancel(true);
        //Intent
        Intent resultIntent = new Intent(this, PSLoginActivity.class);
        resultIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.ps_service_message_intent_extra));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        //stackBuilder.addParentStack(PSLoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        //NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, mBuilder.build());
    }

    private String canPullMessage(){
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.ps_pref_name), Context.MODE_PRIVATE);
        String userID = sharedPref.getString(getString(R.string.ps_pref_key_id), null);
        String groupID = sharedPref.getString(getString(R.string.ps_pref_key_group), null);
        if(userID!=null && userID.length()>0 && groupID!=null && groupID.length()>0 ) {
            return groupID;
        }
        return null;
    }

    private void pullMessages(String groupID){
        PSMessagePullTaskRequest request = new PSMessagePullTaskRequest();
        request.setGroupID(groupID);
        int lastID = PSDataStore.getInstance().lastMessageID();
        if(lastID>0){
            request.setFrom(""+lastID);
        }
        request.handler = this;
        request.run();
    }

    @Override
    public void onSuccess(PSHttpTaskRequest request, Object result) {
        if(request instanceof PSMessagePullTaskRequest){
            PSDataStore.getInstance().saveMessageArray((JSONArray) result);
            if(((JSONArray) result).length()>0) {
                notifyNewMessage("You have " + ((JSONArray) result).length() + " new messages");
            }
        }
    }

    @Override
    public void onFailure(PSHttpTaskRequest request, String error) {
        displayError(error);
    }

    private void displayError(String message) {
        new AlertDialog.Builder(getApplicationContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public class MessagesBinder extends Binder {
        PSMessagesService getService() {
            return PSMessagesService.this;
        }
    }
}