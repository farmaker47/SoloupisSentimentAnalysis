package com.example.soloupissentimentanalysis;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.soloupissentimentanalysis.EditActivity.ACTION_TAKE_PHOTO_B;

public class MainActivity extends AppCompatActivity {

    private TensorFlowInferenceInterface tf;
    private static final String MODEL_ASSETS = "keras_sentiment.pb";
    private String INPUT_NAME = "embedding_2_input";
    private String OUTPUT_NAME = "output_1";
    private static final String vocabFilename = "result.json";

    public static final int POS_LABEL = 1;
    public static final int NEG_LABEL = 0;
    private static final int maxLenght = 200;

    private Map<String, Integer> vocabMap = null;
    private ArrayList<String> finalWords;
    private List<String> wordsTrancuated;

    //Camera field
    private static final int CAMERA_REQUEST = 1888;
    private static final int PICK_FROM_FILE = 4;

    private TextView resultView, resultText;
    private ImageView imageView, imageSentiment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultView = findViewById(R.id.textViewResult);
        imageView = findViewById(R.id.imageViewPhoto);
        resultText = findViewById(R.id.textViewText);
        imageSentiment = findViewById(R.id.imageSentiment);

        FloatingActionButton fabPhoto = findViewById(R.id.fabPhoto);
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        FloatingActionButton fabFiles = findViewById(R.id.fabFiles);
        fabFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFile();
            }
        });
        FloatingActionButton fabResults = findViewById(R.id.fabResults);
        fabResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCroppedImage();
            }
        });

        //Initialize inference
        tf = new TensorFlowInferenceInterface(getAssets(), MODEL_ASSETS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageView.setImageDrawable(getDrawable(R.drawable.ic_image_grey_80dp));
        resultView.setText("");
        resultText.setText("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("restart", "restart");
        imageSentiment.setImageDrawable(null);
        getCroppedImage();
    }

    public void getCroppedImage() {
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + "Sentiment");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/"
                + "IMG_" + "cropped_bitmap" + ".jpg");

        Bitmap croppedBitmap = BitmapFactory.decodeFile(uriSting);

        if (croppedBitmap != null) {
            setImageAndPredict(croppedBitmap);
        }

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

        //Replace Upper case letters,rmeove panctuation and split string
        String[] words = textToFormat.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        //Initialize an input array with maxSize length
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
            //Find words that exist in vocabulary
            int p = 0;
            finalWords = new ArrayList<>();
            for (String word : words) {
                if (vocabMap.containsKey(word)) {
                    finalWords.add(word);
                    p++;
                }
            }
            Log.i("LENGTH", String.valueOf(finalWords.size()));
            //////////////////////////////////////////////////////////////
            //Trancuate
            if (finalWords.size() >= 200) {
                wordsTrancuated = finalWords.subList(0, 200);
                Log.i("LENGTH", "BIGGER");
            } else {
                wordsTrancuated = finalWords.subList(0, finalWords.size());
                Log.i("LENGTH", "SMALLER");
            }
            /////////////////////////////////////////////////////////////

            ///////////////////////////////////////////////////
            //Padding sequence of maxSize length with integers of final words
            int j = 0;
            for (String word : wordsTrancuated) {

                if (j == maxLenght)
                    break;

                int index = 0;
                if (vocabMap.containsKey(word)) {
                    index = vocabMap.get(word);

                    //Making integer to float
                    input[input.length - wordsTrancuated.size() + j] = index;
                    j++;
                }

            }
            /////////////////////////////////////////////////////

            //Check all input array
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
        Log.d("valueofPredict", String.valueOf(value[199]));

        tf.feed(INPUT_NAME, value, 1, 200);

        tf.run(new String[]{OUTPUT_NAME});

        //TF output
        float[] outputs = new float[1];

        tf.fetch(OUTPUT_NAME, outputs);

        Log.d("Output", "TF output: " + (outputs[0]));
        resultView.setText(String.valueOf(outputs[0]));
        ///Update sentiment ImageView
        if (outputs[0] >= 0.5) {
            imageSentiment.setImageDrawable(getDrawable(R.drawable.ic_thumb_up_black_24dp));
        } else {
            imageSentiment.setImageDrawable(getDrawable(R.drawable.ic_thumb_down_black_24dp));
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            /*AssetManager assetManager = getAssets();
            InputStream istr = null;
            try {
                istr = assetManager.open("maySay.jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bitmapMay = BitmapFactory.decodeStream(istr);*/
            /*setImageAndPredict(picture);*/

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            Intent nextScreen = new Intent(getApplicationContext(), EditActivity.class);
            nextScreen.putExtra("CameraPath", currentPhotoPath);
            nextScreen.putExtra("Choose", ACTION_TAKE_PHOTO_B);
            startActivity(nextScreen);

        } else if (requestCode == PICK_FROM_FILE) {
            Uri myUri = data.getData();
            Intent nextScreen = new Intent(this, EditActivity.class);
            nextScreen.putExtra("imageUri", myUri);
            nextScreen.putExtra("Choose", PICK_FROM_FILE);
            startActivity(nextScreen);
        }
    }

    private void selectFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Load Image"), PICK_FROM_FILE);
    }

    private void setImageAndPredict(final Bitmap bitmap) {
        //Proceed to recognise text
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        //pass the image to the detector
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...

                                imageView.setImageBitmap(bitmap);
                                Log.i("RECOG", firebaseVisionText.getText());
                                resultText.setText(firebaseVisionText.getText());
                                float[] value = transformText(firebaseVisionText.getText());
                                //make prediction
                                predict(value);

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

}
