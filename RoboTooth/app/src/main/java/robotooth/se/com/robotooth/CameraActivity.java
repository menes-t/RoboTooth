package robotooth.se.com.robotooth;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {

    private static final String TAG = "CameraActivity";
    private CameraBridgeViewBase mOpenCvCameraView;
    private ConstraintLayout constraintLayout;


    //sensor icin degiskenler
    private SensorManager sensorManager;
    private float[] lastMagFields = new float[3];
    private float[] lastAccels = new float[3];
    private float[] rotationMatrix = new float[16];
    private float[] orientation = new float[4];

    private static long lastFoundAt;
    private static long lastNotFoundAt;

    public static int screenOrien = 0; // 0 vertical 1 horizontal through left 2 horizontal through right

    List<MatOfPoint2f> points = new ArrayList<>();

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        mOpenCvCameraView.enableView();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        constraintLayout = findViewById(R.id.camera_view_layout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, lastAccels, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, lastMagFields, 0, 3);
                break;
            default:
                return;
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccels, lastMagFields)) {
            SensorManager.getOrientation(rotationMatrix, orientation);

            float xAxis = (float) Math.toDegrees(orientation[1]);
            float yAxis = (float) Math.toDegrees(orientation[2]);

            int orientation = Configuration.ORIENTATION_UNDEFINED;
            if ((yAxis <= 25) && (yAxis >= -25) && (xAxis >= -160)) {
                screenOrien = 0;
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else if ((yAxis < -25) && (xAxis >= -20)) {
                screenOrien = 1;
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            } else if ((yAxis > 25) && (xAxis >= -20)){
                screenOrien = 2;
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat image = inputFrame.rgba();
        //TODO
        List<Circle> circles = houghTransformCircle(image);

        if(circles.size() != 0 && calculateTheTimePassed(lastFoundAt) > 15){
            MainActivity.writeCommand("f");
            lastFoundAt = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
            Log.i(TAG,"XXX Circle found and sent to Sparki");
        }else if(calculateTheTimePassed(lastNotFoundAt) > 1){
            MainActivity.writeCommand("g");
            lastNotFoundAt = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
            Log.i(TAG,"XXX Circle found and sent to Sparki");
        }

        for (Circle point :
                circles) {
            drawCircle(point, image);
        }
        return image;
    }
    private void drawCircle (Circle c, Mat image) {
        Point center = new Point(c.getX(), c.getY());
        Imgproc.circle(image, center, (int) c.getRadius(), new Scalar(127, 255, 212), 3);
    }
    private long calculateTheTimePassed(long fromWhen){
        return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()) - fromWhen;
    }
    public static List<Circle> houghTransformCircle(Mat image){

        Imgproc.GaussianBlur(image, image, new Size(3,3), 2, 2);//image daha soft olmasi icin blurlar.
        List<Circle> houghPointList = new ArrayList<Circle>();
        Mat circles = new Mat();
        Mat tempMat = new Mat();
        Imgproc.cvtColor(image, tempMat, Imgproc.COLOR_RGB2GRAY);//Gray scale a gecis
        Imgproc.HoughCircles(tempMat,circles,Imgproc.CV_HOUGH_GRADIENT,2,20,200,100,10,200);//burdaki sayi degerlerini ne boyutlarda circle tespit etmek istiyosaniz duzenlemeniz gerekir
r0
        for (int x = 0; x < circles.cols(); x++) {//circlelari listeye atar
            Circle temp = new Circle(0,0,0);
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            temp.setX(Math.round(c[0]));
            temp.setY(Math.round(c[1]));
            temp.setRadius(Math.round(c[2]));
            houghPointList.add(temp);
        }
        return houghPointList;
    }

}
