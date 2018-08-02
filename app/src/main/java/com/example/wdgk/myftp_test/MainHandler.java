package com.example.wdgk.myftp_test;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import static com.example.wdgk.myftp_test.MainActivity.CLEAR_DIR;
import static com.example.wdgk.myftp_test.MainActivity.LOGIN_SUCCESS;
import static com.example.wdgk.myftp_test.MainActivity.SHOW_DIR;

/**
 * Created by wdgk on 2018/8/2.
 */

public class MainHandler extends Handler{
    private WeakReference<MainActivity> weakReference;
    public MainHandler(MainActivity mainActivity){
        weakReference = new WeakReference<MainActivity>(mainActivity);
    }
    @Override
    public void handleMessage(Message from_thread) {
        //super.handleMessage(msg);
        MainActivity getactivity = weakReference.get();
        //activity exists
        if(getactivity!=null){
            switch (from_thread.what){
                case LOGIN_SUCCESS:
                    getactivity.title.setText(from_thread.getData().getString("dir"));
                    getactivity.rv.setVisibility(View.VISIBLE);
                    getactivity.u.setVisibility(View.GONE);
                    getactivity.p.setVisibility(View.GONE);
                    getactivity.l.setVisibility(View.GONE);
                    getactivity.title.setVisibility(View.GONE);
                    getactivity.login.setVisibility(View.GONE);
                    break;
                case SHOW_DIR:
                    getactivity.filesname.add(from_thread.getData().getString("new_files"));
                    getactivity.filessize.add(from_thread.getData().getString("size"));
                    getactivity.filetype.add(from_thread.getData().getString("type"));
                    getactivity.filedownload.add(from_thread.getData().getString("download"));
                    getactivity.filesAdapter.notifyDataSetChanged();
                    //in the root dir?
                    if(getactivity.count>0){
                        getactivity.back.setVisibility(View.VISIBLE);
                    }
                    else{
                        getactivity.back.setVisibility(View.GONE);
                    }
                    break;
                case CLEAR_DIR:
                    //used for refreshing
                    getactivity.filesname.clear();
                    getactivity.filessize.clear();
                    getactivity. filetype.clear();
                    getactivity.filedownload.clear();
                    if(getactivity.filesname.size()==0){
                        Log.d("file_num","empty");
                    }
                    getactivity.filesAdapter.notifyDataSetChanged();
                    break;
            }
        }
        else{
            Log.d("activity null error : ","Reference is null");
        }
    }
}
