package com.goandup.lib.utile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

/**
 * 
 * @author nicolas
 *
 */
public class Utile
{
	/**
	 * 
	 * @param fileName
	 * @param data
	 * @param context
	 * @return
	 */
	public static boolean writeFile(String fileName, String data, Context context)
	{
		if(fileName == null || context == null) return false;

		if(data != null)
		{
			OutputStreamWriter outputStreamWriter = null;
			try
			{
				outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
				outputStreamWriter.write(data);
				return true;
			}
			catch (IOException e)
			{
				L.e("File write failed: " + e.toString());
			} 
			finally
			{
				if(outputStreamWriter != null)
				{
					try {
						outputStreamWriter.close();
					} catch (IOException e) {}
				}
			}
		}
		else
		{
			return context.deleteFile(fileName);
		}
		return false;
	}

	/**
	 * 
	 * @param url
	 * @param listener
	 * @return
	 */
	public static Thread readWebFile(final String url, final ReadWebFileListener listener)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					DefaultHttpClient httpclient = new DefaultHttpClient();
					HttpGet httppost = new HttpGet(url);
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity ht = response.getEntity();
					BufferedHttpEntity buf = new BufferedHttpEntity(ht);
					InputStream is = buf.getContent();
					BufferedReader r = new BufferedReader(new InputStreamReader(is));
					StringBuilder total = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null)
					{
						total.append(line + "\n");
					}
					listener.readWebFileEnd(total.toString());
				}
				catch (Exception e)
				{
					L.w("Utile.readWebFile - Can not read file - url=["+url+"] - e=[" + e + "]");
				}
			}
		});
		
		thread.start();
		
		return thread;
	}
	
	/**
	 * 
	 * @author nicolas
	 *
	 */
	public interface ReadWebFileListener
	{
		public void readWebFileEnd(String content);
	}

	/**
	 * 
	 * @param fileName
	 * @param context
	 * @return
	 */
	public static String readFile(String fileName, Context context)
	{
		return readFile(fileName, null, context);
	}

	/**
	 * 
	 * @param fileName
	 * @param defaut
	 * @param context
	 * @return
	 */
	public static String readFile(String fileName, String defaut, Context context)
	{
		InputStream inputStream = null;
		try
		{
			if(context.getFileStreamPath(fileName).isFile())
			{
				inputStream = context.openFileInput(fileName);
			}
			else
			{
				inputStream = context.getAssets().open(fileName);
			}

			if ( inputStream != null )
			{
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString;
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null)
				{
					if(stringBuilder.length() > 0) stringBuilder.append("\n");
					stringBuilder.append(receiveString);
				}

				return stringBuilder.toString();
			}
		}
		catch (Exception e)
		{
			L.w("Utile.readFile - Can not read file - url=["+fileName+"] - e=[" + e + "]");
		}
		finally
		{
			if(inputStream != null)
			{
				try {
					inputStream.close();
				} catch (IOException e) {}
			}
		}
		return defaut;
	}

	/**
	 * @param url
	 * @return
	 */
	public static boolean isValidUrl(String url)
	{
		if(url == null) return false;
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {}
		return false;
	}

	/**
	 * 
	 * @param num
	 * @return
	 */
	public static boolean isNumber(String num)
	{
		if(num == null) return false;
		try
		{
			Integer.parseInt(num);
			return true;
		}
		catch(NumberFormatException e) {}
		return false;
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static int toInt(String str)
	{
		return toInt(str, 0);
	}

	/**
	 * 
	 * @param str
	 * @param defautVal
	 * @return
	 */
	public static int toInt(String str, int defautVal)
	{
		if(str == null) return defautVal;

		int i = defautVal;
		try
		{
			i = Integer.parseInt(str);
		}
		catch(NumberFormatException e) {}
		return i;
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static float toFloat(String str)
	{
		return toFloat(str, 0f);
	}

	/**
	 * 
	 * @param str
	 * @param defautVal
	 * @return
	 */
	public static float toFloat(String str, float defautVal)
	{
		if(str == null) return defautVal;

		float f = defautVal;
		try
		{
			f = Float.parseFloat(str);
		}
		catch(NumberFormatException e) {}
		return f;
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean toBool(String str)
	{
		return str != null && ("1".equals(str.trim()) || "true".equals(str.trim())); 
	}

	//GETTER JSON param;
	/**
	 * 
	 * @param key
	 * @param json
	 * @return
	 */
	public static String get(String key, JSONObject json)
	{
		if(json == null || key == null) return null;
		if(!json.containsKey(key)) return null;
		return (String) json.get(key);
	}

	/**
	 * 
	 * @param key
	 * @param json
	 * @return
	 */
	public static boolean getB(String key, JSONObject json)
	{
		return toBool(get(key, json));
	}

	/**
	 * 
	 * @param key
	 * @param json
	 * @return
	 */
	public static float getF(String key, JSONObject json)
	{
		return toFloat(get(key, json));
	}

	/**
	 * 
	 * @param key
	 * @param json
	 * @return
	 */
	public static int getI(String key, JSONObject json)
	{
		return toInt(get(key, json));
	}
	//END

	private static ProgressDialog progressDialog;

	/**
	 * 
	 * @param context
	 */
	public static void showProgress(Context context)
	{
		showProgress("Chargement...", context);
	}

	/**
	 * 
	 * @param context
	 */
	public static void showProgress(String text, Context context)
	{
		progressDialog = ProgressDialog.show(context, "", text);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		
		Timer timer = new Timer(true);
		timer.schedule(new ProgressDialogTimerTask(progressDialog), 30000);
	}

	/**
	 * 
	 * @author nicolas
	 *
	 */
	static class ProgressDialogTimerTask extends TimerTask
	{
		private ProgressDialog progressDialog;
		
		public ProgressDialogTimerTask(ProgressDialog progressDialog)
		{
			super();
			this.progressDialog = progressDialog;
		}
		
		@Override
		public void run()
		{
			progressDialog.cancel();
		}
	}
	
	/**
	 * 
	 */
	public static void hideProgress()
	{
		if(progressDialog != null) progressDialog.cancel();
		progressDialog = null;
	}

	//// DUMP
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static String dump(List<?> list)
	{
		String line = "[";
		if(list != null)
		{
			for (int i = 0; i < list.size(); i++)
			{
				line += list.get(i);
				if(i < list.size() - 1) line += ",";
			}
		}
		return line + "]";
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	public static String dump(Map<?, ? extends List<?>> map)
	{
		String line = "[";
		Iterator<?> it = map.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<?,?> pairs = (Entry<?,?>)it.next();
			line += pairs.getKey() + ":" + dump((List<?>) pairs.getValue());
			line += " - ";
		}
		return line + "]";
	}
	//END
	
	//// TOAST
	/**
	 * 
	 * @param idRessource
	 * @param context
	 */
	public static void showToast(final int idRessource, final Context context)
	{
        if(context == null && !(context instanceof Activity)) return;
		showToast(context.getResources().getString(idRessource), context);
	}
	
	/**
	 * 
	 * @param text
	 * @param context
	 */
	public static void showToast(final String text, final Context context)
	{
        if(context == null && !(context instanceof Activity)) return;
		((Activity)context).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
		});
	}
	//END
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context)
	{
		try
		{
			return  context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		}
		catch (Exception e) { }
		
		return 0;
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context)
	{
		try
		{
			return  context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		}
		catch (Exception e) { }
		
		return "0.0";
	}
	
	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isSameDay(Date date1, Date date2)
	{
        if (date1 == null || date2 == null)
        {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
	
	/**
	 * 
	 * @param uri
	 * @param context
	 * @return
	 */
	public static boolean isAppInstalled(String uri, Context context)
    {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try
        {
        	pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
        	app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
        	app_installed = false;
        }
        return app_installed ;
    }

	/**
	 * 
	 * @param appName
	 * @param context
	 * @return
	 */
	public static boolean isAppBackground(String appName, Context context)
	{
	    ActivityManager am = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
	    Iterator<RunningAppProcessInfo> i = l.iterator();
	    while(i.hasNext()) 
	    {
	          ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
	          if(info.processName.equals(appName))
	          {
	        	  return true;
	          }
	    }

	    return false;
	}

	/**
	 * 
	 * @param appName
	 * @param context
	 */
	public static void killApp(String appName, Context context)
	{
	    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

		L.e(">>>> "+appName+" is on background : "+Utile.isAppBackground(appName, context));
		
		am.killBackgroundProcesses(appName);

		L.e(">>>> "+appName+" is on background : "+Utile.isAppBackground(appName, context));
	}
	
	/**
	 * 
	 * @param defaultLoc
	 * @param context
	 * @return
	 */
	public static Location getCurrentLocation(Location defaultLoc, Context context)
	{
		LocationManager service = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean enabledGPS = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //check GPS activity
        if (!enabledGPS && !enabledWiFi)
        {
        	Utile.showToast("Votre GPS semble désactivé", context);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        }

        //recupere location
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = lm.getBestProvider(new Criteria(), false);
        Location location = lm.getLastKnownLocation(provider);

        //return location
        return location != null ? location : defaultLoc;
	}
	
	/**
	 * 
	 * @param resImg
	 * @param context
	 * @return
	 */
	public static int[] getImgSize(int resImg, Context context)
	{
		BitmapFactory.Options dimensions = new BitmapFactory.Options(); 
		dimensions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), resImg, dimensions);
		int[] rep = new int[2];
		rep[0] = dimensions.outWidth;
		rep[1] = dimensions.outHeight;
		
		return rep;
	}
	
	/**
	 * @return
	 */
	public static int[] removeElements(int[] tab, int val) {
		
		List<Integer> tmp = new ArrayList<Integer>();

	    for(int i = 0; i < tab.length; i++)
	    {
	    	int el = tab[i];
	    	if(el != val)
	    	{
	    		tmp.add(Integer.valueOf(el));
	    	}
	    }
	    
		int[] result = new int[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			result[i] = tmp.get(i).intValue();
		}

	    return result;
	}
	
	public static String formatSeconde(int seconde)
	{
		Date date = new Date((long)(seconde*1000));
		String str = new SimpleDateFormat("mm:ss", Locale.FRANCE).format(date);
		if(seconde > 3600) str = seconde/3600 + ":" + str;
		return str;
	}
	
	public static void setAlpha(View view, float toAlpha){
		setAlpha(view, 1f, toAlpha, 0);
	}
	
	public static void setAlpha(View view, float fromAlpha, float toAlpha){
		setAlpha(view, fromAlpha, toAlpha, 0);
	}

    public static void setAlpha(View view, float fromAlpha, float toAlpha, int duration)
    {
        AlphaAnimation alpha = new AlphaAnimation(fromAlpha, toAlpha);
        alpha.setDuration(duration);
        alpha.setFillAfter(true);

        view.startAnimation(alpha);
        view.setVisibility(View.VISIBLE);
    }


    public static void fadeIn(View view, int duration) {
        fadeIn(view, duration, null);
    }
    public static void fadeIn(final View view, int duration, final Animation.AnimationListener listener)
    {
        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setDuration(duration);
        alpha.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationEnd(Animation animation) {
                if(listener != null) listener.onAnimationEnd(animation);
            }
            @Override public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
                if(listener != null) listener.onAnimationStart(animation);
            }
            @Override public void onAnimationRepeat(Animation animation) {
                if(listener != null) listener.onAnimationRepeat(animation);
            }
        });

        view.startAnimation(alpha);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOut(final View view, int duration) {
        fadeOut(view, duration, null);
    }
    public static void fadeOut(final View view, int duration, final Animation.AnimationListener listener)
    {
        AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
        alpha.setDuration(duration);
        alpha.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                if(listener != null) listener.onAnimationEnd(animation);
            }
            @Override public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
                if(listener != null) listener.onAnimationStart(animation);
            }
            @Override public void onAnimationRepeat(Animation animation) {
                if(listener != null) listener.onAnimationRepeat(animation);
            }
        });

        view.startAnimation(alpha);
        view.setVisibility(View.VISIBLE);
    }
	
	@SuppressLint("NewApi")
	public static int getBgColor(View view)
	{
        if(view == null) return 0;

		int color = Color.BLACK;
        Drawable background = view.getBackground();
        if(background instanceof ColorDrawable)
        {
			ColorDrawable colorDrawable = (ColorDrawable) background;  
            if(android.os.Build.VERSION.SDK_INT < 11)
            {
    			Bitmap bitmap = Bitmap.createBitmap(1, 1, Config.ARGB_4444);
    			Canvas canvas = new Canvas(bitmap);
    			colorDrawable.draw(canvas);   
    			color = bitmap.getPixel(0, 0);
    			bitmap.recycle();
            }
            else
            {
            	color = colorDrawable.getColor();
            }
        }
        return color;
	}

    public static int getTextHeight(String text, int maxWidth, float textSize, Typeface typeface, float interligne)
    {
        text = text.replaceAll("\\p{So}", "W ");//replace all unicode char by big letter (W )

        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);

        int lineCount = 0;

        int index = 0;
        int length = text.length();

        while(index <= length - 1) {
            index += paint.breakText(text, index, length, true, maxWidth, null);
            lineCount++;
        }

        Rect bounds = new Rect();
        paint.getTextBounds("Py", 0, 2, bounds);

        int newHeight = (int)Math.floor((lineCount * bounds.height() + (lineCount * bounds.height() * interligne)));

        return newHeight;
    }






    public static Bitmap fastblur(Bitmap sentBitmap, int radius)
    {
        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        L.e(w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                //me
                if(bitmap.getPixel(x, y) == Color.TRANSPARENT)
                {
                    bitmap.setPixel(x, y, Color.BLACK);
                }

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        L.e(w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


    public static boolean isDefaultOrientationLandscape(Context ctx)
    {
        WindowManager windowManager =  (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        Configuration config = ctx.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return true;
        } else {
            return false;
        }
    }

    /** Show an event in the LogCat view, for debugging */
    public static String eventToString(MotionEvent event)
    {
        String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" , "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_" ).append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid " ).append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")" );
        }
        sb.append("[" );
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#" ).append(i);
            sb.append("(pid " ).append(event.getPointerId(i));
            sb.append(")=" ).append((int) event.getX(i));
            sb.append("," ).append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";" );
        }
        sb.append("]" );
        return sb.toString();
    }



    public static void changeSwipeDownDistance(final SwipeRefreshLayout swipe)
    {
        changeSwipeDownDistance(swipe, 33);// 1/3 height of list
    }
    // http://stackoverflow.com/questions/22856754
    public static void changeSwipeDownDistance(final SwipeRefreshLayout swipe, int pourcentHeight)
    {
        final float ratioHeight = Math.max(Math.min(100, pourcentHeight),0) / 100f;

        if(swipe == null) return;
        swipe.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Float mDistanceToTriggerSync = swipe.getHeight() * ratioHeight;

                try {
                    // Set the internal trigger distance using reflection.
                    Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
                    field.setAccessible(true);
                    field.setFloat(swipe, mDistanceToTriggerSync);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Only needs to be done once so remove listener.
                ViewTreeObserver obs = swipe.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    public static String formatBigNumber(long number)
    {
        if(number < 0) return "0";
        if(number < 1000) return ""+number;
        if(number < 1000000) return (number/1000)+"k";
        return (number/1000000000)+"M";
    }
}
