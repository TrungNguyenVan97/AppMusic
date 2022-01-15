package com.example.appmusicmp3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayMP3Activity extends Activity {


    private TextView tvTitle, tvArtist, tvTimeStart, tvTimeEnd;
    private ImageButton btnPlay, btnNext, btnPrev, btnBack;
    private SeekBar sbTime;
    private CheckBox cbLike, cbRandom, cbRepeat;
    private Song songPlaying;
    private ArrayList<Song> listSong;
    private ArrayList<Song> listShuffle = new ArrayList<>();
    public static final String EXTRA_SERVICE_LIST = "EXTRA_SERVICE_LIST";
    public static final String EXTRA_SERVICE_POSITION = "EXTRA_SERVICE_POSITION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_mp3);

        findView();
        setData();
        initAction();
    }


    public void setData() {
        listSong = (ArrayList<Song>) getIntent().getSerializableExtra(MainActivity.EXTRA_PLAY_MP3_LIST);
        songPlaying = (Song) getIntent().getSerializableExtra(MainActivity.EXTRA_PLAY_MP3_POSITION);
        initMedia();
    }

    public void initMedia() {
        if (songPlaying == null) {
            return;
        }
        Uri uri = Uri.parse(songPlaying.getData());
        MusicBuilder.g().initMediaPlayer(this, uri);
        tvTitle.setText(songPlaying.getTitle());
        tvArtist.setText(songPlaying.getArtist());
        setTimeFinish();
        updateTime();
        MusicBuilder.g().play();
        btnPlay.setImageResource(R.drawable.icons_pause);

        startService();
    }

    private void startService() {
        Intent intentService = new Intent(PlayMP3Activity.this, SongService.class);
        intentService.putExtra(EXTRA_SERVICE_LIST, listSong);
        intentService.putExtra(EXTRA_SERVICE_POSITION, songPlaying);
        startService(intentService);
    }

    public void updateTime() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
                tvTimeStart.setText(formatTime.format(MusicBuilder.g().getMediaPlayer().getCurrentPosition()));
                sbTime.setProgress(MusicBuilder.g().getMediaPlayer().getCurrentPosition());

                // kiểm tra bài hát -> nếu hết bài thì next
                MusicBuilder.g().getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        MusicBuilder.g().stop();
                        nextSong();
                        initMedia();
                    }
                });
                handler.postDelayed(this, 200);
            }
        }, 100);
    }

    public void setTimeFinish() {
        SimpleDateFormat formatTime = new SimpleDateFormat("mm:ss");
        tvTimeEnd.setText(formatTime.format(MusicBuilder.g().getMediaPlayer().getDuration()));
        // gán max sbTime = MusicBuilder.g().getMediaPlayer().getDuration()
        sbTime.setMax(MusicBuilder.g().getMediaPlayer().getDuration());
    }

    public void nextSong() {
        boolean isRandom = cbRandom.isChecked();
        boolean isRepeat = cbRepeat.isChecked();
        if (isRandom) {
            if (listShuffle != null && listShuffle.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listShuffle);
            if (position >= 0) {
                if (position == listShuffle.size() - 1) {
                    songPlaying = listShuffle.get(0);
                } else {
                    songPlaying = listShuffle.get(position + 1);
                }
            }

        } else if (isRepeat) {
            if (listSong != null && listSong.isEmpty()) {
                return;
            } else {
                int position = getIndexOfFirst(songPlaying, listSong);
                songPlaying = listSong.get(position);
            }


        } else {
            if (listSong != null && listSong.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listSong);
            if (position >= 0) {
                if (position == listSong.size() - 1) {
                    songPlaying = listSong.get(0);
                } else {
                    songPlaying = listSong.get(position + 1);
                }
            }
        }
    }

    public void preSong() {
        boolean isRandom = cbRandom.isChecked();
        boolean isRepeat = cbRepeat.isChecked();
        if (isRandom) {
            if (listShuffle != null && listShuffle.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listShuffle);
            if (position > 0) {
                songPlaying = listShuffle.get(position - 1);
            }
            if (position == 0) {
                songPlaying = listShuffle.get(0);
            }

        } else if (isRepeat) {
            if (listSong != null && listSong.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listSong);
            songPlaying = listSong.get(position);

        } else {
            if (listSong != null && listSong.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listSong);
            if (position > 0) {
                songPlaying = listSong.get(position - 1);
            }
        }
    }

    public int getIndexOfFirst(Song songSelected, List<Song> list) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        int indexMatch = -1;
        for (int i = 0; i < list.size(); i++) {
            if (songSelected.getId().equals(list.get(i).getId())) {
                indexMatch = i;
            }
        }
        return indexMatch;
    }

    public void initAction() {
        // play
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    MusicBuilder.g().pause();
                    btnPlay.setImageResource(R.drawable.icons_play);
                } else {
                    MusicBuilder.g().play();
                    btnPlay.setImageResource(R.drawable.icons_pause);
                }
            }
        });
        // next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                nextSong();
                initMedia();
            }
        });
        // prev
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                preSong();
                initMedia();
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
                MusicBuilder.g().getMediaPlayer().seekTo(sbTime.getProgress());
            }
        });

        cbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(PlayMP3Activity.this, " Chế độ phát ngẫu nhiên ", Toast.LENGTH_SHORT).show();
                    cbRepeat.setChecked(false);
                    if (listSong != null) {
                        listShuffle.addAll(listSong);
                        Collections.shuffle(listShuffle);
                    }
                }
            }
        });

        cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(PlayMP3Activity.this, " Chế độ phát lặp lại ", Toast.LENGTH_SHORT).show();
                    cbRandom.setChecked(false);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void findView() {
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
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