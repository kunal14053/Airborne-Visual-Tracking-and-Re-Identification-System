package com.dji.FPVDemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.io.FileOutputStream;
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
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.common.camera.CameraSystemState;
import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerControlMode;
import dji.common.flightcontroller.DJIFlightControllerRemoteControllerFlightMode;
import dji.common.flightcontroller.DJIFlightFailsafeOperation;
import dji.common.flightcontroller.DJIFlightOrientationMode;
import dji.common.flightcontroller.DJIIMUState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
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
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import static com.dji.FPVDemo.FPVDemoApplication.getProductInstance;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
/**
 * Created by KunalSaini on 05-Mar-18.
 */

public class Track extends Activity implements TextureView.SurfaceTextureListener,View.OnClickListener {



    public static final String urlUpload1 = "http://192.168.28.124:8000/detect";
    private static final String TAG = "Loaded Flag";
    public native static String KCFTracker(long addr,float a[],float b[],float c[],float d[]);
    public native static String KCFTracker2(long addr,float a,float b,float c,float d);


    float x_array[],y_array[],w_array[],h_array[];
    float[] dumy_array = {0.0f};

    Mat tmp;
    private Handler mHandler;
    ImageView result;
    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    Double controla=0.0028;
    Double controlb=0.003;
    TextView hello;
    TextView hello1;
    TextView hello2;

    Bitmap bmp;
    Bitmap bm;
    int count=0;
    protected DJICodecManager mCodecManager = null;
    int recordbit=0;
    protected TextureView mVideoSurface = null;
    private ToggleButton mRecordBtn;
    int flag=-1;
    int try_once=0;
    int handeler_flag=0;

    ////////////////////////////
    EditText detect_timeout,reid_timeout;
    int detectt,reidt;
    Button mCancel,mTimeout;
    ////////////////////////////

    ///////////////////
    JSONArray jsonArray;
    JSONArray jsonArray_name;
    JSONObject jsonObject;
    ///////////////////

    //////
    FileOutputStream fout1=null;
    /////

