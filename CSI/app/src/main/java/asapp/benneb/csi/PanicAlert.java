package asapp.benneb.csi;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.api.client.http.HttpResponse;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import asapp.benneb.csi.common.AppConstants;

import static android.R.attr.port;

public class PanicAlert implements LocationListener {

    private Context context;
    private LocationManager locationManager;

    private boolean GPSDone = false;
    private double GPSLat = 0;
    private double GPSLon = 0;
    private boolean PhotoDone = false;
    private List<String> photoLinkList;

    String sName;
    String sSurname;
    String sPhone;
    String sAddress;
    String sDirection;
    String sMedicalAid;
    String sMedicalNum;
    String sMedicalCond;
    String sSms1;
    String sSms2;
    String sSms3;

    public void parseSettings(String rawSettings)
    {
        String[] separated = rawSettings.split(",");
        for (String item : separated)
        {
            System.out.println("item = " + item);
            String[] separated_val = item.split(":");
            if(separated_val.length == 2) {
                switch (separated_val[0]) {
                    case "name":
                        sName = separated_val[1];
                        break;
                    case "surname":
                        sSurname = separated_val[1];
                        break;
                    case "phone":
                        sPhone = separated_val[1];
                        break;
                    case "address":
                        sAddress = separated_val[1];
                        break;
                    case "direction":
                        sDirection = separated_val[1];
                        break;
                    case "medical_aid":
                        sMedicalAid = separated_val[1];
                        break;
                    case "medical_number":
                        sMedicalNum = separated_val[1];
                        break;
                    case "medical_condition":
                        sMedicalCond = separated_val[1];
                        break;
                    case "sms1":
                        sSms1 = separated_val[1];
                        break;
                    case "sms2":
                        sSms2 = separated_val[1];
                        break;
                    case "sms3":
                        sSms3 = separated_val[1];
                        break;
                }
            }
        }
    }

    public String readSettings()
    {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public interface Command{
        void execute();
    }
    Command callBack;
    public PanicAlert(Context context,Command callback) {
        this.callBack = callback;
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
    }
    public void vibrate() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(AppConstants.HAPTIC_FEEDBACK_DURATION);
    }

    public void sms()
    {
        String txt = readSettings();
        parseSettings(txt);
        Log.d("blah", "SMS here");
//        String name = "Jan";
//        String surname = "Pan";
        String message = "Please help\nLat:" + GPSLat + " Lon:" +GPSLon + "\nhttps://www.google.co.za/maps?q=" + GPSLat + "," + GPSLon ;

        for (int i=0; i<photoLinkList.size(); i++) {
            Log.d("blah", photoLinkList.get(i));
            message = message + "\n" + photoLinkList.get(i);
        }
        Log.d("blah", "SMS Message " + message);
        try {
            SmsManager smsManager = SmsManager.getDefault();
            if(sSms1 != "")
                smsManager.sendTextMessage(sSms1, null, message, null, null);
            if(sSms2 != "")
                smsManager.sendTextMessage(sSms2, null, message, null, null);
            if(sSms3 != "")
                smsManager.sendTextMessage(sSms3, null, message, null, null);

            Log.d("blah", "HERE");
            if(this.callBack != null)
            {
                this.callBack.execute();
            }
            this.vibrate();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void uploadFile(final int index,final int cameraCount)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Map config = new HashMap();
                config.put("cloud_name", "dzlt1d4ky");
                config.put("api_key", "355435451865185");
                config.put("api_secret", "XD6OauleiqAGlmvMz0FtKp7O7po");
                Cloudinary cloudinary = new Cloudinary(config);
                try {
                    Map uploadMap = cloudinary.uploader().upload(Environment.getExternalStorageDirectory()+"/dirr/"+index+".jpg",
                            ObjectUtils.emptyMap());
                           // ObjectUtils.asMap("public_id", index + ""));
                    String fileName = (String) uploadMap.get("public_id");
//
                    try {
                        String tinyUrl = makeRequest("http://res.cloudinary.com/dzlt1d4ky/image/upload/"+fileName+".jpg");
                        photoLinkList.add(tinyUrl);
                        if(photoLinkList.size() == cameraCount)
                        {
                            PhotoDone = true;
                            Log.d("blah", "PHOTO DONE");
                            if(GPSDone)
                            {
                                Log.d("blah", "DO SMS NOW");
                                sms();
                            }
                        }else
                        {

                        }

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static String makeRequest(String longUrl)
            throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpost = new HttpPost("https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyBEDSkiPMMh_qOoC1n-2mRv2A0H2_QaLmk");

        JSONObject holder = new JSONObject();
        holder.put("longUrl", longUrl);

        StringEntity se = new StringEntity(holder.toString());
        httpost.setEntity(se);
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");

        ResponseHandler responseHandler = new BasicResponseHandler();
        String response = (String) httpclient.execute(httpost, responseHandler);
        JSONObject obj = new JSONObject(response);
        String shortUrl = obj.getString("id");
        return shortUrl;
    }

    private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea < resultArea) {
                    result=size;
                }
            }
        }

