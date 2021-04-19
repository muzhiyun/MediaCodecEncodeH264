package com.example.mediacodecencode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.service.quicksettings.Tile;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class MainActivity extends Activity  implements SurfaceHolder.Callback,PreviewCallback{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private static final int UPDATE_TEXT = 201700;
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
	private Camera camera;
    private Parameters parameters;
    public enum YUVFormat {
        NV21,
        NV12,
        I420,
        NOT_Creat
    }
    private static YUVFormat yuvformat = YUVFormat.NV21;
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_"+ yuvformat +".yuv";
    private static int yuvqueuesize = 10;
    public boolean SourceFromFile = false;
    boolean outputStream_create = false;
    int video_select = 1080;


    public class VIDEOINFO
    {
        int width = 0;
        int height = 0;
        int framerate = 0;
        int yuvframesize = 0;
    }
    int biterate = 1500*1024;


    VIDEOINFO videoinfo = new VIDEOINFO();
    private BufferedOutputStream outputStream;
    public static InputStream inputStream;
    public boolean MainisRuning = false;
	public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
    File file = new File(path);
	private AvcEncoder avcCodec;
    private final static int CAMERA_OK = 10001;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();

    // Example of a call to a native method


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        surfaceview = findViewById(R.id.surfaceview);
        SupportAvcCodec();
        if (Build.VERSION.SDK_INT>22) {
            if (!checkPermissionAllGranted(PERMISSIONS_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        PERMISSIONS_STORAGE, CAMERA_OK);
            }else{
                init();
            }
        }else{
            init();
        }

	}
    Button Button_switch ;
    Button Button_send ;
    Button Button_play ;

	private void init(){
	    switch(video_select)
        {
            case 2160:
                videoinfo.width=3840;
                videoinfo.height=2160;
                videoinfo.framerate=30;
                videoinfo.yuvframesize = videoinfo.width*videoinfo.height*3/2;
                //此程序中 无论NV12、NV21、I420(即YU12)均属于YUV4:2:0 每四个Y共用一组UV分量,一个YUV占8+2+2 = 12bits 1.5个字节
                break;
            case 1440:
                videoinfo.width=2560;
                videoinfo.height=1440;
                videoinfo.framerate=30;
                videoinfo.yuvframesize = videoinfo.width*videoinfo.height*3/2;
                break;
            case 1080:
                videoinfo.width=1920;
                videoinfo.height=1080;
                videoinfo.framerate=30;
                videoinfo.yuvframesize = videoinfo.width*videoinfo.height*3/2;
                break;
            case 720:
                videoinfo.width=1280;
                videoinfo.height=720;
                videoinfo.framerate=30;
                videoinfo.yuvframesize = videoinfo.width*videoinfo.height*3/2;
                break;
        }
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        Button_switch = (Button) findViewById(R.id.button_switch);
        Button_send = (Button) findViewById(R.id.button_send);
        Button_play = (Button) findViewById(R.id.button_play);

    }


    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode) {
            case CAMERA_OK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //这里已经获取到了摄像头的权限，想干嘛干嘛了可以
                    init();
                } else {
                    showWaringDialog("警告！","请前往设置->应用->PermissionDemo->权限中打开相关权限，否则功能无法正常运行！","确定");
                }
                break;
            default:
                break;
        }
    }

    private void showWaringDialog(String Title,String Message,String Button) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(Button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                        finish();
                    }
                }).show();
    }




    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    public boolean fulling = false;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case UPDATE_TEXT:
                    Log.e("handle","recv msg");
                    Button_send.setText("Stoping");
                    break;
            }
        }
    };

    //public void fullthread(){
    public class FullBufferFromFile<fulling> extends Thread {

        public void run() {
            Log.e("FullBufferFromFile","fulling="+fulling);
            //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
            //Date date = new Date(System.currentTimeMillis());
            //Log.e("Time","Date获取当前日期时间"+simpleDateFormat.format(date));

            while (fulling) {
                Log.e("FullBufferFromFile", "Fulling~~"+ String.valueOf(System.currentTimeMillis())  + " File=" + file.length()+" size = "+YUVQueue.size());
                try {
                    //Thread.sleep(100); //加了后解码变慢
                    if(YUVQueue.size()<10)
                    {
                        byte[] readInData = new byte[videoinfo.yuvframesize];
                        //inputStream.skip(videoinfo.yuvframesize); //加了后跳帧会变快
                        if(inputStream.read(readInData,0,videoinfo.yuvframesize) != -1) {
                            YUVQueue.put(readInData);
                            putYUVData(readInData, readInData.length);
                        }else {
                            Log.e("Run", "run: Read Error" );
                            avcCodec.StopThread();
                            Log.e("Button","Stop Avcencoder");
                            MainisRuning=false;
                            fulling = false;
                            Message message = new Message();
                            message.what = UPDATE_TEXT;
                            //将Message对象发送出去
                            handler.sendMessage(message);
                            //Button_send.setText("Stoping");
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }


}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        //editor.putString("SourceFromFile", "true");
        //editor.putInt("INT_KEY", 0);
        //if(sp.getBoolean("SourceFromFile", true)==)
       // editor.putBoolean("SourceFromFile", true);
       // editor.commit();

        //返回STRING_KEY的值
        //Log.d("SP", sp.getString("SourceFromFile", "none"));
        //如果NOT_EXIST不存在，则返回值为"none"
        //Log.d("SP", sp.getString("SourceFromFile", "none"));
        //获取SharedPreferences对象
        Context ctx = MainActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("DataStorage", MODE_PRIVATE);
        Log.d("SP_start", String.valueOf(sp.getBoolean("SourceFromFile", true)));

        //存入数据
        //SharedPreferences.Editor editor = sp.edit();
        if(sp.getBoolean("SourceFromFile",false) == false) {
            SourceFromFile=false;
            Button_switch.setText("Camera");
            Log.d("SP", "SourceFromFile = false");
        } else {
                SourceFromFile=true;
                Button_switch.setText("File");
                Log.d("SP", "SourceFromFile = true");
        }

        if(SourceFromFile==false) {
            camera = getBackCamera();
            startcamera(camera);
        } else {
            try {
                if(openfile()!=0)
                {
                    showWaringDialog("警告","YUV文件不存在,请检查或切换输入源为Camera！！！ ","确认");
                }

            } catch (FileNotFoundException e) {
                showWaringDialog("警告","YUV文件读取异常,请检查或切换输入源为Camera！！！ ","确认");

            }

            }
        //avcCodec = new AvcEncoder(this.width,this.height,framerate,biterate);
        //avcCodec.StartEncoderThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if ( SourceFromFile==false &&null != camera ) {
        	camera.setPreviewCallback(null);
        	camera.stopPreview();
            camera.release();
            camera = null;
            //avcCodec.StopThread();
        }
        else{
            fulling = false;
        }
    }

    public void PlayYuv(View view) throws FileNotFoundException {
        //YuvPlayer yuvPlayer = new YuvPlayer();
        Log.e("Main","PlayYUV");



    }
    public void SwitchSource(View view) throws FileNotFoundException {
        // Do something in response to button click
        //Button button = (Button) findViewById(R.id.button_switch);
        //Button_switch = (Button) findViewById(R.id.button_switch);
        //获取SharedPreferences对象
        Context ctx = MainActivity.this;
        SharedPreferences sp = ctx.getSharedPreferences("DataStorage", MODE_PRIVATE);
        //存入数据
        SharedPreferences.Editor editor = sp.edit();
        Log.e("Main","SourceFromFile="+SourceFromFile);
        if(SourceFromFile==false)
        {
            SourceFromFile=true;
            editor.putBoolean("SourceFromFile", true);
            editor.commit();
            Log.e("Button","Switch Source For File");
        }
        else
        {
            SourceFromFile=false;
            editor.putBoolean("SourceFromFile", false);
            editor.commit();
            Log.e("Button","Switch Source For Camera");
        }
        showWaringDialog("警告","切换成功，请点击确认退出后重新打开应用！！！ ","确认");
    }


    FullBufferFromFile fullthread = null;
    public void sendMessage(View view) throws FileNotFoundException {
        // Do something in response to button click

        if(!MainisRuning) { //由Stop变Run
            avcCodec = new AvcEncoder(this.videoinfo.width,this.videoinfo.height,videoinfo.framerate,biterate);
            if(SourceFromFile==false) {
                createfile();
            } else {
                Log.e("Button","Start Thread");
                fullthread =new FullBufferFromFile();
                Log.e("Button","Start Thread try 1");
                fulling =true;
                fullthread.start(); //创建并启动数据填充进程
            }
            Button_send.setText("Recodeing");
            avcCodec.StartEncoderThread();
            Log.e("Button","Start Avcencoder");
            MainisRuning=true;
        } else {    //由run变Stop
            avcCodec.StopThread();
            Log.e("Button","Stop Avcencoder");
            MainisRuning=false;
            if(SourceFromFile==false)  {
                closefile();
            } else{
                //fullthread.interrupt();
                fulling = false;
                //停止并摧毁数据填充进程
            }
            Button_send.setText("Stoping");
        }
    }

    private int openfile() throws FileNotFoundException {
        Log.e("Main","openfile");
        if(file.exists()) {
            try {
                Log.e("testCodec", "path=" + file.getPath() + ", data length=" + file.length());
                //inputStream = new BufferedInputStream(new FileInputStream(file));
                inputStream = new FileInputStream(file);
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            Log.e("Main","openfile3");
            return 1;
        }
        return 0;
    }

    private void createfile(){
        Log.e("Main","createfile");
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            outputStream_create = true;
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void closefile(){
        Log.e("Main","closefile");
        try {
            outputStream_create = false;
            if(SourceFromFile==false) outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	@Override
	public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
		// TODO Auto-generated method stub
		putYUVData(data,data.length);

    }


	
	public void putYUVData(byte[] buffer, int length) {

        //Log.d("ST",Log.getStackTraceString(new Throwable()));
        if (YUVQueue.size() >= 10) {
            YUVQueue.poll();
        }
        if (outputStream_create == true && SourceFromFile ==false) {
            try {
                switch (yuvformat){
                    case NV21:
                        this.outputStream.write(buffer, 0, length);
                        //Log.e("NV21","length = "+ length);
                        break;
                    case I420:
                        byte[] ret = new byte[length];
                        nv21ToI420(ret,buffer,videoinfo.width,videoinfo.height);
                        this.outputStream.write(ret, 0, length);
                        break;
                    case NV12:
                        byte[] yuv420sp = new byte[videoinfo.width*videoinfo.height*3/2];
                        NV21ToNV12(buffer,yuv420sp,videoinfo.width,videoinfo.height);
                        this.outputStream.write(yuv420sp, 0, length);
                        break;
                    case NOT_Creat:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


		YUVQueue.add(buffer);
        Log.e("putYUVData","length="+length+" YUVQueue.size="+YUVQueue.size());
	}
	
	@SuppressLint("NewApi")
	private boolean SupportAvcCodec(){
		if(Build.VERSION.SDK_INT>=18){
			for(int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--){
				MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
	
				String[] types = codecInfo.getSupportedTypes();
				for (int i = 0; i < types.length; i++) {
					if (types[i].equalsIgnoreCase("video/avc")) {
						return true;
					}
				}
			}
		}
		return false;
	}



    private void startcamera(Camera mCamera){
        Log.e("Main","startcamera");
        if(mCamera != null){
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);
                if(parameters == null){
                    parameters = mCamera.getParameters();
                }
                parameters = mCamera.getParameters();
                //Log.e("camera","Camera Parameters = "+parameters.getSupportedPictureFormats());
                //parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewSize(videoinfo.width, videoinfo.height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(9)
	private Camera getBackCamera() {
        Log.e("Main","getBackCamera");
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    /*
     * nv21转I420
     * @param data
     * @param width
     * @param height
     * @return
     */
    public void  nv21ToI420(byte[] ret,byte[] data, int width, int height) {

        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i=total; i<data.length; i+=2) {
            bufferV.put(data[i]);
            bufferU.put(data[i+1]);
        }

        return ;
    }

    private void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height){
        if(nv21 == null || nv12 == null) {
            return;
        }
        int framesize = width*height;
        int i = 0,j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for(i = 0; i < framesize; i++){
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j-1] = nv21[j+framesize];
        }
        for (j = 0; j < framesize/2; j+=2)
        {
            nv12[framesize + j] = nv21[j+framesize-1];
        }
    }

}
