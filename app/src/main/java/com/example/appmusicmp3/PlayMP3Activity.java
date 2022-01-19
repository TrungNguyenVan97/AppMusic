package com.example.appmusicmp3;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayMP3Activity extends Activity {
    private TextView tvTitle, tvArtist, tvTimeStart, tvTimeEnd;
    private ImageButton btnPlay, btnNext, btnPrev, btnBack;
    private SeekBar sbTime;
    private CheckBox cbLike, cbRandom, cbRepeat;
    private Song songPlaying;
    private boolean isPlaying;
    public static final String EXTRA_NEXT = "EXTRA_NEXT";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String EXTRA_PREV = "EXTRA_PREV";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE ";
    public static final String EXTRA_PLAY_PAUSE = "EXTRA_PLAY_PAUSE";

    private BroadcastReceiver autoNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SongService.ACTION_SEND_PlAY.equals(intent.getAction())) {
                songPlaying = (Song) intent.getSerializableExtra(SongService.SEND_TO_PLAY);
                tvTitle.setText(songPlaying.getTitle());
                tvArtist.setText(songPlaying.getArtist());
                setTimeFinish();
                updateTime();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_mp3);

        IntentFilter filterAutoNext = new IntentFilter(SongService.ACTION_SEND_PlAY);
        registerReceiver(autoNext, filterAutoNext);

        findView();
        initMediaView();
        initAction();
    }

    private void initMediaView() {
        Log.d("check", "initMedia");
        setDataViewFirst();
        setTimeFinish();
        updateTime();
    }

    private void setDataViewFirst() {
        songPlaying = (Song) getIntent().getSerializableExtra(MainActivity.EXTRA_MP3_SONG);
        if (songPlaying == null) {
            return;
        }
        Log.d("check", "setDataViewFirst" + " " + songPlaying.getTitle());
        tvTitle.setText(songPlaying.getTitle());
        tvArtist.setText(songPlaying.getArtist());
        btnPlay.setImageResource(R.drawable.icons_pause);
    }

    private void setDataView() {
        tvTitle.setText(songPlaying.getTitle());
        tvArtist.setText(songPlaying.getArtist());
        btnPlay.setImageResource(R.drawable.icons_pause);
    }

    private void setTimeFinish() {
        Log.d("check", "set time finish");
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        tvTimeEnd.setText(formatTime.format(MusicBuilder.g().getMediaPlayer().getDuration()));
        // gán max sbTime = MusicBuilder.g().getMediaPlayer().getDuration()
        sbTime.setMax(MusicBuilder.g().getMediaPlayer().getDuration());
    }

    private void updateTime() {
        Log.d("check", "update time");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                tvTimeStart.setText(formatTime.format(MusicBuilder.g().getMediaPlayer().getCurrentPosition()));
                sbTime.setProgress(MusicBuilder.g().getMediaPlayer().getCurrentPosition());
                handler.postDelayed(this, 200);
            }
        }, 100);
    }

    private void initAction() {

        // play-pause
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    MusicBuilder.g().pause();
                    isPlaying = false;
                    btnPlay.setImageResource(R.drawable.icons_play);
                } else {
                    MusicBuilder.g().play();
                    isPlaying = true;
                    btnPlay.setImageResource(R.drawable.icons_pause);
                }
                Intent intent = new Intent(ACTION_PLAY_PAUSE);
                intent.putExtra(EXTRA_PLAY_PAUSE, isPlaying);
                sendBroadcast(intent);
            }
        });

        // next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                MusicBuilder.g().nextSong();
                songPlaying = MusicBuilder.g().getSongPlaying();
                MusicBuilder.g().initMediaPlayer(PlayMP3Activity.this, songPlaying);
                MusicBuilder.g().play();
                setDataView();
                setTimeFinish();
                updateTime();

                Intent intent = new Intent(ACTION_NEXT);
                intent.putExtra(EXTRA_NEXT, songPlaying);
                sendBroadcast(intent);
            }
        });

        //prev
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                MusicBuilder.g().preSong();
                songPlaying = MusicBuilder.g().getSongPlaying();
                MusicBuilder.g().initMediaPlayer(PlayMP3Activity.this, songPlaying);
                MusicBuilder.g().play();
                setDataView();
                setTimeFinish();
                updateTime();

                Intent intent = new Intent(ACTION_PREV);
                intent.putExtra(EXTRA_PREV, songPlaying);
                sendBroadcast(intent);
            }
        });

        cbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbRepeat.setChecked(false);
                    MusicBuilder.g().setRepeat(false);
                    MusicBuilder.g().setRandom(true);
                    MusicBuilder.g().setListShuffle();
                } else {
                    MusicBuilder.g().setRandom(false);
                }
            }
        });
        cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbRandom.setChecked(false);
                    MusicBuilder.g().setRepeat(true);
                    MusicBuilder.g().setRandom(false);
                } else {
                    MusicBuilder.g().setRepeat(false);
                }
            }
        });

        // touch seekbar
        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicBuilder.g().getMediaPlayer().seekTo(sbTime.getProgress());
            }
        });

        // back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(autoNext);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void findView() {
        tvTitle = findViewById(R.id.tvPlayTitle);
        tvArtist = findViewById(R.id.tvPlayArtist);
        tvTimeStart = findViewById(R.id.tvTimeStart);
        tvTimeEnd = findViewById(R.id.tvTimeEnd);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnBack = findViewById(R.id.btnBack);
        cbRandom = findViewById(R.id.cbRandom);
        cbRepeat = findViewById(R.id.cbRepeat);
        sbTime = findViewById(R.id.sbTime);
        cbLike = findViewById(R.id.cbLike);
    }
}