        return(result);
    }

    private void takePhoto2(final int index,final int cameraCount) {
        Log.e("blah", "takePhoto2 " + index + " " + cameraCount);
        Camera camera = null;
        try {

            camera = Camera.open(index);
            Camera.Parameters params = camera.getParameters();

            List<Camera.Size> sizes = params.getSupportedPictureSizes();

            Camera.Size pictureSize=getSmallestPictureSize(params);
            params.setPictureSize(pictureSize.width,
                    pictureSize.height);
            params.setPictureFormat(ImageFormat.JPEG);
            camera.setParameters(params);

        } catch (RuntimeException e) {

            Log.e("blah", "Camera not available", e);
            return;
        }

        if (null == camera) {

            Log.e("blah", "Could not get camera instance");
            return;
        }

        Log.d("blah", "Got the camera, creating the dummy surface texture");
        SurfaceTexture dummySurfaceTexture = new SurfaceTexture(0);

        try {

            camera.setPreviewTexture(dummySurfaceTexture);

        } catch (Exception e) {

            Log.e("blah", "Could not set the surface preview texture", e);
        }

        Log.d("blah", "Preview texture set, starting preview");

        camera.startPreview();

        Log.d("blah", "Preview started");

        camera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                Log.d("blah", "Photo taken, stopping preview");

                camera.stopPreview();

                Log.d("blah", "Preview stopped, releasing camera");

                camera.release();

                Log.d("blah", "Camera released");

                Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);

                if(bitmap!=null){

                    File file=new File(Environment.getExternalStorageDirectory()+"/dirr");
                    if(!file.isDirectory()){
                        file.mkdir();
                    }

                    file=new File(Environment.getExternalStorageDirectory()+"/dirr",index + ".jpg");


                    try
                    {
                        FileOutputStream fileOutputStream=new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    catch(Exception exception)
                    {
                        exception.printStackTrace();
                    }

                }
                uploadFile(index,cameraCount);

                if(index < cameraCount -1)
                {
                    takePhoto2(index+1,cameraCount);
                }
            }
        });
    }
    public void takePhoto() {

        photoLinkList = new ArrayList<String>();
        PhotoDone = false;

        Log.d("blah", "Preparing to take photo");
        Camera camera = null;
        int cameraCount = Camera.getNumberOfCameras();
        Log.d("blah", "cameraCount " + cameraCount);
        int camIdx = 0;
        takePhoto2(0,cameraCount);

    }

    public void getGPS()
    {
        PhotoDone = false;
        GPSDone = false;
        GPSLat = 0;
        GPSLon = 0;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d("blah", "GPS is Enabled in your devide");
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                GPSDone = true;
                GPSLat = location.getLatitude();
                GPSLon = location.getLongitude();
                if(PhotoDone)
                {
                    Log.v("test","Do SMS");
                    sms();
                }
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }else{
            Log.d("blah", "GPS is DISEnabled in your devide");
            GPSDone = true;
            GPSLat = 0;
            GPSLon = 0;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            locationManager.removeUpdates(this);
            GPSDone = true;
            GPSLat = location.getLatitude();
            GPSLon = location.getLongitude();
            if(PhotoDone)
            {
                sms();
                Log.v("test","Do SMS");
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
