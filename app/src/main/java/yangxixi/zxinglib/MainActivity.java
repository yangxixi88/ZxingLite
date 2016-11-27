package yangxixi.zxinglib;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button defaultStart = (Button) findViewById(R.id.default_start);
        defaultStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DefaultCaptureActivity.class);
                startActivity(intent);
            }
        });

        Button weStart = (Button) findViewById(R.id.wechat_start);
        weStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WeChatCaptureActivity.class);
                startActivity(intent);
            }
        });

    }
}
