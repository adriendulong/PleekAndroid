/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pleek.app.views;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.goandup.lib.utile.Screen;
import com.goandup.lib.widget.TextViewFont;
import com.pleek.app.R;
import com.pleek.app.adapter.EmojisFontsAdapter;
import com.pleek.app.bean.Emoji;
import com.pleek.app.bean.Overlay;
import com.pleek.app.utils.PicassoUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * @author Ankush Sachdeva (sankush@yahoo.co.in).
 */

public class EmojisFontsPopup<T> extends PopupWindow {

    public static final int POPUP_FONTS = 0;
    public static final int POPUP_STICKERS = 1;

    private int mType;

	private EmojisFontsAdapter mEmojisFontsAdapter;
	private int keyBoardHeight = 0;
	private Boolean pendingOpen = false;
	private Boolean isOpened = false;
    private OnEmojiFontClickListener mOnEmojiFontClickedListener;
	private OnSoftKeyboardOpenCloseListener mOnSoftKeyboardOpenCloseListener;
	private View mRootView;
	private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<T> mData;
	private RecyclerView mEmojisFontsRecycler;
    private int mKeyboardHeight;

	/**
	 * Constructor
	 * @param rootView	The top most layout in your view hierarchy. The difference of this view and the screen height will be used to calculate the keyboard height.
	 * @param context The context of current activity.
	 * @param data
     */
	public EmojisFontsPopup(View rootView, Context context, List<T> data, int type, int keyBoardHeight) {
		super(context);
		this.mContext = context;
		this.mRootView = rootView;
        this.mData = data;
        this.mType = type;
        this.mKeyboardHeight = keyBoardHeight;
		View customView = createCustomView();
		setContentView(customView);
		setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.secondColor)));

        //if (mData.size() % 2 == 1 && type == POPUP_STICKERS) {
        //    mData.add(null);
        //}
	}

	/**
	 * Set the listener for the event of keyboard opening or closing.
	 */
	public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
		this.mOnSoftKeyboardOpenCloseListener = listener;
	}

	/**
	 * Use this function to show the emoji / font popup.
	 * NOTE: Since, the soft keyboard sizes are variable on different android devices, the 
	 * library needs you to open the soft keyboard atleast once before calling this function.
	 * If that is not possible see showAtBottomPending() function.
	 * 
	 */
	public void showAtBottom(){
		showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
        mEmojisFontsRecycler.setAdapter(mEmojisFontsAdapter);
	}

	/**
	 * Use this function when the soft keyboard has not been opened yet. This 
	 * will show the emoji popup after the keyboard is up next time.
	 * Generally, you will be calling InputMethodManager.showSoftInput function after 
	 * calling this function.
	 */ 
	public void showAtBottomPending() {
		if (isKeyBoardOpen())
			showAtBottom();
		else
			pendingOpen = true;
	}

	/**
	 * 
	 * @return Returns true if the soft keyboard is open, false otherwise.
	 */
	public Boolean isKeyBoardOpen(){
		return isOpened;
	}

	/**
	 * Dismiss the popup
	 */
	@Override
	public void dismiss() {
        mEmojisFontsAdapter.setSelectedId("");
		super.dismiss();
	}

	/**
	 * Call this function to resize the emoji / font popup according to your soft keyboard size
	 */
	public void setSizeForSoftKeyboard() {
		mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
                mRootView.getWindowVisibleDisplayFrame(r);

				int screenHeight = mRootView.getRootView()
						.getHeight();
				int heightDifference = screenHeight
						- (r.bottom - r.top);
				int resourceId = mContext.getResources()
						.getIdentifier("status_bar_height",
								"dimen", "android");
				if (resourceId > 0) {
					heightDifference -= mContext.getResources().getDimensionPixelSize(resourceId);
				}

				if (heightDifference > 100) {
					keyBoardHeight = heightDifference;
					setSize(LayoutParams.MATCH_PARENT, keyBoardHeight);
					if (isOpened == false) {
						if (mOnSoftKeyboardOpenCloseListener != null)
                            mOnSoftKeyboardOpenCloseListener.onKeyboardOpen(keyBoardHeight);
					}
                    updateAdapter(keyBoardHeight);

					isOpened = true;

					if (pendingOpen) {
						showAtBottom();
						pendingOpen = false;
					}
				} else {
					isOpened = false;
					if (mOnSoftKeyboardOpenCloseListener != null)
                        mOnSoftKeyboardOpenCloseListener.onKeyboardClose();
				}
			}
		});
	}

	/**
	 * Manually set the popup window size
	 * @param width Width of the popup
	 * @param height Height of the popup
	 */
	public void setSize(int width, int height){
		setWidth(Screen.getInstance(mContext).getWidth());
		setHeight(height);
	}

	private View createCustomView() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_emoji_font, null, false);
		mEmojisFontsRecycler = (RecyclerView) view.findViewById(R.id.recyclerViewEmojisFonts);
        mEmojisFontsRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
		mEmojisFontsAdapter = new EmojisFontsAdapter(mContext, mData, mType, mKeyboardHeight);
        mEmojisFontsAdapter.setOnEmojiFontClickListener(mOnEmojiFontClickedListener);
        mEmojisFontsRecycler.setAdapter(mEmojisFontsAdapter);

		return view;
	}

    public OnEmojiFontClickListener getOnEmojiFontClickedListener() {
        return mOnEmojiFontClickedListener;
    }

    public void setOnEmojiFontClickedListener(OnEmojiFontClickListener onEmojiFontClickedListener) {
        this.mOnEmojiFontClickedListener = onEmojiFontClickedListener;

        if (mEmojisFontsAdapter != null) {
            mEmojisFontsAdapter.setOnEmojiFontClickListener(onEmojiFontClickedListener);
        }
    }

	public interface OnSoftKeyboardOpenCloseListener {
		void onKeyboardOpen(int keyBoardHeight);
		void onKeyboardClose();
	}

    public interface OnEmojiFontClickListener {
        void onEmojiFontClick(Overlay overlay, float size);
    }

    public void updateAdapter(int keyBoardHeight) {
        this.keyBoardHeight = keyBoardHeight;

        if (mEmojisFontsAdapter != null) {
            mEmojisFontsAdapter.setKeyboardHeight(keyBoardHeight);
            mEmojisFontsRecycler.post(new Runnable() {
                @Override
                public void run() {
                    mEmojisFontsAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}