package com.example.appmusicmp3;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class PlayMP3Activity extends AppCompatActivity {

    private TextView tvTitle, tvArtist, tvTimeStart, tvTimeEnd;
    private ImageButton btnPlay, btnNext, btnPrev;
    private SeekBar sbTime;
    private CheckBox cbLike, cbRandom, cbRepeat;
    private static MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<Song> listSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_mp3);

        findView();
        checkMP3();
        setData();
        initAction();
    }

    private void checkMP3() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
    }

    private void setData() {
        listSong = (ArrayList<Song>) getIntent().getSerializableExtra(MainActivity.EXTRA_PLAY_MP3_LIST);
        position = getIntent().getIntExtra(MainActivity.EXTRA_PLAY_MP3_POSITION, 0);
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        Uri uri = Uri.parse(listSong.get(position).getData());
        mediaPlayer = MediaPlayer.create(this, uri);
        tvTitle.setText(listSong.get(position).getTitle());
        tvArtist.setText(listSong.get(position).getArtist());
        setTimeFinish();
        updateTime();
        mediaPlayer.start();
        btnPlay.setImageResource(R.drawable.icons_pause);
    }

    private void updateTime() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                tvTimeStart.setText(formatTime.format(mediaPlayer.getCurrentPosition()));
                sbTime.setProgress(mediaPlayer.getCurrentPosition());

                // kiểm tra bài hát -> nếu hết bài thì next
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (cbRandom.isChecked()) {
                            Random random = new Random();
                            position = random.nextInt(listSong.size());
                        } else if (cbRepeat.isChecked()) {
                            position += 0;
                        } else {
                            position++;
                            if (position > listSong.size() - 1) {
                                position = 0;
                            }
                        }
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        initMediaPlayer();
                    }
                });
                handler.postDelayed(this, 200);
            }
        }, 100);
    }

    private void setTimeFinish() {
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        tvTimeEnd.setText(formatTime.format(mediaPlayer.getDuration()));
        // gán max sbTime = mediaPlayer.getDuration()
        sbTime.setMax(mediaPlayer.getDuration());
    }

    private void initAction() {
        // play
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlay.setImageResource(R.drawable.icons_play);
                } else {
                    mediaPlayer.start();
                    btnPlay.setImageResource(R.drawable.icons_pause);
                }
            }
        });
        // next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position++;
                if (position >= listSong.size()) {
                    position = 0;
                }
                mediaPlayer.stop();
                mediaPlayer.release();
                initMediaPlayer();
            }
        });
        // prev
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position--;
                if (position < 0) {
                    position = listSong.size() - 1;
                }
                mediaPlayer.stop();
                mediaPlayer.release();
                initMediaPlayer();
            }
        });

        // kéo tua bài hát
        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(sbTime.getProgress());
            }
        });

        cbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbRepeat.setChecked(false);
                }
            }
        });

        cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbRandom.setChecked(false);
                }
            }
        });
    }

    private void findView() {
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvTimeStart = findViewById(R.id.tvTimeStart);
        tvTimeEnd = findViewById(R.id.tvTimeEnd);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        cbRandom = findViewById(R.id.cbRandom);
        cbRepeat = findViewById(R.id.cbRepeat);
        sbTime = findViewById(R.id.sbTime);
        cbLike = findViewById(R.id.cbLike);
    }
}