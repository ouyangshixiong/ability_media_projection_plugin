package cn.abilitygame.ability_media_projection_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** AbilityMediaProjectionPlugin */
public class AbilityMediaProjectionPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.ActivityResultListener {

  private static final String TAG = AbilityMediaProjectionPlugin.class.getSimpleName();
  private static Activity activity;
  private static final int BASE_PERMISSION_REQUEST_CODE = 222;
  private static final int SCREEN_RECORD_REQUEST_CODE = 333;

  private Context applicationContext;
  private Result _result;
  private MethodChannel channel;
  private MediaRecorder mediaRecorder;
  private MediaProjectionManager mediaProjectionManager;
  private MediaProjection mediaProjection;
  private ActivityCallBack activityCallBack;
  private VirtualDisplay virtualDisplay;
  private Surface surface;
  private String storePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator;
  private int DISPLAY_WIDTH;
  private int DISPLAY_HEIGHT;
  private int DPI;

  private boolean basePermission = false;

  private String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    _result = result;
    if (call.method.equals("startRecordScreen")) {
      if( !basePermission ){
        getPermission();
      } else {}
      if ( !checkPermission() ){
        result.success("基础权限申请不成功，权限不足！");
        return;
      } else {}

      Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
      ActivityCompat.startActivityForResult(activity, permissionIntent, SCREEN_RECORD_REQUEST_CODE, null);
    } else if (call.method.equals("stopRecordScreen")){
        if( mediaRecorder != null ){
          stopRecordScreen();
          result.success(storePath + "test.mp4");
        }else{
          result.error("501","stop Record Screen Failed!", "");
        }
    } else {
      result.notImplemented();
    }
  }

  private void getPermission() {
    ActivityCompat.requestPermissions(activity, permissions, BASE_PERMISSION_REQUEST_CODE);
  }

  private boolean checkPermission() {
    for (String permission : permissions) {
      if(ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private void initMediaRecorder() {
    try {
      mediaRecorder = new MediaRecorder();
      mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
      mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
      mediaRecorder.setOutputFile( storePath + "test.mp4" );
      mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
      mediaRecorder.setVideoFrameRate(30);
//      mediaRecorder.setVideoEncodingBitRate(5*DISPLAY_HEIGHT*DISPLAY_WIDTH);
      mediaRecorder.prepare();
    } catch (Exception e) {
      Log.i( TAG, e.getMessage() );
    }
  }

  private void stopRecordScreen() {
    try {
      mediaRecorder.stop();
      mediaRecorder.reset();
    } catch (Exception e) {
      Log.e(TAG, e.toString());
    }finally {
      cleanVirtualDisplay();
    }
  }

  private void cleanVirtualDisplay(){
    if (mediaProjection != null) {
      mediaProjection.unregisterCallback(activityCallBack);
      mediaProjection.stop();
      mediaProjection = null;
    }else{}
    if ( virtualDisplay != null ){
      virtualDisplay.release();
    }else{}
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if( requestCode == SCREEN_RECORD_REQUEST_CODE ) {
      if( resultCode == Activity.RESULT_OK ){
        initMediaRecorder();
        mediaProjection = mediaProjectionManager.getMediaProjection(activity.RESULT_OK,data);
        if (mediaProjection == null){
          _result.error("500","mediaProjection is null","");
        }else{}
        activityCallBack = new ActivityCallBack();
        mediaProjection.registerCallback(activityCallBack, null);
        // start前先把virtualDisplay创建好
        surface = mediaRecorder.getSurface();
        virtualDisplay = mediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, DPI, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null);
        mediaRecorder.start();

        _result.success("start media recorder successfully!");
        return true;
      }else {
        _result.error("500", "Activity callback:" + resultCode, "");
      }
    } else if (requestCode == BASE_PERMISSION_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Log.i(TAG, "成功获得了基础权限");
        basePermission = true;
      }else{}
    }else{}
    return false;
  }

  private void setup( Context applicationContext, BinaryMessenger messenger){
    this.applicationContext = applicationContext;
//    this.activity = activity;
    channel = new MethodChannel(messenger, "ability_media_projection_plugin");
    channel.setMethodCallHandler(this);

    mediaProjectionManager = (MediaProjectionManager) applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    DisplayMetrics displayMetrics = applicationContext.getResources().getDisplayMetrics();
    DISPLAY_WIDTH = displayMetrics.widthPixels;
    DISPLAY_HEIGHT = displayMetrics.heightPixels;
    DPI = displayMetrics.densityDpi;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    setup(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
  }

  public static void registerWith(Registrar registrar) {
    final AbilityMediaProjectionPlugin _instance = new AbilityMediaProjectionPlugin();
    registrar.addActivityResultListener(_instance);
    activity = registrar.activity();
    _instance.setup(registrar.context(), registrar.messenger());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }

  class ActivityCallBack extends MediaProjection.Callback {

    @Override
    public void onStop() {
      Log.i(TAG, "ActivityCallBack.onStop");
//      stopRecordScreen();
    }
  }
}
