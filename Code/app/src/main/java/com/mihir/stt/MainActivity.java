package com.mihir.stt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORD_REQUEST_CODE = 101;
    private boolean listening = false;
    private SpeechToText speechService;
    private MicrophoneInputStream capture;
    private MicrophoneHelper microphoneHelper;
    public Button btnRecord;
    public TextView print;
    public static final String tag="sidd";
    public String text;
    public String filepath;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private BottomNavigationView mBottomNav;
    private NavigationView nav;
    private ImageButton RecordImageButton;
    int t=0;
    public EditText mEditText, inputMessage;
    public final Context context = this;
    private static String FILE_NAME = "record001";
    ArrayList<String> al=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecord=(Button)findViewById(R.id.imageButton);
        inputMessage=(EditText) findViewById(R.id.textView);
        print=(TextView)findViewById(R.id.textView2);
        microphoneHelper = new MicrophoneHelper(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        nav = (NavigationView) findViewById(R.id.nav);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        filepath = getExternalFilesDir("Speech to Text").toString();



        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch (item.getItemId()){
                    case R.id.my_records:{
                        Intent i =new Intent(getApplicationContext(),Load.class);
                        startActivity(i);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        break;
                    }
                }
                return true;
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void save(View v) {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                FILE_NAME = userInput.getText().toString();
                                String text = inputMessage.getText().toString();
                                String text2 = text;
                                FileOutputStream fos = null;

                                try {

                                    FILE_NAME = userInput.getText().toString();
                                    File file = new File(filepath, FILE_NAME);
                                    try {
                                        fos = new FileOutputStream(file);
                                        fos.write(text.getBytes());
                                        fos.close();
                                        Log.d("mihir: ", filepath);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(context, "File saved at "+filepath, Toast.LENGTH_SHORT).show();

                                    inputMessage.getText().clear();
                                } finally {
                                    if (fos != null) {
                                        try {
                                            fos.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    public void record(View view) {
        speechService = new SpeechToText();
        speechService.setUsernameAndPassword("apikey", "nRf2Ru7-I1xt1FbXoMeZbH8VUPZ-amsaLBSh21Iaot9P"); //
        speechService.setEndPoint("https://gateway-lon.watsonplatform.net/speech-to-text/api");

        if(!listening) {
            inputMessage.setText("");
            inputMessage.setVisibility(View.INVISIBLE);
            //inputMessage.setText("SPEAKER 0:");
            text = "";
            capture = microphoneHelper.getInputStream(true);
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(capture, getRecognizeOptions(), new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {
                        showError(e);
                    }
                }
            }).start();
            listening = true;
            Toast.makeText(MainActivity.this,"Listening....Click to Stop", Toast.LENGTH_LONG).show();
            print.setText("LISTENING...Press again to stop.");
            btnRecord.setText("STOP");

        } else {
            try {
                print.setText("Press to start recording!");
                btnRecord.setText("RECORD");
                microphoneHelper.closeInputStream();
                listening = false;
                Log.d("finishRecord",text);
                Toast.makeText(MainActivity.this,"Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
                inputMessage.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected){
            return true;
        }
        else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions() {

        return new RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .timestamps(true)
                .inactivityTimeout(2000)
                .wordAlternativesThreshold((double) 0.9)
                .smartFormatting(true)
                //TODO: Uncomment this to enable Speaker Diarization
                .speakerLabels(true)
                .build();
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {

        @Override
        public void onTranscription(SpeechResults speechResults) {
            System.out.println(speechResults);
            //TODO: Uncomment this to enable Speaker Diarization
            SpeakerLabelsDiarization.RecoTokens recoTokens = new SpeakerLabelsDiarization.RecoTokens();
            if(speechResults.getSpeakerLabels() !=null)
            {
                recoTokens.add(speechResults);

            }
            if(speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                text = (speechResults.getResults().get(0).getAlternatives().get(0).getTranscript());
            }
            else
            {
                boolean isFound = text.indexOf("%HESITATION") !=-1? true: false;
                Log.d(tag,text);
                if(isFound){
                    text =  text.replace("%HESITATION","");
                    Log.d(tag,"if:"+isFound);

                }
                showMicText(text);
            }
        }

        @Override public void onConnected() {

        }

        @Override public void onError(Exception e) {
            showError(e);
            enableMicButton();
        }

        @Override public void onDisconnected() {
            enableMicButton();
        }

        @Override
        public void onInactivityTimeout(RuntimeException runtimeException) {

        }

        @Override
        public void onListening() {

        }

        @Override
        public void onTranscriptionComplete() {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case RECORD_REQUEST_CODE: {

                String TAG = "MainActivity";
                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }
            case MicrophoneHelper.REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                inputMessage.append(text);
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                btnRecord.setEnabled(true);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                e.printStackTrace();
            }
        });
    }


    public void viewSaved(View view) {
        Intent intent = new Intent(getApplicationContext(), Load.class);
        startActivity(intent);

    }

}
