package com.dji.FPVDemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Vector;
import dji.common.camera.CameraSystemState;
import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIVirtualStickFlightControlData;
import dji.common.flightcontroller.DJIVirtualStickFlightCoordinateSystem;
import dji.common.flightcontroller.DJIVirtualStickRollPitchControlMode;
import dji.common.flightcontroller.DJIVirtualStickVerticalControlMode;
import dji.common.flightcontroller.DJIVirtualStickYawControlMode;
import dji.common.product.Model;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.camera.DJICamera;
import dji.sdk.camera.DJICamera.CameraReceivedVideoDataCallback;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;
import static com.dji.FPVDemo.FPVDemoApplication.getProductInstance;


public class LostActivity extends Activity implements SurfaceTextureListener,OnClickListener{

    //TODO: We have to move continuously when I lost the person;

    public static final String urlUpload = "http://192.168.1.187:8000/images";
    public static final String urlUpload1 = "http://192.168.28.124:8000/detect";
    public static final String urlUpload2 = "http://192.168.28.124:8000/check";
    public static final String imageList = "file";
    public static final String imageNameList = "name";
    int count_images=0;
    int kcf_counter=0;
    public Vector<String> encodedImageList = new Vector<>();
    public Vector<String>encodedImageNameList = new Vector<>();
    private static final String TAG = "Loaded Flag";
    public native static String KCFTracker3(long addr,float a[],float b[],float c[],float d[], int addr1);
    public native static String KCFTracker4(long addr,float a,float b,float c,float d,int addr1);
    String position_of_culprit="";
    int initilize=0;
    float x_array[],y_array[],w_array[],h_array[];
    float[] dumy_array = {0.0f};
    Mat tmp;
    private Handler mHandler,mCheckHandler;
    ImageView result;
    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    Double controla=0.0028;
    Double controlb=0.003;
    TextView hello;
    TextView hello1;
    TextView hello2;
    int response_position;
    int response_index,saved_index=-1;
    Bitmap bmp;
    Bitmap bm;
    int count=0;
    protected DJICodecManager mCodecManager = null;
    int recordbit=0;
    protected TextureView mVideoSurface = null;
    private Button  mCancel;
    private ToggleButton mRecordBtn;
    int flag=-1;
    int try_once=0;
    int handeler_flag=0;
    int handeler_flag1=0;
    int check=-1;
    int xx=0; //pitch
    int yy=0; //roll
    int restart=0;
    double stuckx,stucky,stuckw,stuckh;
    static{
        if(OpenCVLoader.initDebug())
        {
            System.loadLibrary("KCF");
            System.loadLibrary("KCF2");
            System.loadLibrary("KCF3");
            System.loadLibrary("KCF4");
            Log.d(TAG,"Successfully Loaded");
        }
        else
        {
            Log.d(TAG,"OpenCV Not Loaded");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initUI();

        FPVDemoApplication.getAircraftInstance().getFlightController().enableVirtualStickControlMode(new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
            }
        });

        FPVDemoApplication.getAircraftInstance().getFlightController().isVirtualStickControlModeAvailable();
        FPVDemoApplication.getAircraftInstance().getFlightController().setHorizontalCoordinateSystem(DJIVirtualStickFlightCoordinateSystem.Body);
        FPVDemoApplication.getAircraftInstance().getFlightController().setVerticalControlMode(DJIVirtualStickVerticalControlMode.Velocity);
        FPVDemoApplication.getAircraftInstance().getFlightController().setRollPitchControlMode(DJIVirtualStickRollPitchControlMode.Velocity);
        FPVDemoApplication.getAircraftInstance().getFlightController().setYawControlMode(DJIVirtualStickYawControlMode.AngularVelocity);


