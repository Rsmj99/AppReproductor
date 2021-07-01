package com.example.tarea13_ejercicio01;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MediaPlayer vectormp[] = null;
    private int position = 0;
    private boolean repeat = false;
    private ArrayList<File> canciones;
    private SeekBar sb_music;
    Thread updateseekbar;
    static MediaPlayer mp;
    private TextView tv_name, tv_start, tv_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_play_pause).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_repeat).setOnClickListener(this);
        findViewById(R.id.btn_previous).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);

        sb_music=findViewById(R.id.sb_music);
        tv_name = findViewById(R.id.tv_name);
        tv_start=findViewById(R.id.tv_start);
        tv_stop=findViewById(R.id.tv_end);
        requestPermissions();

        sb_music.getProgressDrawable().setColorFilter(getResources().getColor(R.color.av_green), PorterDuff.Mode.MULTIPLY);
        sb_music.getThumb().setColorFilter(getResources().getColor(R.color.av_green), PorterDuff.Mode.SRC_IN);

        sb_music.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                findViewById(R.id.btn_next).performClick();
            }
        });

        String endTime= createTime(mp.getDuration());
        tv_stop.setText(endTime);

        final Handler handler  = new Handler();
        final int delay= 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime= createTime(mp.getCurrentPosition());
                tv_start.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);
    }

    private void BarraProgreso(MediaPlayer mediaPlayer){
        updateseekbar = new Thread(){

            @Override
            public void run() {
                int TotalDuration= mediaPlayer.getDuration();
                int  currentposition=0;
                while (currentposition<TotalDuration){
                    try {
                        sleep(500);
                        currentposition= mediaPlayer.getCurrentPosition();
                        sb_music.setProgress(currentposition);
                    }catch ( InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        updateseekbar.start();
    }

    private void requestPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        canciones = EncontrarCanciones(Environment.getExternalStorageDirectory());
                        mp = MediaPlayer.create(getApplicationContext(), Uri.parse(canciones.get(position).toString()));
                        tv_name.setText(canciones.get(position).getName());
                        mp.start();
                        BarraProgreso(mp);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> EncontrarCanciones(File root){
        ArrayList<File> canciones = new ArrayList<File>();
        File[] archivos = root.listFiles();
        for (File lista : archivos){
            if (lista.isDirectory() && !lista.isHidden()){
                canciones.addAll(EncontrarCanciones(lista));
            } else {
                if (lista.getName().endsWith(".mp3") || lista.getName().endsWith(".wav")){
                    canciones.add(lista);
                }
            }
        }
        return canciones;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_play_pause:
                PlayPause();
                break;
            case R.id.btn_stop:
                Stop();
                break;
            case R.id.btn_repeat:
                Repeat();
                break;
            case R.id.btn_previous:
                Previous();
                break;
            case R.id.btn_next:
                Next();
        }
    }

    private void PlayPause(){
        if (mp.isPlaying()){
            mp.pause();
            (findViewById(R.id.btn_play_pause)).setBackgroundResource(R.drawable.ic_play);
            Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
        } else {
            mp.start();
            (findViewById(R.id.btn_play_pause)).setBackgroundResource(R.drawable.ic_pause);
            Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
        }
        String endTime= createTime(mp.getDuration());
        tv_stop.setText(endTime);
        BarraProgreso(mp);
    }

    private void Stop(){
        if (mp != null){
            mp.stop();
            Uri u = Uri.parse(canciones.get(position).toString());
            mp= MediaPlayer.create(getApplication(),u);
            (findViewById(R.id.btn_play_pause)).setBackgroundResource(R.drawable.ic_play);
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
        }
    }

    private void Repeat(){
        if (repeat){
            repeat = false;
            mp.setLooping(repeat);
            (findViewById(R.id.btn_repeat)).setBackgroundResource(R.drawable.ic_norepeat);
            Toast.makeText(this, "No repeat", Toast.LENGTH_SHORT).show();
        }else {
            repeat = true;
            mp.setLooping(repeat);
            (findViewById(R.id.btn_repeat)).setBackgroundResource(R.drawable.ic_repeat);
            Toast.makeText(this, "Repeat", Toast.LENGTH_SHORT).show();
        }
    }

    private void Previous(){
        if (position >= 1){
            if (mp.isPlaying()){
                mp.stop();
                position--;
                Uri u = Uri.parse(canciones.get(position).toString());
                mp= MediaPlayer.create(getApplicationContext(),u);
                tv_name.setText(canciones.get(position).getName());
                String endTime= createTime(mp.getDuration());
                tv_stop.setText(endTime);
                mp.start();
            }else {
                position--;
            }
            BarraProgreso(mp);
        }else {
            Toast.makeText(this, "No hay más canciones", Toast.LENGTH_SHORT).show();
        }
    }

    private void Next(){
        if (position < canciones.size() - 1){
            if (mp.isPlaying()){
                mp.stop();
                position++;
                Uri u = Uri.parse(canciones.get(position).toString());
                mp= MediaPlayer.create(getApplicationContext(),u);
                tv_name.setText(canciones.get(position).getName());
                String endTime= createTime(mp.getDuration());
                tv_stop.setText(endTime);
                mp.start();
            }else {
                position++;
            }
            BarraProgreso(mp);
        }else {
            Toast.makeText(this, "No hay más canciones", Toast.LENGTH_SHORT).show();
        }
    }

    public String createTime(int duration){
        String time= "";
        int min= duration / 60000 ;
        int secs= duration % 60000 ;

        int sec = Math.round((float)secs/1000) ;

        if( min < 10 ){
            time += "0" ;
        }
        time+=min+":";
        if(sec<10){
            time+="0";
        }
        time+=sec;

        return  time;
    }
}