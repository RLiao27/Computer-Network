package com.me.client.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.me.client.R;
import com.me.client.common.CommonAdapter;
import com.me.client.common.CommonViewHolder;
import com.me.client.utils.FileUtils;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载文件页面
 * */
public class DownloadListActivity extends AppCompatActivity {

    private ListView filesLv;
    private CommonAdapter<File> mAdapter;
    private int mSelectPos = -1;
    private List<File> mFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Download Files");
        setContentView(R.layout.activity_download_list);
        filesLv = findViewById(R.id.fileLv);

        File[] files = getExternalFilesDir("download").listFiles();
        for(int i=0;i<files.length;i++){
            mFiles.add(files[i]);
        }

        mAdapter = new CommonAdapter<File>(R.layout.item_download_file,mFiles) {
            @Override
            public void convert(CommonViewHolder holder, File item, int position) {
                holder.setTvText(R.id.fileNameTv,item.getName());
                holder.setTvText(R.id.sizeTv, FileUtils.getFileSize(item.length()));
                ViewGroup rootLayout = holder.getView(R.id.rootLayout);
                if(mSelectPos == position){
                    rootLayout.setBackgroundColor(Color.parseColor("#99cc00"));
                }else{
                    rootLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                }
                TextView countTv = holder.getTextView(R.id.fileCountTv);
                if(item.isDirectory()){
                    countTv.setVisibility(View.VISIBLE);
                    File[] files = item.listFiles();
                    if(files != null) {
                        countTv.setText(item.listFiles().length + " files");
                    }else{
                        countTv.setText("0 files");
                    }
                }else{
                    countTv.setVisibility(View.GONE);
                }
            }
        };
        filesLv.setAdapter(mAdapter);

        filesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectPos = i;
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}