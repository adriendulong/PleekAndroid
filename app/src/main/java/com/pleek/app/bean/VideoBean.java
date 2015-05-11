package com.pleek.app.bean;

import android.content.Context;

import com.goandup.lib.utile.L;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ProgressCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 * Created by tiago on 14/04/2015.
 */
public class VideoBean implements Serializable {

    protected String id;
    protected ParseObject parseObject;
    protected String urlVideo;

    protected LoadVideoEndListener loadVideoEndListener;
    protected LoadVideoProgressListener loadVideoProgressListener;
    protected boolean isLoading;
    protected boolean isLoaded;
    protected boolean isLoadError;

    public String getTmpVideoFilename() {
        return id + ".mp4";
    }

    public void loadVideoToTempFile(final Context context) {
        if(!isVideo()) return;

        isLoading = true;
        if(loadVideoProgressListener != null) loadVideoProgressListener.progress(0, VideoBean.this);

        File tmpDir = new File(context.getFilesDir() + "/tmp/");
        File tmpFile = new File(tmpDir, getTmpVideoFilename());
        if(tmpFile.exists() && tmpFile.length() > 0)
        {
            if(loadVideoProgressListener != null) loadVideoProgressListener.progress(1, VideoBean.this);

            isLoading = false;
            isLoaded = true;
            isLoadError = false;

            if(loadVideoEndListener != null) loadVideoEndListener.done(true, VideoBean.this);
        }
        else
        {
            parseObject.getParseFile("video").getDataInBackground(new GetDataCallback()
            {
                @Override
                public void done(byte[] bytes, ParseException pe)
                {
                    boolean ok = false;
                    if(pe == null)
                    {
                        FileOutputStream fos = null;
                        try
                        {
                            File tmpDir = new File(context.getFilesDir() + "/tmp/");
                            if(!tmpDir.exists()) tmpDir.mkdir();

                            File tmpFile = new File(tmpDir, getTmpVideoFilename());
                            if(!tmpFile.exists())
                            {
                                tmpFile.deleteOnExit();
                                fos = new FileOutputStream(tmpFile);
                                fos.write(bytes);
                            }

                            ok = true;
                        }
                        catch (Exception e)
                        {
                            L.e(">> ERROR : loadVideoToTempFile e=" + e.getMessage());
                            e.printStackTrace();
                        }
                        finally
                        {
                            try {
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        L.e(">> ERROR PARSE : loadVideoToTempFile");
                    }

                    isLoading = false;
                    isLoaded = ok;
                    isLoadError = !ok;

                    if(loadVideoEndListener != null) loadVideoEndListener.done(ok, VideoBean.this);
                }
            }, new ProgressCallback()
            {
                @Override
                public void done(Integer integer)
                {
                    if(loadVideoProgressListener != null) loadVideoProgressListener.progress(integer, VideoBean.this);
                }
            });
        }
    }

    public String getTempFilePath(Context context)
    {
        return new File(context.getFilesDir() + "/tmp/", getTmpVideoFilename()).getAbsolutePath();
    }

    public static boolean deleteAllTempFileVideo(Context context)
    {
        boolean ok = false;
        File tmpDir = new File(context.getFilesDir() + "/tmp/");
        if (tmpDir.isDirectory())
        {
            String[] tabTmpFile = tmpDir.list();
            ok = tabTmpFile.length > 0;
            for (int i = 0; i < tabTmpFile.length; i++)
            {
                ok &= new File(tmpDir, tabTmpFile[i]).delete();
            }
        }

        return ok;
    }

    public interface LoadVideoEndListener
    {
        public void done(boolean ok, VideoBean react);
    }

    public interface LoadVideoProgressListener
    {
        public void progress(int progress, VideoBean react);
    }

    public LoadVideoEndListener getLoadVideoEndListener()
    {
        return loadVideoEndListener;
    }

    public void setLoadVideoEndListener(LoadVideoEndListener loadVideoEndListener)
    {
        this.loadVideoEndListener = loadVideoEndListener;
    }

    public LoadVideoProgressListener getLoadVideoProgressListener()
    {
        return loadVideoProgressListener;
    }

    public void setLoadVideoProgressListener(LoadVideoProgressListener loadVideoProgressListener)
    {
        this.loadVideoProgressListener = loadVideoProgressListener;
    }

    public boolean isLoading()
    {
        return isLoading;
    }

    public boolean isLoaded()
    {
        return isLoaded;
    }

    public boolean isLoadError()
    {
        return isLoadError;
    }

    public void setLoadError(boolean isLoadError)
    {
        this.isLoadError = isLoadError;
    }

    public boolean isVideo() {
        return urlVideo != null && urlVideo.length() > 0;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

}
