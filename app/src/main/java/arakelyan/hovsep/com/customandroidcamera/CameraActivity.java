package arakelyan.hovsep.com.customandroidcamera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity implements
        View.OnClickListener, SurfaceHolder.Callback,
        Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback {


    private boolean isOpenFlesh = true;
    private Camera.Parameters param;

    @BindView(R.id.doneTV)
    TextView done;

    /*new Camera*/
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    @BindView(R.id.SurfaceView01)
    SurfaceView preview;

    @BindView(R.id.camera_btn)
    ImageView shotBtn;

    @BindView(R.id.flesh_relative_layout)
    RelativeLayout fleshRelativeLayout;

    @BindView(R.id.current_scan_img)
     ImageView currentImg;

    private Bitmap bmp;

    @BindView(R.id.flesh_imageView)
    ImageView fleshImageView;

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        ButterKnife.bind(this);

        getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        shotBtn.setOnClickListener(this);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            fleshRelativeLayout.setOnClickListener(this);
        } else {
            fleshImageView.setAlpha(0.3f);
        }

        initToolBar();
        initView();


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {

            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;

        ViewGroup.LayoutParams lp = preview.getLayoutParams();


        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(90);
        } else {
            camera.setDisplayOrientation(0);
        }

        preview.setLayoutParams(lp);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];

        bmp = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
        bmp = rotateBitmap(bmp, 90);
        saveToInternalStorage(bmp);
        currentImg.setImageBitmap(bmp);
        paramCamera.startPreview();
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/CameraAndroid");
        myDir.mkdirs();
        String id = "CurrentImg";
        String fname = id + ".jpg";
        File file = new File(myDir, fname);
        setResult(Const.update,new Intent().putExtra(Const.bitmapImage,file.toString()) );
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
        shotBtn.setAlpha(1f);
        if (paramBoolean) {

            Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            paramCamera.takePicture(null, null, this);
        }
    }



    @Override
    public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera) {
        // здесь можно обрабатывать изображение, показываемое в preview
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
        param = camera.getParameters();

        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();

        bestSize = sizeList.get(0);
        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
            /*if (sizeList.get(i).height > bestSize.height) {
                bestSize = sizeList.get(i);
            }*/
        }

        List<Integer> supportedPreviewFormats = param.getSupportedPreviewFormats();
        Iterator<Integer> supportedPreviewFormatsIterator = supportedPreviewFormats.iterator();
        while (supportedPreviewFormatsIterator.hasNext()) {
            Integer previewFormat = supportedPreviewFormatsIterator.next();
            if (previewFormat == ImageFormat.YV12) {
                param.setPreviewFormat(previewFormat);
            }

        }

        param.setPreviewSize(bestSize.width, bestSize.height);

        param.setPictureSize(bestSize.width, bestSize.height);
        // param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// to do
        try {
            camera.setParameters(param);
            isOpenFlesh = true;

        } catch (Exception e) {
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

    }


    private void initView() {
        currentImg.setOnClickListener(this);
        done.setOnClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }


    private void initToolBar() {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }



    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        isOpenFlesh = false;
        chekFlesh();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.flesh_relative_layout:
                chekFlesh();
                break;

            case R.id.camera_btn:
                view.setAlpha(0.5f);
                try {
                    camera.autoFocus(this);

                } catch (Exception e) {
                    Toast.makeText(this, "AutoFocus failed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.doneTV:
                finish();
                break;

            case R.id.current_scan_img:

                break;

        }

    }

    private void chekFlesh() {

        if (isOpenFlesh) {
            fleshImageView.setColorFilter(Color.YELLOW);
            param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            isOpenFlesh = false;

        } else {
            fleshImageView.setColorFilter(Color.WHITE);
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            isOpenFlesh = true;
        }

        try {
            camera.setParameters(param);

        } catch (Exception e) {
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bmp != null) bmp.recycle();
    }
}
