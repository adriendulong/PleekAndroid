package com.pleek.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.Screen;
import com.pleek.app.R;
import com.pleek.app.bean.Emoji;
import com.pleek.app.utils.PicassoUtils;
import com.pleek.app.views.EmojisFontsPopup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EmojisFontsAdapter<T> extends RecyclerView.Adapter<EmojisFontsAdapter.EmojisViewHolder> {

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
    public EmojisFontsAdapter.EmojisViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        final View view = mLayoutInflater.inflate(R.layout.row_emoji, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = mSize;
        params.height = mSize;
        view.setLayoutParams(params);

        return new EmojisFontsAdapter.EmojisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EmojisFontsAdapter.EmojisViewHolder holder, int position) {
        final Emoji emoji = (Emoji) mViews.get(position);

        if (emoji != null) {
            int onePx = (int) (1 * Screen.getInstance(mContext).getDensity());
            holder.itemView.setPadding(0, onePx, onePx, 0);

            PicassoUtils.with(mContext)
                    .load(emoji.getUrlPhoto())
                    .resize(mSize, mSize)
                    .into(holder.imgEmoji);

            holder.layoutEmoji.setBackgroundResource(emoji.getId().equals(mSelectedId) ? R.color.emojiFontBgSelected : R.color.emojiFontBgNormal);
            holder.imgSelected.setVisibility(emoji.getId().equals(mSelectedId) ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
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
            holder.layoutEmoji.setBackgroundResource(R.color.emojiFontBgNormal);
            holder.imgEmoji.setImageDrawable(null);
            holder.imgSelected.setVisibility(View.GONE);
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
