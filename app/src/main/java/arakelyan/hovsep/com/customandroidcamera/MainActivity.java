package arakelyan.hovsep.com.customandroidcamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.img)
    ImageView imageView;

    @BindView(R.id.open_camera)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        button.setOnClickListener(this);

    }

    public void requestForCameraPermission() {
        final String permission = Manifest.permission.CAMERA;
        final String permissionR = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String permissionW = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                showPermissionRationaleDialog("Test", permission, permissionR, permissionW);
            } else {
                requestForPermission(permission, permissionR, permissionW);
            }
        } else {
            launch();
        }
    }

    private void showPermissionRationaleDialog(final String message, final String permission, final String permissionR, final String permissionW) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.requestForPermission(permission, permissionR, permissionW);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void requestForPermission(final String permission, final String permissionR, final String permissionW) {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission, permissionR, permissionW}, 1);
    }

    private void launch() {
        startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.open_camera:
                requestForCameraPermission();
                break;
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Const.update:
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(data.getStringExtra(Const.bitmapImage), options);

                imageView.setImageBitmap(bitmap);
                break;
        }
    }
}
