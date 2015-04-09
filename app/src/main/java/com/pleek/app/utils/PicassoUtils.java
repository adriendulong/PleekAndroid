package com.pleek.app.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

public class PicassoUtils {
	
    private static Picasso singleton = null;
    private static LruCache mCache = null;

    public static Picasso with(Context context) {
        // Mimicking Picasso's new OkHttpLoader(context), but with our custom OkHttpClient
        if (singleton == null) {
            OkHttpClient client = createClient();

            try {
                client.setCache(createResponseCache(context));
            } catch (IOException ignored) {
            
            }

            Picasso.Builder builder = new Picasso.Builder(context);
            mCache = new LruCache(context);
            builder.memoryCache(mCache);
            builder.downloader(new OkHttpDownloader(client));
            singleton = builder.build();
            singleton.setDebugging(true);
        }
        return singleton;
    }
    
    private static OkHttpClient createClient() {
        OkHttpClient client = new OkHttpClient();

        // Working around the libssl crash: https://github.com/square/okhttp/issues/184
        SSLContext sslContext;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }

        client.setSslSocketFactory(sslContext.getSocketFactory());

        return client;
    }
    
    private static File createDefaultCacheDir(Context context) {
        try {
            final Class<?> clazz = Class.forName("com.squareup.picasso.Utils");
            final Method method = clazz.getDeclaredMethod("createDefaultCacheDir", Context.class);
            method.setAccessible(true);
            return (File)method.invoke(null, context);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static long calculateDiskCacheSize(File dir) {
        try {
            final Class<?> clazz = Class.forName("com.squareup.picasso.Utils");
            final Method method = clazz.getDeclaredMethod("calculateDiskCacheSize", File.class);
            method.setAccessible(true);
            return (Long)method.invoke(null, dir);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Cache createResponseCache(Context context) throws IOException {
        File cacheDir = createDefaultCacheDir(context);
        long maxSize = calculateDiskCacheSize(cacheDir);
        
        if (DataUtils.dirSize(cacheDir) >= 104857600) { // 100MB
        	DataUtils.deleteFile(cacheDir);
        	cacheDir = createDefaultCacheDir(context);
            maxSize = calculateDiskCacheSize(cacheDir);
        }
        
        return new Cache(cacheDir, maxSize);
    }
}