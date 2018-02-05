package edu.brown.engn931.diningdashboard;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.CardHeader;

public class DiningCardHeader extends CardHeader {

    public DiningCardHeader(Context context) {
        this(context, R.layout.dining_inner_base_header);
    }

    public DiningCardHeader(Context context, int innerBaseHeader) {
        super(context, innerBaseHeader);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Add simple title to header
        if (view != null) {
            TextView mTitleView = (TextView) view.findViewById(R.id.project_card_header_inner_simple_title);
            if (mTitleView != null)
                mTitleView.setText(mTitle);
        }

    }

}
