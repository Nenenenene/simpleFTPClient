package com.example.wdgk.myftp_test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import static com.example.wdgk.myftp_test.MainActivity.BACK;
import static com.example.wdgk.myftp_test.MainActivity.CLEAR_DIR;
import static com.example.wdgk.myftp_test.MainActivity.CLICK_A_DIR;
import static com.example.wdgk.myftp_test.MainActivity.LOGIN;
import static com.example.wdgk.myftp_test.MainActivity.LOGIN_SUCCESS;
import static com.example.wdgk.myftp_test.MainActivity.MAX_RETURN_NUM;
import static com.example.wdgk.myftp_test.MainActivity.refreshFiles;

/**
 * Created by wdgk on 2018/8/2.
 */

public class Mhandler extends Handler{
    private WeakReference<MainActivity> weakReference;

    public Mhandler(MainActivity mainActivity){
        weakReference = new WeakReference<MainActivity>(mainActivity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        MainActivity getactivity = weakReference.get();
        if(getactivity!=null){
            boolean login_s = true;
            switch (msg.what){
                case LOGIN:
                    String name = msg.getData().getString("user");
                    String pwd = msg.getData().getString("password");
                    String cur_dir="";
                    FTPFile[] files = new FTPFile[MAX_RETURN_NUM];
                    Log.d("u:p(1)",name+":"+pwd);
                    if(!(name==null)){
                        try {
                            getactivity.client.connect("222.200.181.166");
                            getactivity.client.login(name,pwd);
                            cur_dir = getactivity.client.currentDirectory();
                        }
                        catch (IOException e){
                            Log.e("IOExc: ",e.getMessage());
                            e.printStackTrace();
                        }
                        catch (FTPIllegalReplyException e){
                            Log.e("Illegal: ",e.getMessage());
                            e.printStackTrace();
                        }
                        catch(FTPException e){
                            Log.e("FTPExc: ",e.getMessage());
                            Toast.makeText(getactivity,"Login or password incorrect",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            login_s = false;
                        }
                        finally {
                            if(login_s){
                                files = getactivity.getList(getactivity.client);
                                Log.d("cur_dir",cur_dir);
                                Message root_dir = getactivity.mainHandler.obtainMessage(LOGIN_SUCCESS);
                                Bundle bundle = new Bundle();
                                bundle.putString("dir",cur_dir);
                                root_dir.setData(bundle);
                                getactivity.mainHandler.sendMessage(root_dir);
                                refreshFiles(files,getactivity);
                            }
                        }
                    }
                    break;

                case CLICK_A_DIR:
                    getactivity.count++;
                    String new_path = msg.getData().getString("enter_dir");
                    Log.d("get_path",new_path);
                    String tem ="";
                    FTPFile[] f = new FTPFile[MAX_RETURN_NUM];
                    try{
                        getactivity.client.changeDirectory(new_path);
                        tem = getactivity.client.currentDirectory();
                        f = getactivity.getList(getactivity.client);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    catch (FTPIllegalReplyException e){
                        Log.e("Illegal: ",e.getMessage());
                        e.printStackTrace();
                    }
                    catch(FTPException e){
                        Log.e("FTPExc: ",e.getMessage());
                        e.printStackTrace();
                    }
                    finally {
                        Log.d("new_dir",tem);
                        Message a = getactivity.mainHandler.obtainMessage(CLEAR_DIR);
                        getactivity.mainHandler.sendMessage(a);
                        refreshFiles(f,getactivity);
                    }
                    break;

                case BACK:
                    String t ="";
                    FTPFile[] ff = new FTPFile[MAX_RETURN_NUM];
                    try{
                        getactivity.client.changeDirectoryUp();
                        t = getactivity.client.currentDirectory();
                        ff = getactivity.getList(getactivity.client);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    catch (FTPIllegalReplyException e){
                        Log.e("Illegal: ",e.getMessage());
                        e.printStackTrace();
                    }
                    catch(FTPException e){
                        Log.e("FTPExc: ",e.getMessage());
                        e.printStackTrace();
                    }finally {
                        Log.d("back to:",t);
                        Message a = getactivity.mainHandler.obtainMessage(CLEAR_DIR);
                        getactivity.mainHandler.sendMessage(a);
                        refreshFiles(ff,getactivity);
                    }
                    break;
            }
        }
        else{
            Log.d("zijincheng: ","re is null");
        }
    }
}
