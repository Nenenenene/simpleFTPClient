package com.example.wdgk.myftp_test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;
import com.example.wdgk.myftp_test.MainActivity;

/**
 * Created by wdgk on 2018/6/15.
 */

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder>{

    private Context mcontext;
    private List<String> file_list,size_list,download;
    private MyItemClickListener mItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView file_name;
        TextView file_size;
        TextView file_Download;
        private  MyItemClickListener mListener;
        public ViewHolder(View view, MyItemClickListener cl){
            super(view);
            file_name = (TextView)view.findViewById(R.id.file_name);
            file_size = (TextView)view.findViewById(R.id.file_size);
            file_Download = (TextView)view.findViewById(R.id.download);
            this.mListener = cl;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getPosition());
            }
        }
    }

    public FilesAdapter(Context context,List<String > file_list,List<String> size_list,List<String> download){
        this.mcontext = context;
        this.file_list = file_list;
        this.size_list = size_list;
        this.download = download;
    }

    @Override
    public FilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        ViewHolder holder = new ViewHolder(view,mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = file_list.get(position);
        String size = size_list.get(position);
        String d = download.get(position);
        holder.file_name.setText(name);
        holder.file_size.setText(size);
        holder.file_Download.setText(d);
    }

    @Override
    public int getItemCount() {
        return file_list.size();
    }
    /**
     * 创建一个回调接口
     */
    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }
    /**
     * 在activity里面adapter就是调用的这个方法,将点击事件监听传递过来,并赋值给全局的监听
     *
     * @param myItemClickListener
     */
    public void setItemClickListener(MyItemClickListener myItemClickListener) {
        this.mItemClickListener = myItemClickListener;
    }
}
