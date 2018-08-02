package com.example.wdgk.myftp_test;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import static it.sauronsoftware.ftp4j.FTPFile.TYPE_DIRECTORY;
import static it.sauronsoftware.ftp4j.FTPFile.TYPE_FILE;

public class MainActivity extends AppCompatActivity {
    FTPClient client = new FTPClient();
    LinearLayout u,p,l;
    TextView title;
    EditText username,pwd;
    Button login,back;
    CheckBox remember;
    String user,password;
    RecyclerView rv;
    final Bundle bun = new Bundle();
    public Mhandler mhandler;
    public MainHandler mainHandler;
    int count = 0;
    final static int MAX_RETURN_NUM = 50;
    final static int LOGIN_SUCCESS = 1;
    final static int SHOW_DIR = 2;
    final static int CLEAR_DIR = 4;
    final static int LOGIN = 99;
    final static int CLICK_A_DIR = 98;
    final static int BACK = 97;
    private MyTask myTask;
    final ArrayList<String> filesname = new ArrayList<>(MAX_RETURN_NUM);
    final ArrayList<String> filessize = new ArrayList<>(MAX_RETURN_NUM);
    final ArrayList<String> filedownload = new ArrayList<>(MAX_RETURN_NUM);
    final ArrayList<String> filetype = new ArrayList<>(MAX_RETURN_NUM);
    final int isDowning[] = new int[MAX_RETURN_NUM];
    final FilesAdapter filesAdapter = new FilesAdapter(this,filesname,filessize,filedownload);

