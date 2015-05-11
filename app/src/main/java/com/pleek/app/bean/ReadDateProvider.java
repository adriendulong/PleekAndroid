package com.pleek.app.bean;

import android.content.Context;

import com.goandup.lib.utile.L;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by nicolas on 05/01/15.
 */
public class ReadDateProvider
{
    private final static String DATA_FILENAME = "readdate.data";
    private static ReadDateProvider me;

    private HashMap<String, Date> mapData;
    private Context context;

    public static ReadDateProvider getInstance(Context context)
    {
        if(me == null)
        {
            me = new ReadDateProvider(context);
        }
        return me;
    }

    public ReadDateProvider(Context context) {
        this.context = context;
    }

    public Date getReadDate(String idPiki)
    {
        if(mapData == null) mapData = readHashMapNoNull();

        return mapData.get(idPiki);
    }

    public boolean setReadDateNow(String idPiki)
    {
        return setReadDate(idPiki, new Date());
    }

    public boolean setReadDate(String idPiki, Date readDate)
    {
        if(mapData == null) mapData = readHashMapNoNull();

        mapData.put(idPiki, readDate);

        return saveMapData(mapData);
    }

    public boolean isNeverRead()
    {
        return readHashMap() == null;
    }

    public int nbRead()
    {
        if(mapData == null) mapData = readHashMapNoNull();
        return mapData.size();
    }


    private boolean saveMapData(HashMap<String, Date> mapData)
    {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try
        {
            fos = context.openFileOutput(DATA_FILENAME, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(mapData);

            return true;
        }
        catch(IOException ioe)
        {
            L.e(">> ReadDateProvider.saveMapData() ERROR : " + ioe.getMessage());
            ioe.printStackTrace();
        }
        finally
        {
            try
            {
                if(oos != null) oos.close();
                if(fos != null) fos.close();
            }
            catch (IOException e) {}
        }

        return false;
    }

    private HashMap<String, Date> readHashMapNoNull()
    {
        HashMap<String, Date> mapData = null;
        if(mapData == null) mapData = readHashMap();
        if(mapData == null) mapData = new HashMap<String, Date>();
        return mapData;
    }

    private HashMap<String, Date> readHashMap()
    {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        HashMap<String, Date> mapData = null;
        // TODO test if file exists
        try
        {
            fis = context.openFileInput(DATA_FILENAME);
            ois = new ObjectInputStream(fis);
            mapData = (HashMap<String, Date>) ois.readObject();
        }
        catch(Exception e)
        {
            L.e(">> ReadDateProvider.readHashMap() ERROR : "+e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(ois != null) ois.close();
                if(fis != null) fis.close();
            }
            catch (IOException e) {}
        }

        return mapData;
    }
}
