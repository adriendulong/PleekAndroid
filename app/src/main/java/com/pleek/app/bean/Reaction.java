package com.pleek.app.bean;

import android.content.Context;
import android.graphics.Bitmap;

import com.goandup.lib.utile.L;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by nicolas on 31/12/14.
 */
public class Reaction implements Serializable
{
    public enum Type {
        PHOTO,
        VIDEO,
        TEXTE,
        EMOJI,
        UNKNOW
    }

    private String id;
    private String urlPhoto;
    private String urlVideo;
    private String nameUser;
    private ParseObject parseObject;
    private Bitmap tmpPhoto;//use between create and save to Parse
    public Type type = Type.UNKNOW;

    private LoadVideoEndListener loadVideoEndListener;
    private LoadVideoProgressListener loadVideoProgressListener;
    private boolean isLoading;
    private boolean isLoaded;
    private boolean isLoadError;


    public Reaction(String nameUser, Bitmap tmpPhoto)
    {
        this.id = UUID.randomUUID().toString();//tmp random id
        this.nameUser = nameUser;
        this.tmpPhoto = tmpPhoto;
    }

    public Reaction(ParseObject parseObject)
    {
        if(parseObject == null) return;

        id = parseObject.getObjectId();
        if(parseObject.getParseFile("photo") != null)
        {
            urlPhoto = parseObject.getParseFile("photo").getUrl();
        }
        else if(parseObject.getParseFile("previewImage") != null)
        {
            urlPhoto = parseObject.getParseFile("previewImage").getUrl();
        }
        urlVideo = parseObject.getParseFile("video") != null ? parseObject.getParseFile("video").getUrl() : null;

        ParseObject user = parseObject.containsKey("user") ? parseObject.getParseObject("user") : null;
        if(user != null) nameUser = user.containsKey("username") ? user.getString("username") : null;
        if(nameUser == null) nameUser = "@none";

        this.parseObject = parseObject;
    }

    public void loadVideoToTempFile(final Context context)
    {
        if(!isVideo()) return;

        isLoading = true;
        if(loadVideoProgressListener != null) loadVideoProgressListener.progress(0, Reaction.this);

        File tmpDir = new File(context.getFilesDir() + "/tmp/");
        File tmpFile = new File(tmpDir, getTmpVideoFilename());
        if(tmpFile.exists() && tmpFile.length() > 0)
        {
            if(loadVideoProgressListener != null) loadVideoProgressListener.progress(1, Reaction.this);

            isLoading = false;
            isLoaded = true;
            isLoadError = false;

            if(loadVideoEndListener != null) loadVideoEndListener.done(true, Reaction.this);
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
                            L.e(">> ERROR : loadVideoToTempFile e="+e.getMessage());
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

                    if(loadVideoEndListener != null) loadVideoEndListener.done(ok, Reaction.this);
                }
            }, new ProgressCallback()
            {
                @Override
                public void done(Integer integer)
                {
                    if(loadVideoProgressListener != null) loadVideoProgressListener.progress(integer, Reaction.this);
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
        public void done(boolean ok, Reaction react);
    }

    public interface LoadVideoProgressListener
    {
        public void progress(int progress, Reaction react);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public String getTmpVideoFilename() {
        return id + ".mp4";
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
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

    public Type getType()
    {
        return type != null ? type : Type.UNKNOW;
    }

    public String getTypeStr()
    {
        if(type == Type.PHOTO) return "Photo";
        else if(type == Type.VIDEO) return "Video";
        else if(type == Type.TEXTE) return "Text";
        else if(type == Type.EMOJI) return "Emoji";
        return "Unknow";
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Bitmap getTmpPhoto()
    {
        return tmpPhoto;
    }

    public void setTmpPhoto(Bitmap tmpPhoto)
    {
        this.tmpPhoto = tmpPhoto;
    }

    public byte[] getTmpPhotoByte()
    {
        if(tmpPhoto == null) return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        tmpPhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);//fix : crash #34
        return stream.toByteArray();
    }

    public boolean isTmpReaction()
    {
        return urlPhoto == null || urlPhoto.trim().isEmpty();
    }

    public boolean iamOwner()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null || parseObject == null || parseObject.getParseUser("user") == null) return false;//fix : crash #18
        return currentUser.getObjectId().equals(parseObject.getParseUser("user").getObjectId());
    }

    @Override
    public boolean equals(Object o)
    {
        return o != null && o instanceof Reaction && id.equals(((Reaction)o).getId());
    }

    @Override
    public String toString()
    {
        return "Reaction("+id+")";
    }
}