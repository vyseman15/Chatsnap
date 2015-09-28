package com.example.speechrecognizer2;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class ShowCamera extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "ShowCamera";
	
   private Size mPreviewSize;
   private List<Size> mSupportedPreviewSizes;
   private SurfaceHolder holdMe;
   private Camera mCamera;
   SurfaceView mSurfaceView;

public ShowCamera(Context context,SurfaceView sv) {
      super(context);
      mSurfaceView = sv;
      //mCamera = camera;
      holdMe = mSurfaceView.getHolder();
      holdMe.addCallback(this);
      holdMe.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
   }

   @Override
   public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	   
	   
   }
   @Override
   public void surfaceCreated(SurfaceHolder holder) {
	    //mCamera = Camera.open();

	    if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {}
	    setDisplayOrientation(mCamera, 90);

	    try {
	    	if(mCamera != null){
	        mCamera.setPreviewDisplay(holdMe);

	        //mCamera.startPreview();
	    	}
	    } catch (IOException exception) {
	        mCamera.release();
	        mCamera = null;
	        Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
	    }
	}
   
   protected void setDisplayOrientation(Camera camera, int angle){
	    Method setDisplayOrientationMethod;
	    try {
	        setDisplayOrientationMethod = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
	        if (setDisplayOrientationMethod != null) {
	            setDisplayOrientationMethod.invoke(camera, new Object[] {angle});
	        }

	    } catch (Exception e1) {
	    	mCamera.release();
	    }
	}
/*
   @Override
   public void surfaceCreated(SurfaceHolder holder) {
      try   {
         mCamera.setPreviewDisplay(holder);
         mCamera.setDisplayOrientation(90);
         mCamera.startPreview(); 
      } catch (IOException e) {
      }
   }
*/
   @Override
   public void surfaceDestroyed(SurfaceHolder holder) {
       // Surface will be destroyed when we return, so stop the preview.
       if (mCamera != null) {
           mCamera.stopPreview();
       }
   }

   

   public void setCameraDefaults(Camera camera)
   {
	   mCamera = camera;
	   if (mCamera != null) {
   		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
   		requestLayout();   
		   
       Camera.Parameters params = mCamera.getParameters();
       
       // Supported picture formats (all devices should support JPEG).
       List<Integer> formats = params.getSupportedPictureFormats();

       if (formats.contains(ImageFormat.JPEG))
       {
           params.setPictureFormat(ImageFormat.JPEG);
           params.setJpegQuality(100);
       }
       else
           params.setPictureFormat(PixelFormat.RGB_565);
       
		List<String> focusModes = params.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			// set the focus mode
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			// set Camera parameters
			
		       // Set the brightness to auto.
		       params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);


		       // Set the scene mode to auto.
		       params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

			mCamera.setParameters(params);
		}
	   }
   }
   
   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       // We purposely disregard child measurements because act as a
       // wrapper to a SurfaceView that centers the camera preview instead
       // of stretching it.
       final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
       final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
       setMeasuredDimension(width, height);

       if (mSupportedPreviewSizes != null) {
           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
       }
   }
   
   private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
       final double ASPECT_TOLERANCE = 0.1;
       double targetRatio = (double) w / h;
       if (sizes == null) return null;

       Size optimalSize = null;
       double minDiff = Double.MAX_VALUE;

       int targetHeight = h;

       // Try to find an size match aspect ratio and size
       for (Size size : sizes) {
           double ratio = (double) size.width / size.height;
           if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
           if (Math.abs(size.height - targetHeight) < minDiff) {
               optimalSize = size;
               minDiff = Math.abs(size.height - targetHeight);
           }
       }

       // Cannot find the one match the aspect ratio, ignore the requirement
       if (optimalSize == null) {
           minDiff = Double.MAX_VALUE;
           for (Size size : sizes) {
               if (Math.abs(size.height - targetHeight) < minDiff) {
                   optimalSize = size;
                   minDiff = Math.abs(size.height - targetHeight);
               }
           }
       }
       return optimalSize;
   }

   @Override
   protected void onLayout(boolean changed, int l, int t, int r, int b) {
       if (changed && getChildCount() > 0) {
           final View child = getChildAt(0);

           final int width = r - l;
           final int height = b - t;

           int previewWidth = width;
           int previewHeight = height;
           if (mPreviewSize != null) {
               previewWidth = mPreviewSize.width;
               previewHeight = mPreviewSize.height;
           }

           // Center the child SurfaceView within the parent.
           if (width * previewHeight > height * previewWidth) {
               final int scaledChildWidth = previewWidth * height / previewHeight;
               child.layout((width - scaledChildWidth) / 2, 0,
                       (width + scaledChildWidth) / 2, height);
           } else {
               final int scaledChildHeight = previewHeight * width / previewWidth;
               child.layout(0, (height - scaledChildHeight) / 2,
                       width, (height + scaledChildHeight) / 2);
           }
       }
   }
   
   
   /**
    * This method configures the camera with a set of defaults for brightness,
    * flash on, camera mode, and picture sizes.
    */
   public void setCameraFlash(Camera camera)
   {
	   mCamera = camera;
	   if (mCamera != null) {
       Camera.Parameters params = mCamera.getParameters();

       // Supported picture formats (all devices should support JPEG).
       List<Integer> formats = params.getSupportedPictureFormats();

       if (formats.contains(ImageFormat.JPEG))
       {
           params.setPictureFormat(ImageFormat.JPEG);
           params.setJpegQuality(100);
       }
       else
           params.setPictureFormat(PixelFormat.RGB_565);

       // Now the supported picture sizes.
       List<Size> sizes = params.getSupportedPictureSizes();
       Camera.Size size = sizes.get(sizes.size()-1);
       params.setPictureSize(size.width, size.height);

       // Set the brightness to auto.
       params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

       // Set the flash mode to on.
       params.setFlashMode(Camera.Parameters.FLASH_MODE_ON );

       // Set the scene mode to auto.
       params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

       // Lastly set the focus to auto.
       params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

       mCamera.setParameters(params);
	   }
   }
   
   /**
    * This method configures the camera with a set of defaults for brightness,
    * flash off, camera mode, and picture sizes.
    */
   public void setCameraFirst(Camera camera)
   {
	   mCamera = camera;
	   if (mCamera != null) {
       Camera.Parameters params = mCamera.getParameters();

       // Supported picture formats (all devices should support JPEG).
       List<Integer> formats = params.getSupportedPictureFormats();

       if (formats.contains(ImageFormat.JPEG))
       {
           params.setPictureFormat(ImageFormat.JPEG);
           params.setJpegQuality(100);
       }
       else
           params.setPictureFormat(PixelFormat.RGB_565);

       // Now the supported picture sizes.
       List<Size> sizes = params.getSupportedPictureSizes();
       Camera.Size size = sizes.get(sizes.size()-1);
       params.setPictureSize(size.width, size.height);

       // Set the brightness to auto.
       params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

       // Set the flash mode to auto.
       params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

       // Set the scene mode to auto.
       params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);

       // Lastly set the focus to auto.
       params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

       mCamera.setParameters(params);
	   }
   }
   
   
}