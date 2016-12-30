package asapp.benneb.csi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.Timer;
import java.util.TimerTask;




public class MainActivity extends AppCompatActivity {
    Button panicBtn ;
    Drawable background ;
    int alarmTime = 1500;
    Timer timer;
    TimerTask timerTask;
    Drawable draw;
    int height;
    final Handler handler = new Handler();
    int count = 0;
    Context context;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    public void setSettingsView()
    {
        setContentView(R.layout.settings);
        final EditText name = (EditText)findViewById(R.id.name);
        final EditText surname = (EditText)findViewById(R.id.surname);
        final EditText phone = (EditText)findViewById(R.id.phone);
        final EditText address = (EditText)findViewById(R.id.address);
        final EditText direction = (EditText)findViewById(R.id.direction);
        final EditText medical_aid = (EditText)findViewById(R.id.medical_aid);
        final EditText medical_number = (EditText)findViewById(R.id.medical_number);
        final EditText medical_condition = (EditText)findViewById(R.id.medical_condition);
        final EditText sms1 = (EditText)findViewById(R.id.sms1);
        final EditText sms2 = (EditText)findViewById(R.id.sms2);
        final EditText sms3 = (EditText)findViewById(R.id.sms3);

        name.setText(sName);
        surname.setText(sSurname);
        phone.setText(sPhone);
        address.setText(sAddress);
        direction.setText(sDirection);
        medical_aid.setText(sMedicalAid);
        medical_number.setText(sMedicalNum);
        medical_condition.setText(sMedicalCond);
        sms1.setText(sSms1);
        sms2.setText(sSms2);
        sms3.setText(sSms3);

        Button settingsButton= (Button) findViewById(R.id.save_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String writeStr = "";
                writeStr = writeStr + "name:" + name.getText() + ",";
                writeStr = writeStr + "surname:" + surname.getText() + ",";
                writeStr = writeStr + "phone:" + phone.getText() + ",";
                writeStr = writeStr + "address:" + address.getText() + ",";
                writeStr = writeStr + "direction:" + direction.getText() + ",";
                writeStr = writeStr + "medical_aid:" + medical_aid.getText() + ",";
                writeStr = writeStr + "medical_number:" + medical_number.getText() + ",";
                writeStr = writeStr + "medical_condition:" + medical_condition.getText() + ",";
                writeStr = writeStr + "sms1:" + sms1.getText() + ",";
                writeStr = writeStr + "sms2:" + sms2.getText() + ",";
                writeStr = writeStr + "sms3:" + sms3.getText() + ",";

                writeSettings(writeStr);
                setMainView();
            }
        });

    }

    public void setMainView()
    {
        setContentView(R.layout.activity_main);


        panicBtn = (Button)findViewById(R.id.panicBtn);
        background = panicBtn.getBackground();

        panicBtn.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                panicBtn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                height = panicBtn.getMeasuredHeight();
                android.view.ViewGroup.LayoutParams params = null;
                params = panicBtn.getLayoutParams();
                params.height = height;
                params.width = height;
                panicBtn.setLayoutParams(params);

                panicBtn.setText("Hold to\nPanic");

                LayerDrawable lBackground = (LayerDrawable)background;
                draw = lBackground.findDrawableByLayerId(R.id.btn_progress);
                draw.setBounds(0,0,0,0);
            }
        });

        panicBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    panicBtn.setText("Hold to\nPanic");
                    draw.setBounds(0,0,0,0);
                    count = 0;
                    startTimer();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    stoptimertask();
                    if(count != alarmTime)
                    {
                        count = 0;
                        draw.setBounds(0,0,0,0);
                    }
                }
                return false;
            }
        });

        String txt = readSettings();
        parseSettings(txt);
//        Toast.makeText(getApplicationContext(), "Settings " + txt, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        int id = item.getItemId();
//        Toast.makeText(getApplicationContext(), "menu item pressed" + id, Toast.LENGTH_SHORT).show();
        setSettingsView();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i= new Intent(this, HardwareTriggerService.class);
        startService(i);

        context = this;
        setMainView();

    }

    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, 0, 10); //
    }

    public void stoptimertask() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if(count != alarmTime)
        {
            count = 0;
            draw.setBounds(0,0,0,0);
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                if(count != alarmTime)
                    count = count +10;
                else
                {
                    stoptimertask();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            panicBtn.setText("Sending\nPanic");
                            PanicAlert panicAlert = getPanicAlert(context);
                            panicAlert.vibrate();
                            panicAlert.getGPS();
                            panicAlert.takePhoto();
                        }
                    });

                }

                float percent = (count * 1.0f) / alarmTime;
                final int pos = Math.round( height * percent);
                final int size = Math.round(height - pos);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        draw.setBounds(size,size,pos,pos);

                    }
                });

            }
        };
    }

    protected PanicAlert getPanicAlert(Context context) {
        return new PanicAlert(context,new PanicAlert.Command()
        {
            public void execute(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        panicBtn.setText("Panic\nSent");
                    }
                });
            }
        });
    }

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
            setSettingsView();
            return "NIKS SETTINGS NIE";
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void writeSettings(String data)
    {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
