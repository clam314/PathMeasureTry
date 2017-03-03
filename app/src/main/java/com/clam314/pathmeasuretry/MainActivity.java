package com.clam314.pathmeasuretry;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private PolygonView poly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitity_main_sticky);
        SwipeRefreshLayout layout;
//        final PierreBezierCircleView circle3 = (PierreBezierCircleView)findViewById(R.id.circle3);
//        View btnstart = findViewById(R.id.btn_start);
//        btnstart.setOnClickListener(new View.OnClickListener() {
//            @Override public void onClick(View v) {
//                circle3.startAnimation();
//            }
//        });
//        poly = (PolygonView) findViewById(R.id.poly);
//
//        RadioGroup group = (RadioGroup) findViewById(R.id.group);
//        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (group.getCheckedRadioButtonId()){
//                    case R.id.point0: poly.setTestPoint(0); break;
//                    case R.id.point1: poly.setTestPoint(1); break;
//                    case R.id.point2: poly.setTestPoint(2); break;
//                    case R.id.point3: poly.setTestPoint(3); break;
//                    case R.id.point4: poly.setTestPoint(4); break;
//                }
//            }
//        });
    }
}
