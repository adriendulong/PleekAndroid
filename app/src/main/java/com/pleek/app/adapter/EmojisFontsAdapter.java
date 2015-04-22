package com.pleek.app.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.widget.TextViewFont;
import com.pleek.app.R;
import com.pleek.app.bean.Emoji;
import com.pleek.app.bean.Font;
import com.pleek.app.utils.PicassoUtils;
import com.pleek.app.views.EmojisFontsPopup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EmojisFontsAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> mViews;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mType;
    private int mKeyboardHeight;
    private String mSelectedId;
    private int mSize;
    private EmojisFontsPopup.OnEmojiFontClickListener mOnEmojiFontClickListener;

    public EmojisFontsAdapter(Context context, List<T> views, int type, int keyboardHeight) {
        this.mViews = views;
        this.mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mType = type;
        mKeyboardHeight = keyboardHeight;
        mSize = mKeyboardHeight / 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        final View view;

        if (mType == EmojisFontsPopup.POPUP_STICKERS) {
            view = mLayoutInflater.inflate(R.layout.row_emoji, parent, false);
        } else {
            view = mLayoutInflater.inflate(R.layout.row_font, parent, false);
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = mSize;
        params.height = mSize;
        view.setLayoutParams(params);

        if (mType == EmojisFontsPopup.POPUP_STICKERS) {
            return new EmojisFontsAdapter.EmojisViewHolder(view);
        } else {
            return new FontsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (mViews.get(position) instanceof Emoji) {
            EmojisViewHolder holderE = (EmojisViewHolder) holder;
            final Emoji emoji = (Emoji) mViews.get(position);

            if (emoji != null) {
                int onePx = (int) (1 * Screen.getInstance(mContext).getDensity());
                holder.itemView.setPadding(0, onePx, onePx, 0);

                PicassoUtils.with(mContext)
                        .load(emoji.getUrlPhoto())
                        .resize(mSize, mSize)
                        .into(holderE.imgEmoji);

                holderE.layoutEmoji.setBackgroundResource(emoji.getId().equals(mSelectedId) ? R.color.emojiFontBgSelected : R.color.emojiFontBgNormal);
                holderE.imgSelected.setVisibility(emoji.getId().equals(mSelectedId) ? View.VISIBLE : View.GONE);
                holderE.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!emoji.getId().equals(mSelectedId)) {
                            mSelectedId = emoji.getId();
                        } else {
                            mSelectedId = "";
                        }

                        notifyDataSetChanged();

                        if (mOnEmojiFontClickListener != null) {
                            mOnEmojiFontClickListener.onEmojiFontClick(mSelectedId.equals("") ? null : emoji);
                        }
                    }
                });
            } else {
                holderE.layoutEmoji.setBackgroundResource(R.color.emojiFontBgNormal);
                holderE.imgEmoji.setImageDrawable(null);
                holderE.imgSelected.setVisibility(View.GONE);
            }
        } else if (mViews.get(position) instanceof Font) {
            FontsViewHolder holderF = (FontsViewHolder) holder;
            final Font font = (Font) mViews.get(position);

            if (font != null) {
                int onePx = (int) (1 * Screen.getInstance(mContext).getDensity());
                holderF.itemView.setPadding(0, onePx, onePx, 0);
                holderF.txtFont.setCustomFont(mContext, font.getName());
                holderF.txtFont.setVisibility(View.VISIBLE);

                Rect bounds = new Rect();
                holderF.txtFont.getPaint().getTextBounds("a", 0, 1, bounds);
                System.out.println("HEIGHT : " + bounds.height());

                ViewGroup.LayoutParams params = holderF.txtFont.getLayoutParams();
                params.width = mSize;
                params.height = mSize;
                holderF.txtFont.setLayoutParams(params);

                ViewGroup.LayoutParams params2 = holderF.layoutFont.getLayoutParams();
                params2.width = mSize;
                params2.height = mSize;
                holderF.layoutFont.setLayoutParams(params2);

                holderF.layoutFont.setBackgroundResource(font.getName().equals(mSelectedId) ? R.color.emojiFontBgSelected : R.color.emojiFontBgNormal);
                holderF.imgSelected.setVisibility(font.getName().equals(mSelectedId) ? View.VISIBLE : View.GONE);
                holderF.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!font.getName().equals(mSelectedId)) {
                            mSelectedId = font.getName();
                        } else {
                            mSelectedId = "";
                        }

                        notifyDataSetChanged();

                        if (mOnEmojiFontClickListener != null) {
                            mOnEmojiFontClickListener.onEmojiFontClick(mSelectedId.equals("") ? null : font);
                        }
                    }
                });
            } else {
                holderF.layoutFont.setBackgroundResource(R.color.emojiFontBgNormal);
                holderF.imgSelected.setVisibility(View.GONE);
                holderF.txtFont.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return mViews.size();
    }

    class EmojisViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.layoutEmoji)
        FrameLayout layoutEmoji;
        @InjectView(R.id.imgEmoji)
        ImageView imgEmoji;
        @InjectView(R.id.imgSelected)
        ImageView imgSelected;

        public EmojisViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    class FontsViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.layoutFont)
        RelativeLayout layoutFont;
        @InjectView(R.id.imgSelected)
        ImageView imgSelected;
        @InjectView(R.id.txtFont)
        TextViewFont txtFont;

        public FontsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    public void setOnEmojiFontClickListener(EmojisFontsPopup.OnEmojiFontClickListener listener) {
        this.mOnEmojiFontClickListener = listener;
    }

    public String getSelectedId() {
        return mSelectedId;
    }

    public void setSelectedId(String selectedId) {
        this.mSelectedId = selectedId;
    }
}
