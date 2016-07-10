package com.pervasive.sth.smarttreasurehunt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class TreasureCaught extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_caught);

        String picName = getIntent().getStringExtra("WINNER_PICTURE");
        ImageView winnerImageView = (ImageView) findViewById(R.id.winner_imageview);

        File picFile = new File(picName);
        if  ( picFile.exists() ) {
            Bitmap bmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
            winnerImageView.setImageBitmap(bmap);
        }
    }

    public void onWinnerButtonClick(View view) {
        finish();
    }
}
