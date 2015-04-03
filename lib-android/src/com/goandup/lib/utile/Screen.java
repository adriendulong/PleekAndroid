package com.goandup.lib.utile;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import com.goandup.lib.R;

public class Screen
{
	private static Screen me;
	private Context context;
	
	public static Screen getInstance(Context context)
	{
		if(me == null)
		{
			me = new Screen(context);
		}
		return me;
	}
	
	public Screen(Context context)
	{
		this.context = context;
	}

	public int pourcentWidthToPx(float pourcent)
	{
		return pourcentWidthToPx(pourcent, context);
	}
	public static int pourcentWidthToPx(float pourcent, Context context)
	{
		return (int) (getWidthDp(context) / 100f * pourcent);
	}

	public int pourcentHeightToPx(float pourcent)
	{
		return pourcentHeightToPx(pourcent, context);
	}
	public static int pourcentHeightToPx(float pourcent, Context context)
	{
		return (int) (getHeightDp(context) / 100f * pourcent);
	}
	
	public int dpToPx(int dp)
	{
		return dpToPx(dp, context);
	}
	public static int dpToPx(int dp, Context context)
	{
		if(context == null) return dp;//error
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public int getWidth()
	{
		return getWidth(context);
	}
	@SuppressWarnings("deprecation")
	public static int getWidth(Context context)
	{
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = 0;
		try
		{
			// Try to get size without the Status bar, if we can (API level 13)
			Method getSizeMethod = display.getClass().getMethod("getSize", Point.class);
			Point pt = new Point();
			getSizeMethod.invoke( display, pt );
			width = pt.x;
		}
		catch (Exception ignore)
		{
			// Use older APIs
			width = display.getWidth();
		}
		return width;
	}

	public int getWidthScreen()
	{
		return getWidthScreen(context);
	}
	public static int getWidthScreen(Context context)
	{
		if(isVertical(context)) return getWidth(context);
		return getHeight(context);
	}

	public int getHeight()
	{
		return getHeight(context);
	}
	@SuppressWarnings("deprecation")
	public static int getHeight(Context context)
	{
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int height = 0;
		try
		{
			// Try to get size without the Status bar, if we can (API level 13)
			Method getSizeMethod = display.getClass().getMethod("getSize", Point.class);
			Point pt = new Point();
			getSizeMethod.invoke( display, pt );
			height = pt.y;
		}
		catch (Exception ignore)
		{
			// Use older APIs
			height = display.getHeight();
		}
		return height;
	}

	public int getHeightScreen()
	{
		return getHeightScreen(context);
	}
	public static int getHeightScreen(Context context)
	{
		if(isVertical(context)) return getHeight(context);
		return getWidth(context);
	}

	public float getDensity()
	{
		return getDensity(context);
	}
	public static float getDensity(Context context)
	{
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return metrics.density;
	}

	public int getWidthDp()
	{
		return getWidthDp(context);
	}
	public static int getWidthDp(Context context)
	{
		int widthPx = getWidth(context);
		float density = getDensity(context);
		return (int) (((double) widthPx / density) + 0.5);
	}

	public int getHeightDp()
	{
		return getHeightDp(context);
	}
	public static int getHeightDp(Context context)
	{
		int heightPx = getHeight(context);
		float density = getDensity(context);
		return (int) (((double) heightPx / density) + 0.5);
	}

	public int getOrientation()
	{
		return getOrientation(context);
	}

	public static int getOrientation(Context context)
	{
		return context.getResources().getConfiguration().orientation;
	}

	public boolean isHorizontal()
	{
		return isHorizontal(context);
	}
	public static boolean isHorizontal(Context context)
	{
		return getWidth(context) > getHeight(context);
//		return getOrientation(context) == Configuration.ORIENTATION_LANDSCAPE;
	}

	public boolean isVertical()
	{
		return isVertical(context);
	}
	public static boolean isVertical(Context context)
	{
		return !isHorizontal(context);
//		return getOrientation(context) == Configuration.ORIENTATION_PORTRAIT;
	}

	public boolean isQuatreTiers()
	{
		return isQuatreTiers(context);
	}
	public static boolean isQuatreTiers(Context context)
	{
		return getWidth(context) * 4 == getHeight(context) * 3;
	}

	public boolean isTablet()
	{
		return isTablet(context);
	}
	public static boolean isTablet(Context context) {
	    return (context.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
    public View adapt(View view)
    {
        return Screen.adapt(view, context);
    }
    public static View adapt(View view, final Context c)
    {
        if(view != null)
        {
            Object tag = view.getTag(R.string.app_name);
            float oldRatio = (Float) (tag != null && tag instanceof Float ? tag : 0f);

            float ratio =  (float) (getWidthDp(c) / 320.0);
            if(ratio != oldRatio)
            {
                // Marque la vue comme adaptée.
                // Permet aussi de stoquer le ratio utilisé pour redimensionner cette vue,
                // cela permet de pouvoir recalculé la largeur initial en 320
                view.setTag(R.string.app_name, new Float(ratio));

                // pour récupérer la largeur d'une vue en 320
                if(oldRatio != 0) ratio = ratio / oldRatio;

                ViewGroup.LayoutParams params = view.getLayoutParams();
                // width & height
                if(params != null)
                {
                    params.width = params.width > 0 ? (int) (params.width * ratio) : params.width;
                    params.height = params.height > 0 ? (int) (params.height * ratio) : params.height;
                }
                // margin
                if(params != null && params instanceof ViewGroup.MarginLayoutParams)
                {
                    ViewGroup.MarginLayoutParams paramsM = (ViewGroup.MarginLayoutParams)params;
                    paramsM.leftMargin = (int) (paramsM.leftMargin * ratio);
                    paramsM.topMargin = (int) (paramsM.topMargin * ratio);
                    paramsM.rightMargin = (int) (paramsM.rightMargin * ratio);
                    paramsM.bottomMargin = (int) (paramsM.bottomMargin * ratio);
                }
                //textSize
                if(view instanceof TextView)
                {
                    ((TextView)view).setTextSize(((TextView)view).getTextSize() * ratio / getDensity(c));
                }
                //WebView
                if(view instanceof WebView)
                {
                    ((WebView)view).setInitialScale((int) (100f * ratio * getDensity(c)));
                }
                // padding
                int paddingLeft = (int) (view.getPaddingLeft() * ratio);
                int paddingTop = (int) (view.getPaddingTop() * ratio);
                int paddingRight = (int) (view.getPaddingRight() * ratio);
                int paddingBottom = (int) (view.getPaddingBottom() * ratio);
                view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

                //on rappel la méthode pour tous les enfants.
                if(view instanceof ViewGroup)
                {
                    ViewGroup vg = (ViewGroup) view;
                    for (int i = 0; i < vg.getChildCount(); i++)
                    {
                        adapt(vg.getChildAt(i), c);
                    }
                }
            }
        }
        return view;
    }
}