    public void init(){
        for(int i=0;i<isDowning.length;++i){
            isDowning[i] = 0;
        }
        u = (LinearLayout) findViewById(R.id.user);
        p = (LinearLayout)findViewById(R.id.password);
        l = (LinearLayout)findViewById(R.id.login_group);

        mainHandler  = new MainHandler(this);
        //set recycleview
        rv = (RecyclerView)findViewById(R.id.recyclerview);
        rv.setVisibility(View.GONE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(filesAdapter);

        back = (Button)findViewById(R.id.back);
        back.setVisibility(View.GONE);
        title = (TextView)findViewById(R.id.title);
        username = (EditText)findViewById(R.id.username);
        pwd = (EditText)findViewById(R.id.pwd);
        login = (Button)findViewById(R.id.login) ;
        remember = (CheckBox)findViewById(R.id.remember);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CustomThread().start();

        //Permissions
        verifyStoragePermissions(this);

        //remember the id and passwork ?
        SharedPreferences sp = getSharedPreferences("isChecked",Context.MODE_PRIVATE);
        if(sp.getInt("remember",0) == 1){
            username.setText(sp.getString("username",""));
            pwd.setText(sp.getString("password",""));
            remember.setChecked(true);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
                if(username.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"username can't be empty",Toast.LENGTH_SHORT).show();
                }
                else if(pwd.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"password can't be empty",Toast.LENGTH_SHORT).show();
                }
                else{
                    user = username.getText().toString();
                    password = pwd.getText().toString();
                    Log.d("try to log in with",user+" & "+password);
                    //Toast.makeText(getApplicationContext(),"logining",Toast.LENGTH_SHORT).show();
                    bun.putString("user",user);
                    bun.putString("password",password);
                    Message msg = mhandler.obtainMessage(LOGIN);
                    msg.setData(bun);
                    mhandler.sendMessage(msg);
                }
            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SharedPreferences sp = getSharedPreferences("isChecked", Context.MODE_PRIVATE);
                    sp.edit().remove("remember").apply();
                    sp.edit().putInt("remember",1).apply();
                    sp.edit().remove("username").apply();
                    sp.edit().remove("password").apply();
                    sp.edit().putString("username",username.getText().toString()).apply();
                    sp.edit().putString("password",pwd.getText().toString()).apply();
                }
                else{
                    SharedPreferences sp = getSharedPreferences("isChecked", Context.MODE_PRIVATE);
                    sp.edit().remove("remember").apply();
                    sp.edit().putInt("remember",0).apply();
                    sp.edit().remove("username").apply();
                    sp.edit().remove("password").apply();
                }
            }
        });

        filesAdapter.setItemClickListener(new FilesAdapter.MyItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {
                //click a dir
                if(filetype.get(position).equals(Integer.toString(TYPE_DIRECTORY))){
                    Message message = mhandler.obtainMessage(CLICK_A_DIR);
                    Bundle b = new Bundle();
                    b.putString("enter_dir",filesname.get(position));
                    message.setData(b);
                    Log.d("try to enter path : ",filesname.get(position));
                    mhandler.sendMessage(message);
                }
                //click a file downloaded
                else if(filetype.get(position).equals(Integer.toString(TYPE_FILE))&&fileIsExists(filesname.get(position))){
                    Log.d("click:","open the file");
                    Intent intent = new Intent();
                    //this method is not perfect
                    intent = OpenFiles.openFile(getApplicationContext().getFilesDir()+"provider-files/"+filesname.get(position),getApplication());
                    if(intent != null){
                        startActivity(intent);
                    }
                    else{
                        Log.e("open file error : ","can't find the file");
                    }
                }
                //click a file havn't downloaded, so download it
                else if(filetype.get(position).equals(Integer.toString(TYPE_FILE))&&isDowning[position]==0){
                    myTask = new MyTask(MainActivity.this);
                    Log.d("download star : " ,filesname.get(position));
                    //do not download twice
                    isDowning[position] = 1;
                    myTask.execute(filesname.get(position));
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                Message message = mhandler.obtainMessage(BACK);
                mhandler.sendMessage(message);
            }
        });
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have the writting permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,1
            );
        }
    }

    public String Size_to_string(long size){
        String s_size = "";
        if(size>1000000000){
            size = size/1000000000;
            s_size = Long.toString(size);
            s_size = s_size + "GB";
        }
        else if(size>1000000){
            size = size/1000000;
            s_size = Long.toString(size);
            s_size = s_size + "MB";
        }
        else if(size>1000){
            size = size/1000;
            s_size = Long.toString(size);
            s_size = s_size + "KB";
        }
        return s_size;
    }

    public FTPFile[] getList(FTPClient c){
        FTPFile[] f = new FTPFile[MAX_RETURN_NUM];
        try{
            f = c.list();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }catch (FTPIllegalReplyException e){
            e.printStackTrace();
            return null;
        }catch (FTPException e){
            e.printStackTrace();
            return null;
        }catch (FTPDataTransferException e){
            e.printStackTrace();
            return null;
        }catch (FTPAbortedException e){
            e.printStackTrace();
            return null;
        }catch (FTPListParseException e){
            e.printStackTrace();
            return null;
        }finally {
            return f;
        }
    }

    public boolean fileIsExists(String filename){
        try{
            File f=new File(Environment.getExternalStorageDirectory().getPath()+"/"+filename);
            Log.d("path",filename);
            if(!f.exists()){
                return false;
            }
        }catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    public static void refreshFiles(FTPFile[] ff,MainActivity getactivity){
        for(int i=0;i<ff.length;++i){
            Message cur_files = getactivity.mainHandler.obtainMessage(SHOW_DIR);
            Bundle b = new Bundle();
            b.putString("new_files",ff[i].getName());
            b.putString("size",getactivity.Size_to_string(ff[i].getSize()));
            b.putString("type",Integer.toString(ff[i].getType()));
            b.putString("type",Integer.toString(ff[i].getType()));
            if(getactivity.fileIsExists(ff[i].getName()) && ff[i].getType() == TYPE_FILE){
                b.putString("download","已下载");
            }
            else if(ff[i].getType() == TYPE_DIRECTORY){
                b.putString("download","");
            }
            else{
                b.putString("download","未下载");
            }
            cur_files.setData(b);
            getactivity.mainHandler.sendMessage(cur_files);
        }
    }

    public class CustomThread extends Thread {
        @Override
        public void run() {
            //prepare the looper
            Looper.prepare();//init the Looper
            mhandler = new Mhandler(MainActivity.this);
            Looper.loop();//star the looper
        }
    }
}