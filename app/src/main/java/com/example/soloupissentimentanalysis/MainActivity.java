package com.example.soloupissentimentanalysis;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TensorFlowInferenceInterface tf;
    private static final String MODEL_ASSETS = "keras_sentiment.pb";
    private String INPUT_NAME = "embedding_2_input";
    private String OUTPUT_NAME = "output_1";
    float[] PREDICTIONS = new float[1000];
    private static final String vocabFilename = "result.json";

    public static final int POS_LABEL = 1;
    public static final int NEG_LABEL = 0;
    private static final int maxLenght = 200;

    private String stringText;
    private Map<String, Integer> vocabMap = null;
    private String[] wordsTrancuated;

    //Load the tensorflow inference library
    /*static {
        System.loadLibrary("tensorflow_inference");
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tf = new TensorFlowInferenceInterface(getAssets(), MODEL_ASSETS);

        //text manipulation
        /*stringText = "This movie had the best acting and the dialogue was so good. I loved it.";*/
        stringText = "If she fails, then more chaos and upheaval follows, and there’s no chance she can deliver Brexit in time to avoid EU elections. But there’s little to lose by trying -- if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls. if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the pollsif she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls if she doesn’t put the bill to Parliament in the next couple of weeks, there’s no way the U.K. can leave before the polls";

        float[] value = transformText(stringText);

        //make prediction
        predict(value);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private float[] transformText(String textToFormat) {

        //Replace Upper case letters and split string
        String[] words = textToFormat.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        Log.e("LENGTH", String.valueOf(words.length));
        if (words.length>=200){
            wordsTrancuated = Arrays.copyOf(words,200);
        }else{
            wordsTrancuated = Arrays.copyOf(words,words.length);
        }

        Log.e("LENGTH", String.valueOf(wordsTrancuated.length));
        /*//Make new array with only 200 inputs
        String[] wordsTrancuated = new String[200];
        for (int o = 0; o < wordsTrancuated.length; o++) {
            wordsTrancuated[o] = "nnnn";
        }
        Log.e("LENGTH", String.valueOf(words.length));
        //Trancuate array to 200 length

        for (int i = 0; i < words.length; i++) {
            wordsTrancuated[i] = words[i];
            Log.e("LENGTH", wordsTrancuated[i]);
        }*/
        //Transform input array of words to array of integers
        float[] input = new float[maxLenght]; // 1 sentence by maxLenWords
        //Make every position 0
        for (int l = 0; l < maxLenght; l++) {
            input[l] = 0;
        }

        String vocabJson = null;
        try {
            //Open .json file
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(vocabFilename)));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            vocabJson = sb.toString();
            br.close();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type type = new TypeToken<Map<String, Integer>>() {
            }.getType();
            //Create mapped vocabulary
            vocabMap = gson.fromJson(vocabJson, type);

            //////////////////////////////////////////////////////////////

            //Replace every desired position with numbers
            int j = 1;
            for (String word : wordsTrancuated) {

                if (j == maxLenght)
                    break;

                int index = 0;
                if (vocabMap.containsKey(word)) {
                    index = vocabMap.get(word);

                    //Making integer to float
                    input[input.length - wordsTrancuated.length + j] = index;
                    j++;
                } /*else {
                    Log.i("Not found","Word Not Found");
                    continue;
                }*/

            }

            for (int k = 0; k < input.length; k++) {
                Log.e("ArrayWords", String.valueOf(input[k]));
            }

            return input;

        } catch (Exception e) {
            Log.e("exception", e.toString());
            return new float[200];
        }
    }

    private void predict(float[] value) {
        /*float[] value = new float[200];

        for (int i = 0; i < 185; i++) {
            value[i] = 0;
        }
        value[185] = 11;
        value[186] = 18;
        value[187] = 68;
        value[188] = 1;
        value[189] = 117;
        value[190] = 113;
        value[191] = 2;
        value[192] = 1;
        value[193] = 410;
        value[194] = 14;
        value[195] = 37;
        value[196] = 50;
        value[197] = 10;
        value[198] = 445;
        value[199] = 8;*/
        Log.e("valueofPredict", String.valueOf(value[199]));

        tf.feed(INPUT_NAME, value, 1, 200);

        tf.run(new String[]{OUTPUT_NAME});

        //TF output
        float[] outputs = new float[1];

        tf.fetch(OUTPUT_NAME, outputs);

        Log.e("Output", "TF output: " + (outputs[0]));
    }

}
