package com.example.tanyairenesheppard.afinally.helper;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.tanyairenesheppard.afinally.R;

/**
 * Created by AcadGildMentor on 6/8/2015.
 */
public class SongView extends LinearLayout {
    private Context mContext;
    private View view ;
    private ImageView mImgSong ;
    private TextView mtxtSongName , mTxtSongAlbumName , mTxtSongDuration;
    private Song song;
    private Uri mUri;

    public SongView(Context context) {
        super(context);
        mContext = context;
        hookUp();
    }
    private void hookUp(){
        view = View.inflate(mContext , R.layout.song_list_item, this);
        mImgSong = (ImageView) view.findViewById(R.id.img_listitem_file);
        mtxtSongName = (TextView) view.findViewById(R.id.txt_listitem_filename);
        mTxtSongAlbumName = (TextView) view.findViewById(R.id.txt_listitem_albumname);
        mTxtSongDuration = (TextView) view.findViewById(R.id.txt_listitem_duration);
    }
    public void setSongName(String name ){
        mtxtSongName.setText(name);
    }

    public String getSongName (){
        return this.mtxtSongName.getText().toString();
    }

    public void setSongImage(){
        mImgSong.setImageResource(R.drawable.no_clipart);
    }

    public void setSongAlbum(String album_name){ mTxtSongAlbumName.setText(album_name); }

    public String getAlbumName(){
        return  this.mTxtSongAlbumName.getText().toString();
    }

    public void setSongDuration(String duration){
        mTxtSongDuration.setText(duration);
    }

    public String getSongDuration(){
        return this.mTxtSongDuration.getText().toString();
    }

    public void setSong(Song song){ this.song = song;  }

    public Song getSong(){
        return this.song;
    }

    public void setSongUri(Uri uri){ this.mUri = uri; }

    public Uri getSongUri(){ return this.mUri; }
}
