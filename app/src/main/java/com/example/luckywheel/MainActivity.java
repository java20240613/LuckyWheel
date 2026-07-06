package com.example.luckywheel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView tvResult;
    EditText etInput, etWeightInput; // 新增 etWeightInput 變數
    Button btnAdd, btnSpin;

    ArrayList<Option> items = new ArrayList<>();
    RecyclerView recyclerView;
    OptionAdapter adapter;
    WheelView wheelView;
    private boolean isSpinning = false;

    private static final String PREFS_NAME = "LuckyWheelPrefs";
    private static final String KEY_OPTIONS = "option_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tvResult);
        etInput = findViewById(R.id.etInput);
        etWeightInput = findViewById(R.id.etWeightInput); // 綁定元件
        btnAdd = findViewById(R.id.btnAdd);
        btnSpin = findViewById(R.id.btnSpin);
        wheelView = findViewById(R.id.wheelView);

        // 🎯 優先從本地存檔載入，若無存檔則載入預設資料
        loadOptionsFromPrefs();
        wheelView.setItems(items);

        // 新增選項
        btnAdd.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();
            String weightStr = etWeightInput.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "請輸入選項名稱", Toast.LENGTH_SHORT).show();
                return;
            }

            int weight = 10; // 預設權重
            if (!weightStr.isEmpty()) {
                try {
                    weight = Integer.parseInt(weightStr);
                    if (weight <= 0) {
                        Toast.makeText(this, "權重必須大於 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "請輸入有效的數字", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            items.add(new Option(input, weight));
            adapter.notifyDataSetChanged();
            wheelView.setItems(items);

            // 🎯 儲存更新後的資料到 SharedPreferences
            saveOptionsToPrefs();

            etInput.setText("");
            etWeightInput.setText(""); // 清空輸入框
            tvResult.setText("已加入：" + input + " (權重:" + weight + ")");
        });

        // 抽籤
        btnSpin.setOnClickListener(v -> {
            if (items.isEmpty() || isSpinning) return;

            isSpinning = true;
            btnSpin.setEnabled(false);
            btnAdd.setEnabled(false);

            // 1. 計算總權重
            int totalWeight = 0;
            for (Option option : items) {
                totalWeight += option.getWeight();
            }

            if (totalWeight <= 0) {
                isSpinning = false;
                btnSpin.setEnabled(true);
                btnAdd.setEnabled(true);
                return;
            }

            // 2. 隨機決定落點權重值
            int random = new Random().nextInt(totalWeight);
            int current = 0;
            int index = 0;

            for (int i = 0; i < items.size(); i++) {
                current += items.get(i).getWeight();
                if (random < current) {
                    index = i;
                    break;
                }
            }

            // 3. 計算目標項目在非等分轉盤上的物理角度中心
            float accumulatedAngle = 0;
            for (int i = 0; i < index; i++) {
                accumulatedAngle += (items.get(i).getWeight() / (float) totalWeight) * 360f;
            }
            float targetSweepAngle = (items.get(index).getWeight() / (float) totalWeight) * 360f;
            float sliceCenterAngle = accumulatedAngle + (targetSweepAngle / 2f);

            // 4. 動態計算動畫旋轉角度（確保每次都是順時針推進）
            float currentRotation = wheelView.getRotation();
            float baseRotation = currentRotation - (currentRotation % 360f);

            float extraRotation = 270f - sliceCenterAngle;
            if (extraRotation < 0) {
                extraRotation += 360f;
            }

            float targetAngle = baseRotation + (360f * 5) + extraRotation;

            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    wheelView,
                    "rotation",
                    currentRotation,
                    targetAngle
            );

            animator.setDuration(4000);
            animator.setInterpolator(new DecelerateInterpolator());

            final int finalIndex = index; // 供 Animator 內部監聽使用
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isSpinning = false;
                    btnSpin.setEnabled(true);
                    btnAdd.setEnabled(true);

                    tvResult.setText("結果：" + items.get(finalIndex).getName());

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(100);
                    }
                }
            });

            animator.start();
        });

        recyclerView = findViewById(R.id.recyclerView);

        // 點擊刪除處理
        adapter = new OptionAdapter(items, position -> {
            if (isSpinning) return;

            String removedItem = items.get(position).getName();
            items.remove(position);

            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, items.size());

            wheelView.setItems(items);

            // 🎯 刪除後更新本地存檔
            saveOptionsToPrefs();

            tvResult.setText("已刪除：" + removedItem);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // 🎯 儲存至 SharedPreferences (使用 JSON 格式)
    private void saveOptionsToPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (Option option : items) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", option.getName());
                jsonObject.put("weight", option.getWeight());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString(KEY_OPTIONS, jsonArray.toString());
        editor.apply();
    }

    // 🎯 自 SharedPreferences 讀取資料
    private void loadOptionsFromPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(KEY_OPTIONS, null);

        items.clear();
        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String name = jsonObject.getString("name");
                    int weight = jsonObject.getInt("weight");
                    items.add(new Option(name, weight));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 🎯 若初次開啟無存檔，則載入預設資料
        if (items.isEmpty()) {
            items.add(new Option("吃火鍋", 50));
            items.add(new Option("打遊戲", 30));
            items.add(new Option("睡覺", 20));
        }
    }
}