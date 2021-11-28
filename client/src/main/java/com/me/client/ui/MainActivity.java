package com.me.client.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.me.client.R;
import com.me.client.common.CommonAdapter;
import com.me.client.common.CommonViewHolder;
import com.me.client.common.Constants;
import com.me.client.common.RxTools;
import com.me.client.core.FtpCore;
import com.me.client.utils.FileUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import org.apache.commons.net.ftp.FTPFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1 ;
    private ListView filesRv;
    private Button refreshBtn;
    private Button uploadFileBtn;
    private Button uploadFolderBtn;
    private Button downloadListBtn;
    private Button quitBtn;
    private TextView curDirTv;
    private TextView backDirTv;

    private CommonAdapter<FTPFile> mAdapter;
    private List<FTPFile> mFiles = new ArrayList();
    private String mCurDir = "";
    private long mPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filesRv = findViewById(R.id.filesRv);
        curDirTv = findViewById(R.id.curDirTv);
        backDirTv = findViewById(R.id.backDirTv);

        refreshBtn = findViewById(R.id.refreshBtn);
        uploadFileBtn = findViewById(R.id.uploadFileBtn);
        uploadFolderBtn =findViewById(R.id.uploadFolderBtn);
        quitBtn = findViewById(R.id.quitBtn);
        downloadListBtn = findViewById(R.id.downloadListBtn);
        mAdapter = new CommonAdapter<FTPFile>(R.layout.item_ftp_file,mFiles) {
            @Override
            public void convert(CommonViewHolder holder, FTPFile item, int position) {
                holder.setTvText(R.id.fileNameTv,item.getName());
                holder.setTvText(R.id.sizeTv,FileUtils.getFileSize(item.getSize()));
                holder.getView(R.id.downloadIv).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "start download file", Toast.LENGTH_SHORT).show();
                        downloadFile(item);
                    }
                });
            }
        };
        filesRv.setAdapter(mAdapter);
        filesRv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>=0 && i<mFiles.size()){
                    FTPFile file = mFiles.get(i);
                    if(file.isDirectory()){
                        changeDir(file.getName());
                    }
                }
            }
        });

        backDirTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDir("");
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDirFiles();
            }
        });

        uploadFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxPermissions permissions = new RxPermissions(MainActivity.this);
                Disposable d = permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if(aBoolean){
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_SELECT_CODE);
                            }
                        }
                    });
            }
        });

        uploadFolderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxPermissions permissions = new RxPermissions(MainActivity.this);
                Disposable d = permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"ftp_upload_folder";
                                File folderFile = new File(folderPath);
                                if(folderFile.exists()){
                                    Log.d(Constants.TAG,folderFile.getName());
                                    Toast.makeText(MainActivity.this, "start upload folder", Toast.LENGTH_SHORT).show();
                                    Disposable d = Observable.just(folderPath).map(new Function<String, Object>() {
                                        @Override
                                        public Object apply(@NonNull String s) throws Exception {
                                            FtpCore.instance().upload(folderPath,mCurDir);
                                            return true;
                                        }
                                    }).compose(RxTools.oIoMain())
                                    .subscribe(new Consumer<Object>() {
                                        @Override
                                        public void accept(Object o) throws Exception {
                                            Toast.makeText(MainActivity.this, "upload folder over", Toast.LENGTH_SHORT).show();
                                            delayRefresh();
                                        }
                                    });
                                }else{
                                    Toast.makeText(MainActivity.this, "please push ftp_upload_folder to sdcard",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            }
        });

        downloadListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,DownloadListActivity.class));
            }
        });

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitFtp();
            }
        });

        refreshBtn.performClick();
    }

    private void downloadFile(FTPFile file)
    {
        Disposable d = Observable.just("")
            .map(new Function<String, Boolean>() {
                @Override
                public Boolean apply(@NonNull String s) throws Exception {
                    try {
                        File path = getApplicationContext().getExternalFilesDir("download");
                        if(!path.exists()){
                            path.mkdirs();
                        }
                        return FtpCore.instance().download(file.getName(), path.getAbsolutePath(),
                                file.isDirectory());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            }).compose(RxTools.oIoMain())
            .subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    if(aBoolean){
                        Toast.makeText(MainActivity.this,
                                "download successfully",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this,
                                "download failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void getDirFiles()
    {
        Disposable d = Observable.just("")
            .map(new Function<String, List<FTPFile>>() {
                @Override
                public List<FTPFile> apply(@NonNull String s) throws Exception {
                    List<FTPFile> result = new ArrayList();
                    try {
                        FTPFile[] files = null;
                            files = FtpCore.instance().getAllFile();
                        if(files != null){
                            for(int i=0;i<files.length;i++){
                                result.add(files[i]);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return result;
                }
            }).compose(RxTools.oIoMain())
            .subscribe(new Consumer<List<FTPFile>>() {
                @Override
                public void accept(List<FTPFile> ftpFiles) throws Exception {
                    mFiles.clear();
                    mFiles.addAll(ftpFiles);
                    mAdapter.refreshView(ftpFiles);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {

                }
            });
    }


    private void changeDir(String dir)
    {
        Observable.just(dir)
            .map(new Function<String, Boolean>() {
                @Override
                public Boolean apply(@NonNull String s) throws Exception {
                    return FtpCore.instance().cwd(s);
                }
            }).compose(RxTools.oIoMain())
            .subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    if(aBoolean){
                        mCurDir = dir;
                        curDirTv.setText("... /"+mCurDir);
                        getDirFiles();
                    }
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == FILE_SELECT_CODE){
            Uri uri = data.getData();
            String path = FileUtils.getFilePathByUri(this,uri);
            File file = new File(path);
            Log.i(Constants.TAG, "------->" + path+",exist="+file.exists());
            if(file.exists()) {
                Disposable d = Observable.just(path)
                    .map(new Function<String, Boolean>() {
                        @Override
                        public Boolean apply(String s) throws Exception {
                            FtpCore.instance().upload(file.getAbsolutePath(),mCurDir);
                            return true;
                        }
                    }).compose(RxTools.oIoMain())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            delayRefresh();
                        }
                    });
            }
        }
    }


    private void delayRefresh()
    {
        Observable
            .timer(1000, TimeUnit.MILLISECONDS)
            .compose(RxTools.oIoMain())
            .doOnNext(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Exception {
                    refreshBtn.performClick();
                }
            })
            .subscribe();
    }


    private void quitFtp()
    {
        Toast.makeText(this, "quiting now...", Toast.LENGTH_SHORT).show();
        Disposable d = RxTools.observableOnIoMain(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return FtpCore.instance().quit();
            }
        }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if(aBoolean){
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,
                            "quit failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();
        if((mNowTime - mPressedTime) > 2000){
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else{
            quitFtp();
        }
    }
}