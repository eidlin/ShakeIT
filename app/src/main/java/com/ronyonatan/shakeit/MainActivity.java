package com.ronyonatan.shakeit;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import com.startapp.android.publish.adsCommon.VideoListener;

import java.util.ArrayList;



public class MainActivity  extends Activity implements SensorEventListener, SeekBar.OnSeekBarChangeListener,View.OnClickListener {
    private StartAppAd startAppAd=new StartAppAd(this);
ConstraintLayout blackbackground;
    private SeekBar sensitivitySB; // seekbar for controlling sensitivity
    private TextView sensitivityTV;
  private ImageView gifView;
    // MediaPlayer controls playing the mp3
    private MediaPlayer mp;
    private boolean playing = false;
    Button gifOne;
    Button gifTwo;
    Button gifThree;
    // Display the gif in a webview for simplicity
    //private WebView webWV;
   // private WebView webWV;
    // Stuff for detecting shakes
    private SensorManager sm;
    private Sensor accel;
    private float xAccel, yAccel, zAccel;
    private boolean initialized = false;
    private double NOISE = 5.0;
    private ArrayList<Double> previousNoise;
    private final int MAXNOISECOUNT = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make this activity, full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Hide the Title bar of this activity screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, "205738275", true);
        startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO);
        startAppAd.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {

            }
        });
StartAppAd.showSplash(this,savedInstanceState);

        setContentView(R.layout.activity_main);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        blackbackground=(ConstraintLayout)findViewById(R.id.black);
        blackbackground.setBackgroundColor(Color.BLACK);
        gifOne=(Button)findViewById(R.id.gifOne);
        gifTwo=(Button)findViewById(R.id.gifTwo);
        gifThree=(Button)findViewById(R.id.gifThree);

        gifOne.setOnClickListener(this);
        gifTwo.setOnClickListener(this);
        gifThree.setOnClickListener(this);
        gifView = (ImageView) findViewById(R.id.gifView);
        sensitivitySB = (SeekBar)findViewById(R.id.sensitivitySB);
        sensitivitySB.setOnSeekBarChangeListener(this);
        sensitivityTV = (TextView) findViewById(R.id.sensitivityTV);
        sm = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        mp = MediaPlayer.create(this,R.raw.click );
        mp.setVolume(1.0f, 1.0f);

          //  Glide.with(this).load("https://media.giphy.com/media/TfKfqjt2i4GIM/giphy.gif").asGif().placeholder(R.drawable.point).into(gifView);


        Glide.with(this).load("")
                .thumbnail(Glide.with(this).load(R.drawable.giphyorg))
                .fitCenter()
                .crossFade()
                .into(gifView);



        gifView.setVisibility(View.INVISIBLE);

        previousNoise = new ArrayList<Double>();


    }

    @Override
    public void onBackPressed() {
        startAppAd.onBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAppAd.onResume();
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        startAppAd.onPause();
        sm.unregisterListener(this);
        stopGif();

    }
    public void onShowedAd(){
startAppAd.showAd();
    }
    private void playGif(){
        mp.start();
        mp.setLooping(true);
        gifView.setVisibility(View.VISIBLE);

    }
    private void stopGif(){
        mp.pause();
        mp.seekTo(mp.getCurrentPosition());
        gifView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (!initialized){
            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            initialized = true;
        }
        else {
            float dAX = xAccel - event.values[0];
            float dAY = yAccel - event.values[1];
            float dAZ = zAccel - event.values[2];
            double noiseVector = Math.sqrt(Math.pow(dAX,2)+Math.pow(dAY,2)+Math.pow(dAZ,2));

            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            previousNoise.add(noiseVector);
            while (previousNoise.size() > MAXNOISECOUNT){
                previousNoise.remove(0);
            }
            if (previousNoise.size() == MAXNOISECOUNT && !playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT > NOISE){
                    playGif();

                    playing = true;
                }
            }
            else if (playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT < NOISE){
                    if (mp.isPlaying()){
                        onShowedAd();
                        stopGif();
                    }
                    playing = false;
                }
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sensitivityTV.setText("Sensitivity (" + progress + "/10)");
        NOISE = (10-progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.gifOne:
                Glide.with(this).load("")
                        .thumbnail(Glide.with(this).load(R.drawable.btngifone))
                        .fitCenter()
                        .crossFade()
                        .into(gifView);
                break;
            case R.id.gifTwo:
                Glide.with(this).load("")
                        .thumbnail(Glide.with(this).load(R.drawable.gip)).crossFade()
                        .into(gifView);

                break;
            case R.id.gifThree:
                onShowedAd();
                Glide.with(this).load("")
                        .thumbnail(Glide.with(this).load(R.drawable.giphyorg))
                        .fitCenter()
                        .crossFade()
                        .into(gifView);
                break;

        }    }
}
