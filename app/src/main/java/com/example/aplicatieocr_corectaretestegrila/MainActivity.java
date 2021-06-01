package com.example.aplicatieocr_corectaretestegrila;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "aici";
    private String flag;

    private TextView textView;
    private TextView textView1;
    private SurfaceView surfaceView;
    private Button mLogout;

    private CameraSource cameraSource;
    private TextRecognizer textRecognizer;

    private TextToSpeech textToSpeech;
    private String testResult = null;
    private String answersResult = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        mLogout = findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }

    private void textRecognizer(){
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setRequestedPreviewSize(1280, 1024)
                .build();

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i<sparseArray.size(); ++i){
                    TextBlock textBlock = sparseArray.valueAt(i);
                    if (textBlock != null && textBlock.getValue() !=null){
                        stringBuilder.append(textBlock.getValue() + " ");
                    }
                }

                final String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (flag.equals("test")) {
                            testResult = stringText;
                        } else if (flag.equals("results")) {
                            answersResult = stringText;
                        }
                    }
                });
            }
        });
    }


    public void scanTest(View view) {
        setContentView(R.layout.camera);
        textRecognizer();
        flag = "test";
    }

    public void scanResults(View view) {
        setContentView(R.layout.camera);
        textRecognizer();
        flag = "results";
    }

    public void takePhoto(View view) {
        setContentView(R.layout.activity_main);
        if (flag.equals("results")) {
            Toast.makeText(this, "Baremul a fost scanat cu succes!", Toast.LENGTH_LONG).show();

        } else if (flag.equals("test")) {
            Toast.makeText(this, "Testul a fost scanat cu succes!", Toast.LENGTH_LONG).show();
        }
    }

    public void getTestScore(View view) {
        try {
            textView1 = findViewById(R.id.textView4);
            testResult.replaceAll("[\\s|\\u00A0]+", "");
            answersResult.replaceAll("[\\s|\\u00A0]+", "");
            // System.out.print("trim test: " + testResult);
            // System.out.print("trim answers: " + answersResult);
            int questionNo = 0;
            int correctAnswers = 0;
            for (int i = 0; i < testResult.length(); i++) {
                char answer;
                if (testResult.charAt(i) == ')') {
                    answer = testResult.charAt(i - 1);
                    questionNo++;
                    if (checkIfCorrect(questionNo, answer)) {
                        correctAnswers++;
                    }
                    //System.out.println("answer: " + answer);
                }
                //System.out.println("answer: " + i + ": " + questionResult);
            }
            Log.v(TAG, testResult);
            Log.v(TAG, answersResult);
//            if (testResult == null || answersResult == null) {
//                Toast.makeText(this, "Nu ați scanat cele două imagini!", Toast.LENGTH_LONG).show();
//            }
            textView1.setText("A răspuns corect la " + correctAnswers + " întrebări din " + questionNo);
        } catch (Exception e) {
            Toast.makeText(this, "Nu ați scanat cele două imagini!", Toast.LENGTH_LONG).show();
        }
    }

    public Boolean checkIfCorrect(int questionNo, char answer) {
        for (int i = 0; i < answersResult.length(); i ++) {
            if(Character.getNumericValue(answersResult.charAt(i)) == questionNo) {
                for (int j = i; j < answersResult.length(); j ++) {
                    if(answersResult.charAt(j) == ')') {
                        System.out.println ("Nr. intrebare: " + questionNo);
                        System.out.println ("compar " + answer + " cu " +  answersResult.charAt(j-1));
                        if(answer == answersResult.charAt(j-1)) return true;
                        else return false;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        setContentView(R.layout.activity_main);
    }

}
