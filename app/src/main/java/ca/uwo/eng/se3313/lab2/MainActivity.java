package ca.uwo.eng.se3313.lab2;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    /**
     * View that showcases the image
     */
    private ImageView ivDisplay;

    /**
     * Skip button
     */
    private ImageButton skipBtn;

    /**
     * Progress bar showing how many seconds left (percentage).
     */
    private ProgressBar pbTimeLeft;

    /**
     * Label showing the seconds left.
     */
    private TextView tvTimeLeft;

    /**
     * Control to change the interval between switching images.
     */
    private SeekBar sbWaitTime;

    /**
     * Editable text to change the interval with {@link #sbWaitTime}.
     */
    private EditText etWaitTime;

    /**
     * ArrayList that holds all the asynchronously downloaded Bitmap images.
     */
    private ArrayList<Bitmap> images;

    /**
     * ObjectAnimator instance that animates the progress bar to increase over time.
     */
    private ObjectAnimator objectAnimator;

    /**
     * CountDownTimer instance that allows a countdown timer to show time running down.
     */
    private CountDownTimer countDownTimer;

    /**
     * Instance of the downloader class that enables downloading in a new thread
     */
    private AsynchronousDownloader asynchronousDownloader;

    /**
     * The current Bitmap image being displayed in the ImageView
     */
    private Bitmap currentImage;

    /**
     * List of image URLs of cute animals that will be displayed.
     */
    private static final ArrayList<String> urlList = new ArrayList<String>() {{
        add("http://i.imgur.com/CPqbVW8.jpg");
        add("http://i.imgur.com/Ckf5OeO.jpg");
        add("http://i.imgur.com/3jq1bv7.jpg");
        add("http://i.imgur.com/8bSITuc.jpg");
        add("http://i.imgur.com/JfKH8wd.jpg");
        add("http://i.imgur.com/KDfJruL.jpg");
        add("http://i.imgur.com/o6c6dVb.jpg");
        add("http://i.imgur.com/B1bUG2K.jpg");
        add("http://i.imgur.com/AfxvVuq.jpg");
        add("http://i.imgur.com/DSDtm.jpg");
        add("http://i.imgur.com/SAVYw7S.jpg");
        add("http://i.imgur.com/4HznKil.jpg");
        add("http://i.imgur.com/meeB00V.jpg");
        add("http://i.imgur.com/CPh0SRT.jpg");
        add("http://i.imgur.com/8niPBvE.jpg");
        add("http://i.imgur.com/dci41f3.jpg");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get all the UI components from the XML layout file
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        skipBtn = (ImageButton) findViewById(R.id.btnSkip);
        pbTimeLeft = (ProgressBar) findViewById(R.id.pbTimeLeft);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        sbWaitTime = (SeekBar) findViewById(R.id.sbWaitTime);
        etWaitTime = (EditText) findViewById(R.id.etWaitTime);

        // Insert your code here (and within the class!)
        //Generate a random image to start with
        shuffleImages();
        //Count down starts from 50 seconds by default
        seekBarFunctionality(50);

        //Event listener that tracks if the SeekBar value changes and executes corresponding functionality
        sbWaitTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minimum = 5;
                if(progress > minimum){
                    tvTimeLeft.setText(String.valueOf(progress));
                    etWaitTime.setText(String.valueOf(progress));
                    seekBarFunctionality(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Event listener that tracks if the EditText value changes and executes corresponding functionality
        etWaitTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()) {
                    int value = Integer.parseInt(editable.toString());
                    if(value < 5 || value > 60) {
                        etWaitTime.setError("Number should be between 5 and 60");
                    }else {
                        etWaitTime.setError(null);
                        seekBarFunctionality(Integer.parseInt(editable.toString()));
                    }
                }
            }
        });

        //Event listener to generate a new image every time the button is clicked
        skipBtn.setOnClickListener(v -> shuffleImages());
    }

    /**
     * seekBarFunctionality method sets the visibility of the progress bar and animates it as the timer counts down
     *
     * @param maxValue , The maximum value from which the counter starts counting down
     */
    private void seekBarFunctionality(int maxValue){
        pbTimeLeft.setVisibility(View.VISIBLE);
        pbTimeLeft.setProgress(0);
        pbTimeLeft.setMax(maxValue);
        objectAnimator = ObjectAnimator.ofInt(pbTimeLeft, "progress",0, maxValue);
        objectAnimator.setDuration(maxValue*1000);
        objectAnimator.start();
        startCounter(maxValue);
    }

    /**
     * startCounter method cancels the current timer and starts a new count down from the specific max value
     *
     * @param maxValue, The maximum value from which the counter starts counting down
     */
    private void startCounter(int maxValue){
        if(countDownTimer != null){
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(maxValue*1000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeLeft.setText(String.valueOf(millisUntilFinished / 1000));
                if(tvTimeLeft.getText().equals("0")){
                    shuffleImages();
                    pbTimeLeft.setProgress(0);
                    seekBarFunctionality(maxValue);
                }
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    /**
     * This method executes the asynchronous task and gets the
     * BitMap associated with the image url and then sets it as the ImageView Bitmap
     *
     * If the task returns null, the ImageView is set to the cat error drawable
     */
    private void shuffleImages() {
        //Async task to download all the images and store them as an ArrayList of bitmaps
        asynchronousDownloader = new AsynchronousDownloader(urlList.get(generateRandomNumber()));
        try {
            currentImage = asynchronousDownloader.execute().get();
            if(currentImage == null) {
                ivDisplay.setImageResource(R.drawable.cat_error);
            }else{
                ivDisplay.setImageBitmap(currentImage);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method generates a random number that selects the url to be downloaded from the ArrayList of URl's
     *
     * @return , Integer between 0 and list_size - 1
     */
    private int generateRandomNumber(){
        Random rand = new Random();
        return rand.nextInt(urlList.size() - 1);
    }
}