        mReceivedVideoDataCallBack = new CameraReceivedVideoDataCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if(mCodecManager != null){
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }else {
                    Log.e(TAG, "mCodecManager is null");
                }
            }
        };

        DJICamera camera = FPVDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setDJICameraUpdatedSystemStateCallback(new DJICamera.CameraUpdatedSystemStateCallback() {
                @Override
                public void onResult(CameraSystemState cameraSystemState) {
                }
            });

        }

    }






    protected void onProductChange() {
        initPreviewer();
    }

    @Override
    public void onResume() {

        super.onResume();
        initPreviewer();
        onProductChange();
        FPVDemoApplication.getAircraftInstance().getFlightController().enableVirtualStickControlMode(new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
        FPVDemoApplication.getAircraftInstance().getFlightController().setHorizontalCoordinateSystem(DJIVirtualStickFlightCoordinateSystem.Body);
        FPVDemoApplication.getAircraftInstance().getFlightController().setVerticalControlMode(DJIVirtualStickVerticalControlMode.Velocity);
        FPVDemoApplication.getAircraftInstance().getFlightController().setRollPitchControlMode(DJIVirtualStickRollPitchControlMode.Velocity);
        FPVDemoApplication.getAircraftInstance().getFlightController().setYawControlMode(DJIVirtualStickYawControlMode.AngularVelocity);
        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {

        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {

        super.onStop();
    }



    @Override
    protected void onDestroy() {

        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {

        mHandler=new Handler();
        mCheckHandler=new Handler();
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        hello=(TextView)findViewById(R.id.kunal);   //result from KCF Tracker
        hello1=(TextView)findViewById(R.id.kunal1); // velocity in x and y
        hello2=(TextView)findViewById(R.id.kunal2); // scale in x and y
        result=(ImageView)findViewById(R.id.result);

        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mCancel = (Button) findViewById(R.id.btn_cancel);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mRecordBtn.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecord();
                } else {
                    stopRecord();
                }
            }
        });

    }



    private void initPreviewer() {

        DJIBaseProduct product = getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UnknownAircraft)) {
                DJICamera camera = product.getCamera();
                if (camera != null){
                    camera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {
        DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null){
            FPVDemoApplication.getCameraInstance().setDJICameraReceivedVideoDataCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    public int get_scale_x(double position_x, int size_x) {

        double diff=Math.abs(size_x-position_x);
        if(diff<=100)
            return 1;
        else if(diff>100 && diff<=200)
            return 2;
        else if(diff>200 && diff<=250)
            return 3;
        else if(diff>250 && diff<=300)
            return 4;
        else
            return 5;

    }


    public int get_scale_y(double position_y, int size_y) {
        double diff=Math.abs(size_y-position_y);
        if(diff<=100)
            return 1;
        else if(diff>100 && diff<=200)
            return 2;
        else if(diff>200 && diff<=250)
            return 3;
        else if(diff>250 && diff<=300)
            return 4;
        else
            return 5;
    }

    public void where_to_move()
    {

        // xx roll  ^
        // yy pitch   <- ->

        double sx=(stuckx+stuckw)/2;
        double sy=(stucky+stuckh)/2;

        if(sx<=240 && sy<=226)
        {
            xx=2;
            yy=-2;
        }
        else if(sx>=480 && sy<=226)
        {
            xx=2;
            yy=2;
        }
        else if(sx<=240 && sy>=452)
        {
            xx=-2;
            yy=-2;
        }
        else if(sx>=480 && sy>=452)
        {
            xx=-2;
            yy=2;
        }
        else if(sx>=480)
        {
            yy=2;
        }
        else if(sx<=240)
        {
            yy=-2;
        }
        else if(sy<=339)
        {
            xx=2;
        }
        else
        {
            xx=-2;
        }

    }

    public void move(){
        showToast("Moving in lost Direction");
        FPVDemoApplication.getAircraftInstance().
                getFlightController().sendVirtualStickFlightControlData(
                new DJIVirtualStickFlightControlData(
                        yy, xx, 0, 0
                ), new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                }
        );
    }




    public void check_person(Bitmap bm, final double a,final double b,final double c,final double d) {
        showToast("check for person");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("image",encodedImage);
            jsonObject.put("x",a);
            jsonObject.put("y",b);
            jsonObject.put("w",c);
            jsonObject.put("h",d);
        } catch (JSONException e) {
            showToast("JSONObject Error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urlUpload2, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        try {

                            check = (int) jsonObject.get("result");
                            if(check==0)
                            {
                                showToast("Person is not there");
                                //handler removed
                                mCheckHandler.removeCallbacks(null);
                                mHandler.removeCallbacksAndMessages(null);
                                //height increased by 3 once
                                showToast("Increase Height");
                                FPVDemoApplication.getAircraftInstance().
                                        getFlightController().sendVirtualStickFlightControlData(
                                        new DJIVirtualStickFlightControlData(
                                                0, 0, 0, 3
                                        ), new DJICommonCallbacks.DJICompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {

                                            }
                                        }
                                );

                                //co-ordinates saved and the id;
                                showToast("Save last co-ordinates");
                                stuckx=a;
                                stucky=b;
                                stuckw=c;
                                stuckh=d;
                                saved_index=response_index;

                                showToast("Where to move");
                                //where to move
                                where_to_move();
                                move();
                                showToast("restart");
                                //stop recording
                                restart=1;
                                stopRecord();

                                //start recording
                                //startRecord();

                            }

                        } catch (JSONException e) {

                            Toast.makeText(getApplication(),"Error in function",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showToast("Volley Error Occurred "+volleyError);
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }


    public void detect_person(Bitmap bm)
    {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("image",encodedImage);
        } catch (JSONException e) {
            showToast("JSONObject Error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urlUpload1, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                        try {

                            JSONArray x = jsonObject.getJSONArray("x_array");
                            JSONArray y = jsonObject.getJSONArray("y_array");
                            JSONArray w = jsonObject.getJSONArray("w_array");
                            JSONArray h = jsonObject.getJSONArray("h_array");

                            if(x.length()>0)
                            {

                                flag =1;
                                x_array=new float[x.length()];
                                y_array=new float[x.length()];
                                w_array=new float[x.length()];
                                h_array=new float[x.length()];

                                for(int i=0;i<x.length();i++)
                                {
                                    x_array[i]=((Double)x.get(i)).floatValue();

                                    y_array[i]=((Double)y.get(i)).floatValue();
                                    w_array[i]=((Double)w.get(i)).floatValue();
                                    h_array[i]=((Double)h.get(i)).floatValue();
                                }

                            }
                            else
                            {
                                flag=0;

                            }

                        } catch (JSONException e) {
                            flag=2;
                            Toast.makeText(getApplication(),"Error in function",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showToast("Volley Error Occurred "+volleyError);
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, (float)2.0));
        Volley.newRequestQueue(this).add(jsonObjectRequest);


    }




    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        if(recordbit!=0) {
            bmp = mVideoSurface.getBitmap();
            int bytes = bmp.getByteCount();
            ByteBuffer buffer = ByteBuffer.allocate(bytes);
            bmp.copyPixelsToBuffer(buffer);
            byte[] array = buffer.array();
            tmp=new Mat(bmp.getHeight(),bmp.getWidth(),CvType.CV_8UC4);
            tmp.put(0,0,array);
            Imgproc.cvtColor(tmp,tmp, Imgproc.COLOR_RGBA2RGB);

            if(count==-1)
            {

                if(initilize==0)
                {
                    kcf_counter=0;
                    hello.setText((KCFTracker3(tmp.getNativeObjAddr(),dumy_array, dumy_array, dumy_array , dumy_array, kcf_counter)+""));
                    kcf_counter=1;
                    String s=hello.getText().toString();
                    Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                    String[] parts = s.split("@");
                    position_of_culprit=parts[response_position];
                    String[] sub_parts=position_of_culprit.split(":");
                    float a=Float.parseFloat(sub_parts[0]);
                    float b=Float.parseFloat(sub_parts[1]);
                    float c=Float.parseFloat(sub_parts[2]);
                    float d=Float.parseFloat(sub_parts[3]);
                    bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(tmp, bm);
                    result.setImageBitmap(bm);
                    hello.setText((KCFTracker4(tmp.getNativeObjAddr(), a, b, c,d,kcf_counter) + ""));
                    initilize=1;
                }
                else
                {
                    hello.setText((KCFTracker4(tmp.getNativeObjAddr(), 0, 0, 0, 0, kcf_counter) + ""));
                    String s = hello.getText().toString();
                    String[] parts = s.split("@");
                    final double a = Float.parseFloat(parts[0]);
                    final double b = Float.parseFloat(parts[1]);
                    final double c = Float.parseFloat(parts[2]);
                    final double d = Float.parseFloat(parts[3]);
                    double scale_x=get_scale_x(a+c/2,tmp.rows()/2);
                    double scale_y=get_scale_y(b+d/2,tmp.cols()/2);
                    double a1=scale_x*controla*((a+c/2)-(tmp.cols()/2));
                    double b1=scale_y*controlb*((tmp.rows()/2)-(b+d/2));
                    hello1.setText("velociy2:" + a1 + " " + b1);
                    hello2.setText("scale2: " + scale_x + " " + scale_y);
                    final float af = (float) a1;
                    final float bf = (float) b1;
                    if(handeler_flag1==0)
                    {
                        handeler_flag1=1;

                        mCheckHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //This will check if the person is there or not, if not then change the flag and further.....
                                check_person(bm,a,b,c,d);
                                mHandler.postDelayed(this,1000);
                            }
                        });
                    }




                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FPVDemoApplication.getAircraftInstance().
                                        getFlightController().sendVirtualStickFlightControlData(
                                        new DJIVirtualStickFlightControlData(
                                                af, bf, 0, 0
                                        ), new DJICommonCallbacks.DJICompletionCallback() {
                                            @Override
                                            public void onResult(DJIError djiError) {

                                            }
                                        }
                                );


                            }
                        },100);

                    bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(tmp, bm);
                    result.setImageBitmap(bm);

                }

            }
            else if(count>0)
            {


                hello.setText((KCFTracker3(tmp.getNativeObjAddr(), dumy_array, dumy_array, dumy_array , dumy_array, kcf_counter) + ""));
                String s=hello.getText().toString();

                String[] parts = s.split("@");
                double mean_x = 0.0, mean_y = 0.0;
                for (int i = 0; i < parts.length; i++) {
                    String[] sub_parts = parts[i].split(":");
                    double a = Float.parseFloat(sub_parts[0]);
                    double b = Float.parseFloat(sub_parts[1]);
                    double c = Float.parseFloat(sub_parts[2]);
                    double d = Float.parseFloat(sub_parts[3]);
                    mean_x = mean_x + (a + c / 2);
                    mean_y = mean_y + (b + d / 2);
                }
                mean_x = mean_x / parts.length;
                mean_y = mean_y / parts.length;

                int scale_x=get_scale_x(mean_x,tmp.rows()/2);
                int scale_y=get_scale_y(mean_y,tmp.cols()/2);
                double a=scale_x*controla*(mean_x - (tmp.cols() / 2));
                double b=scale_y*controlb*((tmp.rows() / 2) - mean_y);

                hello1.setText("velocity1:" + a + " " + b);
                hello2.setText("scale1: " + scale_x + " " + scale_y);
                final float af = (float) a;
                final float bf = (float) b;


                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FPVDemoApplication.getAircraftInstance().
                                    getFlightController().sendVirtualStickFlightControlData(
                                    new DJIVirtualStickFlightControlData(
                                            af, bf, 0, 0
                                    ), new DJICommonCallbacks.DJICompletionCallback() {
                                        @Override
                                        public void onResult(DJIError djiError) {

                                        }
                                    }
                            );


                        }
                    },100);


                if (count_images < 16) {

                    bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(tmp, bm);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    encodedImageList.add(encodedImage);
                    encodedImageNameList.add(s);
                    count_images++;

                }
                else if (count_images == 16)
                {
                    JSONArray jsonArray = new JSONArray();
                    JSONArray jsonArray_name = new JSONArray();
                    JSONObject jsonObject = new JSONObject();

                    for (String encoded : encodedImageList) {
                        jsonArray.put(encoded);
                    }
                    for (String name : encodedImageNameList) {
                        jsonArray_name.put(name);
                    }

                    try {
                        jsonObject.put(imageNameList, jsonArray_name);
                        jsonObject.put(imageList, jsonArray);
                    } catch (JSONException e) {
                        showToast("JSONObject Error");
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urlUpload, jsonObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {

                                    try {
                                        showToast("Server Responded");
                                        response_position = (int) jsonObject.get("id");
                                        response_index=(int) jsonObject.get("index");
                                        //Divided by two as there are 2 feature per person
                                        response_index=response_index/2;

                                        if(saved_index!=-1 && response_index!=saved_index)
                                        {
                                            showToast("Sorry We are not able to find the same ID");
                                            mHandler.removeCallbacksAndMessages(null);
                                            //where to move
                                            showToast("We will move now");
                                            move();
                                            showToast("Restart");
                                            restart=1;
                                            stopRecord();

                                        }
                                        else {
                                            showToast("Its first time for culprit");
                                            saved_index=response_index;
                                            count = -1;
                                            mHandler.removeCallbacksAndMessages(null);
                                            encodedImageList.clear();
                                            encodedImageNameList.clear();
                                        }
                                    } catch (JSONException e) {
                                        showToast(e.toString());
                                    }

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            showToast("Volley Error Occurred "+volleyError);

                            encodedImageList.clear();
                            encodedImageNameList.clear();
                            count_images=0;
                        }
                    });

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                            0,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    Volley.newRequestQueue(this).add(jsonObjectRequest);


                    count_images++;
                }

                bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(tmp, bm);
                result.setImageBitmap(bm);
                count++;

            }

            else
            {

                bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(tmp, bm);
                result.setImageBitmap(bm);
                if(try_once==0) {

                    showToast("Try to Detected People");
                    try_once=1;
                    detect_person(bm);
                }

                if(flag==1) {

                    showToast("Server Detected People");
                    hello.setText((KCFTracker3(tmp.getNativeObjAddr(), x_array, y_array, w_array , h_array, kcf_counter) + ""));
                    if(hello.getText().toString().equals("YES")) {
                        kcf_counter=1;
                        hello.setText((KCFTracker3(tmp.getNativeObjAddr(), dumy_array, dumy_array, dumy_array , dumy_array, kcf_counter) + ""));
                        count++;
                    }

                }
                else if(flag==0 || flag==2)
                {
                    showToast("Not Able To Detect");
                    count=0;
                    try_once=0;
                }
                else
                {
                    count=0;
                }

            }

        }

    }



    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LostActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //useless
            case R.id.btn_cancel:{

                mHandler.removeCallbacksAndMessages(null);
                recordbit=0;
                controla=0.0;
                controlb=0.0;
                showToast("Stop!!");
                break;
            }

            default:
                break;
        }
    }




    private void startRecord(){

        restart=0;
        controla=0.0028;
        controlb=0.003;


        final DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){
                @Override

                public void onResult(DJIError error)
                {
                    if (error == null) {
                        recordbit=1;
                        showToast("Record video: success");

                    }else {
                        showToast(error.getDescription());
                    }
                }
            });
        }
    }


    private void stopRecord(){

        controla=0.0;
        controlb=0.0;
        try_once=0;
        handeler_flag=0;
        handeler_flag1=0;
        recordbit=0;
        flag=-1;
        check=-1;
        count=0;
        count_images=0;
        initilize=0;
        kcf_counter=0;
        if(restart==1){
            restart=0;
            startRecord();
        }
        else
        {
            mHandler.removeCallbacksAndMessages(null);
            saved_index=-1;
            xx=0;
            yy=0;
        }

        DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {

            camera.stopRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){

                @Override
                public void onResult(DJIError error)
                {
                    if(error == null) {
                        showToast("Stop recording: success");

                    }else {
                        showToast(error.getDescription());
                    }
                }
            });
        }

    }

}
