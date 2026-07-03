package com.example.luckywheel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;
public class MainActivity extends AppCompatActivity {

    TextView tvResult;
    EditText etInput;
    Button btnAdd, btnSpin;

    ArrayList<String> items = new ArrayList<>();
    RecyclerView recyclerView;
    OptionAdapter adapter;
    WheelView wheelView;
    private boolean isSpinning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tvResult);
        etInput = findViewById(R.id.etInput);
        btnAdd = findViewById(R.id.btnAdd);
        btnSpin = findViewById(R.id.btnSpin);
        wheelView = findViewById(R.id.wheelView);
        // 預設資料
        items.add("吃火鍋");
        items.add("打遊戲");
        items.add("睡覺");
        wheelView.setItems(items);

        // 新增選項
        btnAdd.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();

            if (!input.isEmpty()) {
                items.add(input);
                adapter.notifyDataSetChanged();
                wheelView.setItems(items);
                etInput.setText("");
                tvResult.setText("已加入：" + input);
            }
        });

        // 抽籤

        btnSpin.setOnClickListener(v -> {
            if (items.isEmpty() || isSpinning) return;

            isSpinning = true;
            btnSpin.setEnabled(false); // 旋轉時禁用按鈕
            btnAdd.setEnabled(false);  // 旋轉時禁用新增，避免修改資料導致畫面出錯

            int index = new Random().nextInt(items.size());
            float anglePerItem = 360f / items.size();

            // 1. 取得目前轉盤的角度
            float currentRotation = wheelView.getRotation();

            // 2. 計算基礎旋轉度數（去除不滿一圈的零頭，確保每次都是順時針往前轉）
            float baseRotation = currentRotation - (currentRotation % 360f);

            // 3. 計算目標項目的中心點角度
            float sliceCenterAngle = (index * anglePerItem) + (anglePerItem / 2f);

            // 4. 計算為了讓中心點停在正上方（270度），需要額外旋轉的角度
            float extraRotation = 270f - sliceCenterAngle;
            if (extraRotation < 0) {
                extraRotation += 360f; // 確保角度為正數
            }

            // 5. 最終目標角度 = 基礎角度 + 轉 5 圈 + 補足指針位置的差額
            float targetAngle = baseRotation + (360f * 5) + extraRotation;

            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    wheelView,
                    "rotation",
                    currentRotation,
                    targetAngle
            );

            animator.setDuration(4000); // 建議 3~4 秒，體驗較為流暢自然
            animator.setInterpolator(new DecelerateInterpolator());

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isSpinning = false;
                    btnSpin.setEnabled(true);
                    btnAdd.setEnabled(true);

                    tvResult.setText("結果：" + items.get(index));

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(100);
                    }
                }
            });

            animator.start();
        });
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new OptionAdapter(items, position -> {
            if (isSpinning) return; // 旋轉中不允許刪除

            String removedItem = items.get(position);
            items.remove(position);

            // 通知 Adapter 資料改變
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, items.size());

            // 同步更新轉盤
            wheelView.setItems(items);
            tvResult.setText("已刪除：" + removedItem);
        });        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}