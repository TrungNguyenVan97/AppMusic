package com.example.appmusicmp3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

public class FavoriteActivity extends Activity {

    private RecyclerView favRvList;
    private ImageView favImgBack;
    private Button favDeleteAll;
    private ArrayList<Song> listFavorite = new ArrayList<>();
    private ArrayList<Song> listCheck = new ArrayList<>();
    private SongAdapter adapter;
    private Song songPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        findView();
        initView();
        initAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        initAction();
    }

    private void initView() {
        checkListFavorite();
        listFavorite = (ArrayList<Song>) DatabaseSong.getInstance(FavoriteActivity.this).daoSong().getListSong();
        Collections.sort(listFavorite, new CompareToTiTle());
        if (listFavorite != null) {
            adapter = new SongAdapter(listFavorite);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FavoriteActivity.this, RecyclerView.VERTICAL, false);
            favRvList.setLayoutManager(linearLayoutManager);
            favRvList.setAdapter(adapter);
        }
    }

    private void checkListFavorite() {
        listCheck = MusicBuilder.g().getListSong();
        listFavorite = (ArrayList<Song>) DatabaseSong.getInstance(FavoriteActivity.this).daoSong().getListSong();
        for (int i = 0; i < listFavorite.size(); i++) {
            boolean check = false;
            for (int j = 0; j < listCheck.size(); j++) {
                if (listFavorite.get(i).getId().equals(listCheck.get(j).getId())) {
                    check = true;
                    break;
                } else {
                    check = false;
                }
            }
            if (check) {
            } else {
                DatabaseSong.getInstance(FavoriteActivity.this).daoSong().deleteSong(listFavorite.get(i));
            }
        }
    }

    private void initAction() {
        adapter.setCallBack(new SongAdapter.CallBack() {
            @Override
            public void playMP3(int position) {
                // gửi Data cho PlayMP3
                songPlaying = listFavorite.get(position);
                Intent intentPlayMP3 = new Intent(FavoriteActivity.this, PlayMP3Activity.class);
                intentPlayMP3.putExtra(Tags.EXTRA_MP3_SONG, songPlaying);
                startActivity(intentPlayMP3);

                // gửi Data cho service
                Intent intent = new Intent(FavoriteActivity.this, SongService.class);
                intent.putExtra(Tags.EXTRA_SERVICE_SONG, songPlaying);
                startService(intent);
            }
        });

        favDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseSong.getInstance(FavoriteActivity.this).daoSong().deleteListSong();
                initView();
            }
        });

        favImgBack.setOnClickListener(new View.OnClickListener() {
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

    private void findView() {
        favRvList = findViewById(R.id.favRvList);
        favImgBack = findViewById(R.id.favImgBack);
        favDeleteAll = findViewById(R.id.favDeleteAll);
    }
}