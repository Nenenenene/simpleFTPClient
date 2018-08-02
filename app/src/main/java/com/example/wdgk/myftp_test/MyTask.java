package com.example.wdgk.myftp_test;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Created by wdgk on 2018/8/2.
 */

public class MyTask extends AsyncTask<String,Integer,Integer>{

    WeakReference<MainActivity> weakReference;

    public MyTask(MainActivity mainActivity){
        weakReference  = new WeakReference<MainActivity>(mainActivity);
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        File f = new File(weakReference.get().getFilesDir()+"provider-files/"+strings[0]);
        if(!f.exists()){
            boolean success = f.mkdirs();
            if(success){
                Log.d("mkdirs() result : ","success");
            }
            else{
                Log.d("mkdirs() result : ","fail");
            }
        }
        try{
            boolean res = f.createNewFile();
            if(res){
                Log.d("createNewFile result : ","success");
            }
            else{
                Log.d("createNewFile result : ","fail");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            weakReference.get().client.download(strings[0],f);
        }catch (IOException e){
            e.printStackTrace();
        }catch (FTPIllegalReplyException e){
            e.printStackTrace();
        }catch (FTPException e){
            e.printStackTrace();
        }catch (FTPDataTransferException e){
            e.printStackTrace();
        }catch (FTPAbortedException e){
            e.printStackTrace();
        }
        return weakReference.get().filesname.indexOf(strings[0]);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer s) {
        //super.onPostExecute(s);
        weakReference.get().filedownload.set(s,"downloaded");
        weakReference.get().isDowning[s] = 0;
        weakReference.get().filesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
