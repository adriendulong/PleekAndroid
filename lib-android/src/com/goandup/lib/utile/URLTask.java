package com.goandup.lib.utile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;


public class URLTask extends AsyncTask<String, Void, Void>
{
	public final static int ERROR_UNKNOW = 0;
	
	private Listener listener;
	
	public URLTask(Listener listener) {
		super();
		this.listener = listener;
	}

	@Override
	protected Void doInBackground(String ... urls)
	{
		for (String url : urls)
		{
			try
			{
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
				HttpConnectionParams.setSoTimeout(httpParameters, 10000);
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

				HttpUriRequest request = new HttpGet(url); 
				HttpResponse response = httpClient.execute(request);
				String data = convertStreamToString(response.getEntity().getContent());
				
				listener.endTask(data, url);
			}
			catch (Exception e)
			{
				listener.errorTask(ERROR_UNKNOW, url);
				L.e(">>>>> Erreur URLTask - e=["+e+"]");
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public interface Listener {
		public void endTask(String data, String url);
		public void errorTask(int error, String url);
	}
	
	public static String convertUrlToString(String url)
	{
		String rep = null;

		HttpClient httpclient = new DefaultHttpClient();

		HttpGet httpget = new HttpGet(url); 

		HttpResponse response;
		try {
			response = httpclient.execute(httpget);			
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				rep = convertStreamToString(instream);
				instream.close();
			}

		} catch (Exception e) {
			L.e(">>>>> Erreur convertUrlToString("+url+") - e=["+e+"]");
			e.printStackTrace();
		}

		return rep != null ? rep : "";
	}
	
	public static String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			L.e(">>>>> Erreur convertStreamToString("+is+") - e=["+e+"]");
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
