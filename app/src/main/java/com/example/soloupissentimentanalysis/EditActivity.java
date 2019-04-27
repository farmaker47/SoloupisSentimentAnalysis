package com.example.soloupissentimentanalysis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Vector;

public class EditActivity extends AppCompatActivity {

    public static final int ACTION_TAKE_PHOTO_B = 1;
    public static final int PICK_FROM_FILE = 3;
    public static Bitmap bitmap;
    private static Canvas canvas;
    public static int chose;
    private static Context context;
    public static boolean doOrNot = false;
    private static Vector<PointF> largestSq;
    private static Vector<PointF> myPath;
    private static PointF point1;
    private static PointF point2;
    private static PointF point3;
    private static PointF point4;
    private static PointF point5;
    private static PointF point6;
    private static PointF point7;
    private static PointF point8;
    private static ProgressDialog prog;
    private Vector<Bitmap> bitmapVec = new Vector();
    private Float bottom;


    private ImageButton btnLoadOrg;


    private float circRadius;
    private boolean cropFlag = false;
    private int deviceWidth;
    Display display;
    private double drawHeight = 0.0D;
    private double drawLeft = 0.0D;
    private double drawTop = 0.0D;

    private double drawWidth = 0.0D;
    private boolean flag = false;
    private double h = 0.0D;
    private double h1 = 0.0D;
    private double height;
    private int index = 0;
    private LinearLayout l;
    private Float left;
    RelativeLayout.LayoutParams lp;
    private String mCurrentPhotoPath = " ";
    private ImageView mImageView;
    DisplayMetrics metrics;
    private boolean notCropping;
    public boolean notException = true;
    private PointF old0;
    private PointF old2;
    private PointF old4;
    private PointF old6;
    private PointF origTouched;
    private Paint pC;
    private Paint paint;
    private Path path;
    private int picFrameDim = 15;
    private SharedPreferences prfs;
    private double rad = 0.0D;
    private Float right;
    private RelativeLayout root;
    private boolean showFlag = true;
    Point size = new Point();
    private boolean startFlag;
    private TypedArray styledAttributes;
    private Float top;
    private double w = 0.0D;
    private double w1 = 0.0D;
    private double width;
    private RelativeLayout dyn_layout_img;

    private Uri picUri;
    final int PIC_CROP = 2;

    private CropImageView mCropView;
    private ImageButton btnKeyStone;
    private ImageButton btnDone;
    private ImageButton btnRotateRight;
    private ImageButton btnShow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.crop_image);

        mImageView = findViewById(R.id.imgview);
        mCropView = findViewById(R.id.cropImageView);
        btnKeyStone = findViewById(R.id.btnKeyStone);
        btnDone = findViewById(R.id.btnDone);
        btnRotateRight = findViewById(R.id.btnRotateRight);
        btnShow = findViewById(R.id.btnShow);

        context = this;
        this.deviceWidth = this.size.x;
        /////////////


        dyn_layout_img = findViewById(R.id.dyn_layout_img);
        this.prfs = getSharedPreferences("prefs", 0);

        Intent localIntent = getIntent();
        chose = localIntent.getIntExtra("Choose", 1);
        Uri localUri;
        if (chose == 4) {
            localUri = localIntent.getParcelableExtra("imageUri");
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(localUri));

                Drawable d = new BitmapDrawable(getResources(), bitmap);
                mCropView.setImageDrawable(d);
                mCropView.setInitialFrameScale(1.00f);

                /* scaleToActualAspectRatio1(bitmap, false);*/
               /* mImageView.setImageDrawable(d);
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);*/
                ///////////////////
                /*Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    display.getSize(size);
                }
                int width = size.x;
                int height = size.y;


                Resources res = getResources();

                Bitmap overlay = Bitmap.createScaledBitmap(bitmap, width, height, false);
                Drawable d = new BitmapDrawable(getResources(),overlay);
                mImageView.setImageDrawable(d);*/

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (chose == ACTION_TAKE_PHOTO_B) {
            /*localUri = (Uri)localIntent.getParcelableExtra("CameraPath");*/
            this.mCurrentPhotoPath = localIntent.getExtras().getString("CameraPath");
            bitmap = BitmapFactory.decodeFile(this.mCurrentPhotoPath);

            Drawable d = new BitmapDrawable(getResources(), bitmap);
            mCropView.setImageDrawable(d);
            mCropView.setInitialFrameScale(1.00f);

            /*mImageView.setImageDrawable(d);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);*/


            ////////////////////////////////
            /*Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                display.getSize(size);
            }
            int width = size.x;
            int height = size.y;


            Resources res = getResources();

            Bitmap overlay = Bitmap.createScaledBitmap(bitmap, width, height, false);

            Drawable d = new BitmapDrawable(getResources(),overlay);
            mImageView.setImageDrawable(d);*/

        }


    }

    public void btnKeyStoneclick(View view) {
        mCropView.setImageBitmap(mCropView.getCroppedBitmap());
    }


    public void btnRotateRightclick(View view) {
        /////////////rotate right + miliseconds
        mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D, 1000);
    }

    public void btnDoneclick(View view) {
        mImageView.setImageBitmap(mCropView.getCroppedBitmap());

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        mImageView.setColorFilter(filter);

        Bitmap image = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        Bitmap grey = toGrayscale(image);

        saveImageFile(grey);
        /////////////////////////
        finish();
        /*mCropView.setVisibility(View.INVISIBLE);
        btnShow.setVisibility(View.VISIBLE);*/

    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }


    public void btnShowclick(View view) {
        /*mImageView.buildDrawingCache();
        spinner.setVisibility(View.VISIBLE);*/



        /*Intent croppedphoto = new Intent (EditActivity.this, Readyforocr.class);
        startActivity(croppedphoto);

        finish();*/



        /*Bundle extras = new Bundle();
        extras.putParcelable("imagebitmap", image);
        Intent croppedphoto = new Intent (EditActivity.this, Readyforocr.class);
        croppedphoto.putExtras(extras);
        startActivity(croppedphoto);*/

        ///////////////////////////

    }


    public String saveImageFile(Bitmap bitmap) {
        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + "Sentiment");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/"
                + "IMG_" + "cropped_bitmap" + ".jpg");
        return uriSting;
    }





    /*private void performCrop(){
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);

        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }*/






    /*private void scaleToActualAspectRatio1(Bitmap bitmap, boolean cropCheck) {
        if (bitmap != null) {
            float scale;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int bounding = dpToPx(this.deviceWidth);
            float xScale = ((float) bounding) / ((float) width);
            float yScale = ((float) bounding) / ((float) height);
            if (xScale <= yScale) {
                scale = xScale;
            } else {
                scale = yScale;
            }
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            BitmapDrawable result = new BitmapDrawable(Bitmap.createBitmap(EditActivity.bitmap, 0, 0, width, height, matrix, true));
            this.lp.setMargins(this.picFrameDim, 0, this.picFrameDim, 0);
            *//*runOnUiThread(new AnonymousClass11(cropCheck, result));*//*
            mImageView.setImageDrawable(result);
        }
    }*/


    /*private int dpToPx(int paramInt)
    {
        return Math.round(getApplicationContext().getResources().getDisplayMetrics().density * paramInt);
    }*/


}
