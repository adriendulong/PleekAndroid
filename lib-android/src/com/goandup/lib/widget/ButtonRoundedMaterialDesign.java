package com.goandup.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.goandup.lib.R;

/**
 * TODO: document your custom view class.
 */
public class ButtonRoundedMaterialDesign extends RelativeLayout
{
    private RelativeLayout container;
    private ImageView circularImageView;
    private ImageView imagePicto;

    private OnPressedListener listener;
    private boolean initSize;

    public ButtonRoundedMaterialDesign(Context context)
    {
        this(context, null);
    }

    public ButtonRoundedMaterialDesign(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ButtonRoundedMaterialDesign(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        // load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ButtonRoundedMaterialDesign, defStyle, 0);

        int color = attributes.getColor(R.styleable.ButtonRoundedMaterialDesign_buttonColor, Color.BLUE);
        Drawable picto = attributes.getDrawable(R.styleable.ButtonRoundedMaterialDesign_buttonPicto);

        inflate(context, R.layout.button_rounded_material_design, this);

        container = (RelativeLayout)findViewById(R.id.container);
        circularImageView = (ImageView)findViewById(R.id.circularImageView);
        circularImageView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(listener != null) listener.endPress(ButtonRoundedMaterialDesign.this);
            }
        });
        Drawable imgDown = getContext().getResources().getDrawable(R.drawable.btn_rounded_down);
        Drawable imgUp = getContext().getResources().getDrawable(R.drawable.btn_rounded);
        circularImageView.setOnTouchListener(new DownTouchListener(imgDown, imgUp));
        imagePicto = (ImageView)findViewById(R.id.imagePicto);
        imagePicto.setImageDrawable(picto);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //Measure Width
        if (!initSize && widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY)
        {
            circularImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            initSize = true;
        }

    }

    public void setOnPressedListener(OnPressedListener listener) {
        this.listener = listener;
    }

    public interface OnPressedListener
    {
        //public void startPress(ButtonRoundedMaterialDesign button);
        public void endPress(ButtonRoundedMaterialDesign button);
    }
}
