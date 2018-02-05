package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCardThumbnail;

public class DiningCardThumbnail extends MaterialLargeImageCardThumbnail {

    public DiningCardThumbnail(Context context) {
        super(context);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View imageView) {
        //Call the super method
        super.setupInnerViewElements(parent, imageView);

        //Your code here
        mTitleOverImageView = (TextView) parent.findViewById(default_text_id);
        mTitleOverImageView.setTextColor(Color.WHITE);
    }
}