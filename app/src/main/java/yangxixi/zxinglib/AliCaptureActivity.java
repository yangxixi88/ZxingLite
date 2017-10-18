package yangxixi.zxinglib;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.android.AnimeViewCallback;
import com.google.zxing.client.android.BaseCaptureActivity;
import com.google.zxing.client.android.FlowLineView;

public class AliCaptureActivity extends BaseCaptureActivity {

    private static final String TAG = "AliCaptureActivity";
    private SurfaceView surfaceView;
    private FlowLineView flowLineView;
    private boolean isPause = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ali_capture);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        flowLineView = (FlowLineView) findViewById(R.id.autoscanner_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        flowLineView.setCameraManager(cameraManager);
        if(isPause){
            flowLineView.Pause();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        flowLineView.Pause();
        isPause = true;
    }

    @Override
    public SurfaceView getSurfaceView() {
        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
    }

        @Override
    public AnimeViewCallback getViewfinderHolder() {
        return (flowLineView == null) ? (FlowLineView) findViewById(R.id.viewfinder_view) : flowLineView;
//            return null;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        Log.i(TAG, "dealDecode ~~~~~ " + rawResult.getText() + " " + barcode + " " + scaleFactor);
        playBeepSoundAndVibrate(true, false);
        Toast.makeText(this, rawResult.getText(), Toast.LENGTH_LONG).show();
//        对此次扫描结果不满意可以调用
//        reScan();
    }
}
