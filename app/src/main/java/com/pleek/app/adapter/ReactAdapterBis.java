package com.pleek.app.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.widget.TextViewFont;
import com.pleek.app.R;
import com.pleek.app.bean.Reaction;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tiago on 05/04/15.
 */
public class ReactAdapterBis extends RecyclerView.Adapter<ReactViewHolder> {

    public static int NB_COLUMN = 3;

    private List<Reaction> mItems;
    private Listener mListener;
    private Context mContext;
    private Screen mScreen;

    int oneDp, heightPiki, widthPiki;


    public ReactAdapterBis(List<Reaction> listReact, Listener listener) {
        this(listReact, listener, listener instanceof Context ? (Context) listener : null);
    }

    public ReactAdapterBis(List<Reaction> items, Listener listener, Context context) {
        this.mItems = items;
        this.mListener = listener;
        this.mContext = context;

        mScreen = Screen.getInstance(mContext);

        heightPiki = mScreen.getWidth() / NB_COLUMN;
        widthPiki = heightPiki;
    }

    @Override
    public ReactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View row = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_react, viewGroup, false);

        mContext = viewGroup.getContext();
        ReactViewHolder vh = new ReactViewHolder(row);

        //row.getLayoutParams().height = heightPiki;

        return vh;
    }

    @Override
    public void onBindViewHolder(ReactViewHolder reactViewHolder, final int i) {
        boolean isPlaceHolder = false;

        if (reactViewHolder.imgReact != null) {
            reactViewHolder.imgReact.setVisibility(View.VISIBLE);
            reactViewHolder.progressBar.setVisibility(View.GONE);
            reactViewHolder.imgError.setVisibility(View.GONE);
            reactViewHolder.imgMute.setVisibility(View.GONE);
            reactViewHolder.txtUserName.setVisibility(View.GONE);

            if (i >= 1 && i <= mItems.size()) {
                Reaction react = (Reaction) getItem(i);
                reactViewHolder.txtUserName.setText("@" + react.getNameUser());

                if (react.getUrlPhoto() != null) { //is Parse React
                    Drawable drawablePlaceHolder = new BitmapDrawable(mContext.getResources(), react.getTmpPhoto());
                    Picasso.with(mContext)
                            .load(react.getUrlPhoto()).fit()
                            .placeholder(drawablePlaceHolder).into(reactViewHolder.imgReact);

                    reactViewHolder.imgPlay.setVisibility(react.isVideo() ? View.VISIBLE : View.GONE);
                    react.loadVideoToTempFile(mContext);
                } else { // is tmp React, between creating and uploaded
                    reactViewHolder.imgReact.setImageBitmap(react.getTmpPhoto());
                    reactViewHolder.imgPlay.setVisibility(View.GONE);
                    reactViewHolder.progressBar.setVisibility(react.isLoadError() ? View.GONE : View.VISIBLE);

                    if(react.isLoadError()) {
                        reactViewHolder.imgError.setVisibility(View.VISIBLE);
                        reactViewHolder.imgError.setImageResource(R.drawable.picto_reload);
                    }
                }
            } else { // is placeholder item
                isPlaceHolder = true;
                reactViewHolder.imgReact.setImageDrawable(null);
                reactViewHolder.imgReact.setBackgroundColor(mContext.getResources().getColor(i % 2 != 0 ? R.color.grisFondPikiPlaceholder1 : R.color.grisFondPikiPlaceholder2));
                reactViewHolder.imgReact.setOnTouchListener(null);
                reactViewHolder.imgPlay.setVisibility(View.GONE);
                reactViewHolder.txtUserName.setText("");
            }
        }

        //if (!isPlaceHolder && i > 1) {
        //    itemReact.setTag(new Integer(numItem - 1));
        //    itemReact.setTag(R.string.tag_downpresse, new DownRunnable(itemReact));
        //    itemReact.setTag(R.string.tag_longpresse, new LongpressRunnable());
        //    itemReact.setOnTouchListener(this);
        //}
    }

    public List<Reaction> getReactList() {
        List<Reaction> reactions = new ArrayList<Reaction>();

        for (int i = 1; i < mItems.size(); i++) {
            reactions.add((Reaction) mItems.get(i));
        }

        return reactions;
    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }

        return mItems.size();
    }

    public Reaction getItem(int position) {
        return mItems.get(position);
    }

    public void appendReactions(List<Reaction> reactions) {
        mItems.addAll(reactions);
        notifyDataSetChanged();
    }

    public List<Reaction> addReact(Reaction react)
    {
        return addReact(react, null);
    }
    public List<Reaction> addReact(Reaction react, Reaction oldReact) {
        mItems.remove(oldReact);
        mItems.add(0, react);
        notifyDataSetChanged();
        return mItems;
    }

    public void setListReact(List<Reaction> listReact) {
        this.mItems = listReact;
        notifyDataSetChanged();
    }

    public boolean markLoadError(Reaction errorReact, boolean error) {
        int i = mItems.indexOf(errorReact);

        if(i >= 0) {
            mItems.get(i).setLoadError(error);
            notifyDataSetChanged();
        }

        return i >= 0;
    }

    public Reaction removeReaction(int position) {
        Reaction removed = mItems.remove(position);
        notifyDataSetChanged();
        return removed;
    }

    // INTERFACE
    public interface Listener {
        public void clickOnReaction(Reaction react);
        public void doubleTapReaction(Reaction react);
        public void showPlaceHolderItem(boolean show);
    }
}

class ReactViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.back)
    RelativeLayout layoutBack;
    @InjectView(R.id.imgActionOff)
    ImageView imgActionOff;
    @InjectView(R.id.imgActionOn)
    ImageView imgActionOn;
    @InjectView(R.id.front)
    RelativeLayout layoutFront;
    @InjectView(R.id.imgReact)
    ImageView imgReact;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.imgPlay)
    ImageView imgPlay;
    @InjectView(R.id.imgMute)
    ImageView imgMute;
    @InjectView(R.id.imgError)
    ImageView imgError;
    @InjectView(R.id.txtUserName)
    TextViewFont txtUserName;
    @InjectView(R.id.layoutOverlay)
    RelativeLayout layoutOverlay;


    public ReactViewHolder(View itemView) {
        super(itemView);

        ButterKnife.inject(this, itemView);
    }
}