    static{
        if(OpenCVLoader.initDebug())
        {
            System.loadLibrary("KCF");
            System.loadLibrary("KCF2");
            //Log.d(TAG,"Successfully Loaded");
        }
        else
        {
            //Log.d(TAG,"OpenCV Not Loaded");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler(Looper.getMainLooper());
        initUI();

        DJIFlightController flightController=new DJIFlightController() {
            @Override
            public boolean isIntelligentFlightAssistantSupported() {
                return false;
            }

            @Override
            public boolean isLandingGearMovable() {
                return false;
            }

            @Override
            public boolean isRtkSupported() {
                return false;
            }

            @Override
            public void setFlightFailsafeOperation(DJIFlightFailsafeOperation djiFlightFailsafeOperation, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getFlightFailsafeOperation(DJICommonCallbacks.DJICompletionCallbackWith<DJIFlightFailsafeOperation> djiCompletionCallbackWith) {

            }

            @Override
            public void takeOff(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void cancelTakeOff(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void autoLanding(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void cancelAutoLanding(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void turnOnMotors(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void turnOffMotors(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void goHome(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void cancelGoHome(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void setHomeLocation(DJILocationCoordinate2D djiLocationCoordinate2D, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getHomeLocation(DJICommonCallbacks.DJICompletionCallbackWith<DJILocationCoordinate2D> djiCompletionCallbackWith) {

            }

            @Override
            public void setGoHomeAltitude(float v, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getGoHomeAltitude(DJICommonCallbacks.DJICompletionCallbackWith<Float> djiCompletionCallbackWith) {

            }

            @Override
            public boolean isOnboardSDKDeviceAvailable() {
                return false;
            }

            @Override
            public void sendDataToOnboardSDKDevice(byte[] bytes, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void setLEDsEnabled(boolean b, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getLEDsEnabled(DJICommonCallbacks.DJICompletionCallbackWith<Boolean> djiCompletionCallbackWith) {

            }

            @Override
            public void setFlightOrientationMode(DJIFlightOrientationMode djiFlightOrientationMode, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void lockCourseUsingCurrentDirection(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public boolean isVirtualStickControlModeAvailable() {
                return true;
            }

            @Override
            public void sendVirtualStickFlightControlData(DJIVirtualStickFlightControlData djiVirtualStickFlightControlData, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void enableVirtualStickControlMode(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void disableVirtualStickControlMode(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void setGoHomeBatteryThreshold(int i, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getGoHomeBatteryThreshold(DJICommonCallbacks.DJICompletionCallbackWith<Integer> djiCompletionCallbackWith) {

            }

            @Override
            public void setLandImmediatelyBatteryThreshold(int i, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getLandImmediatelyBatteryThreshold(DJICommonCallbacks.DJICompletionCallbackWith<Integer> djiCompletionCallbackWith) {

            }

            @Override
            public void setHomeLocationUsingAircraftCurrentLocation(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void setOnIMUStateChangedCallback(DJIFlightControllerDelegate.FlightControllerIMUStateChangedCallback flightControllerIMUStateChangedCallback) {

            }

            @Override
            public void startIMUCalibration(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void startIMUCalibration(int i, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public int getNumberOfIMUs() {
                return 0;
            }

            @Override
            public void setControlMode(DJIFlightControllerControlMode djiFlightControllerControlMode, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getControlMode(DJICommonCallbacks.DJICompletionCallbackWith<DJIFlightControllerControlMode> djiCompletionCallbackWith) {

            }

            @Override
            public void setTripodModeEnabled(Boolean aBoolean, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getTripodModeEnabled(DJICommonCallbacks.DJICompletionCallbackWith<Boolean> djiCompletionCallbackWith) {

            }

            @Override
            public void setAutoQuickSpinEnabled(Boolean aBoolean, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getQuickSpinEnabled(DJICommonCallbacks.DJICompletionCallbackWith<Boolean> djiCompletionCallbackWith) {

            }

            @Override
            public void getMultiSideIMUCalibrationStatus(DJICommonCallbacks.DJICompletionCallbackWith<DJIIMUState> djiCompletionCallbackWith) {

            }

            @Override
            public void setTerrainFollowModeEnabled(Boolean aBoolean, DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void getTerrainFollowModeEnable(DJICommonCallbacks.DJICompletionCallbackWith<Boolean> djiCompletionCallbackWith) {

            }

            @Override
            public void confirmLanding(DJICommonCallbacks.DJICompletionCallback djiCompletionCallback) {

            }

            @Override
            public void isLandingConfirmationNeeded(DJICommonCallbacks.DJICompletionCallbackWith<Boolean> djiCompletionCallbackWith) {

            }

            @Override
            public void getRemoteControllerFlightModeMappingWithCompletion(DJICommonCallbacks.DJICompletionCallbackWith<DJIFlightControllerRemoteControllerFlightMode[]> djiCompletionCallbackWith) {

            }
        };

        FPVDemoApplication.getAircraftInstance().getFlightController().enableVirtualStickControlMode(new DJICommonCallbacks.DJICompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

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


        mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {

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

        //mHandler=new Handler();
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        hello=(TextView)findViewById(R.id.kunal);   //result from KCF Tracker
        hello1=(TextView)findViewById(R.id.kunal1); // velocity in x and y
        hello2=(TextView)findViewById(R.id.kunal2); // scale in x and y
        result=(ImageView)findViewById(R.id.result);

        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        //////////////////////////////
        detectt=10000;
        reidt=15000;
        detect_timeout=(EditText)findViewById(R.id.detect_timeout);
        reid_timeout=(EditText)findViewById(R.id.reid_timeout);
        mCancel = (Button) findViewById(R.id.btn_cancel);
        mTimeout= (Button) findViewById(R.id.btn_timeout);
        mCancel.setOnClickListener(this);
        mTimeout.setOnClickListener(this);
        ///////////////////////////
        ///////////////////////////////
        jsonArray = new JSONArray();
        jsonArray_name = new JSONArray();
        jsonObject= new JSONObject();
        /////////////////////////////

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }


        //////////////////
        String filename="Track";
        if (isExternalStorageAvailable()) {

            File folder = getPrivateStorageDir(filename);
            File file1 = new File(folder, "TrackInfo.txt");
            try {
                fout1 = new FileOutputStream(file1, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /////////////////

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


    ///////////////////////////////////////////
    private static boolean isExternalStorageAvailable() {
        String extStorageState = android.os.Environment.getExternalStorageState();
        if (android.os.Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public File getPrivateStorageDir(String albumName) {
        File file= new File(getApplicationContext().getExternalFilesDir(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {

        }
        return file;
    }
    ///////////////////////////////////////

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }

    public double get_scale_x(double position_x, int size_x) {

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


    public double get_scale_y(double position_y, int size_y) {

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
                            showToast("Error in function");
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                showToast("Volley Error"+volleyError);
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(detectt, 0,(float)2.0));
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
            tmp=new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC4);
            tmp.put(0,0,array);
            Imgproc.cvtColor(tmp,tmp, Imgproc.COLOR_RGBA2RGB);

            if(count>0)
            {

                hello.setText((KCFTracker(tmp.getNativeObjAddr(), dumy_array, dumy_array, dumy_array , dumy_array) + ""));
                String s=hello.getText().toString();

                String[] parts = s.split("@");
                double mean_x = 0.0, mean_y = 0.0;
                for (int i = 0; i < parts.length; i++) {
                    String[] sub_parts = parts[i].split(":");
                    double a = Float.parseFloat(sub_parts[0]);
                    double b = Float.parseFloat(sub_parts[1]);
                    double c = Float.parseFloat(sub_parts[2]);
                    double d = Float.parseFloat(sub_parts[3]);
                    mean_x = mean_x + (a + c/2.0);
                    mean_y = mean_y + (b + d/2.0);
                }
                mean_x = mean_x / parts.length;
                mean_y = mean_y / parts.length;
                double scale_x=get_scale_x(mean_x,tmp.rows()/2);
                double scale_y=get_scale_y(mean_y,tmp.cols()/2);
                double a=scale_x*controla*(mean_x - (tmp.cols() / 2));
                double b=scale_y*controlb*((tmp.rows() / 2) - mean_y);

                hello1.setText("velocity:" + a + " " + b);
                hello2.setText("scale: " + scale_x + " " + scale_y);
                final float af = (float) a;
                final float bf = (float) b;
                final String sf=s;
                final double scale_xf=scale_x;
                final double scale_yf=scale_y;
                ///////////////////////////
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    jsonArray_name.put(recordbit+","+s+","+scale_x+","+scale_y+","+a+","+b);
                    jsonArray.put(encodedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //////////////////////////////
                //if(handeler_flag==0) {
                //    handeler_flag=1;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FPVDemoApplication.getAircraftInstance().
                                getFlightController().sendVirtualStickFlightControlData(
                                new DJIVirtualStickFlightControlData(
                                        af,bf, 0, 0
                                ), new DJICommonCallbacks.DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                }
                        );

                        /*try {
                            jsonArray_name.put(recordbit+","+sf+","+scale_xf+","+scale_yf+","+af+","+bf);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/

                    }
                },100);

                //}


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
                    hello.setText((KCFTracker(tmp.getNativeObjAddr(), x_array, y_array, w_array , h_array) + ""));
                    if(hello.getText().toString().equals("YES")) {
                        hello.setText((KCFTracker(tmp.getNativeObjAddr(), dumy_array, dumy_array, dumy_array , dumy_array) + ""));
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

            recordbit++;

        }

    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Track.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    ////////////////////////////////////////////////////////
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_cancel:{

                controla=0.0;
                controlb=0.0;
                try_once=0;
                recordbit=0;
                mHandler.removeCallbacksAndMessages(null);
                handeler_flag=0;

                showToast("Stop!!");
                break;
            }

            case R.id.btn_timeout:{

                String dts=detect_timeout.getText().toString();
                String rts=reid_timeout.getText().toString();
                if(!dts.matches("")) {
                    detectt = Integer.parseInt(dts);
                }if(!rts.matches("")) {
                    reidt = Integer.parseInt(rts);
                }

                showToast("Detect Timeout: "+detectt+" Reid Timeout: "+reidt);

            }

            default:
                break;
        }
    }

//////////////////////////////////////////////////////////////////////////


    private void startRecord(){
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
                        showToast("Error: "+error.getDescription());
                    }
                }
            });
        }
    }


    private void stopRecord(){

        controla=0.0;
        controlb=0.0;
        try_once=0;
        mHandler.removeCallbacksAndMessages(null);
        handeler_flag=0;
        recordbit=0;
        ////////////////////
        /*try {
            jsonObject.put("Name", jsonArray_name);
            jsonObject.put("Image", jsonArray);
            fout1.write(jsonObject.toString().getBytes());
            fout1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        ////////////////////
        DJICamera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {

            camera.stopRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){

                @Override
                public void onResult(DJIError error)
                {
                    if(error == null) {
                        showToast("Stop recording: success");
                        try {
                            jsonObject.put("Name", jsonArray_name);
                            jsonObject.put("Image", jsonArray);
                            fout1.write(jsonObject.toString().getBytes());
                            fout1.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        showToast("Error: "+error.getDescription());
                    }
                }
            });
        }

    }

}
