package com.faceunity.fulivedemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.faceunity.fulivedemo.core.FURenderer;
import com.faceunity.fulivedemo.entity.Effect;
import com.faceunity.fulivedemo.utils.MiscUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页面，区分了SDK各个功能，并且获取权限码验证证书是否能够使用该功能
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int[] home_function_type = {
            Effect.EFFECT_TYPE_NONE,
            Effect.EFFECT_TYPE_ANIMOJI,
            Effect.EFFECT_TYPE_NORMAL,
            Effect.EFFECT_TYPE_AR,
            Effect.EFFECT_TYPE_FACE_CHANGE,
            Effect.EFFECT_TYPE_EXPRESSION,
            Effect.EFFECT_TYPE_MUSIC_FILTER,
            Effect.EFFECT_TYPE_BACKGROUND,
            Effect.EFFECT_TYPE_GESTURE,
            Effect.EFFECT_TYPE_FACE_WARP,
//            Effect.EFFECT_TYPE_PORTRAIT_LIGHT,
            Effect.EFFECT_TYPE_PORTRAIT_DRIVE,
    };

    private static final int[] home_function_permissions_code = {
            0x1,                    //美颜
            0x10,                    //Animoji
            0x2 | 0x4,              //道具贴纸
            0x20 | 0x40,            //AR面具
            0x80,                   //换脸
            0x800,                  //表情识别
            0x20000,                //音乐滤镜
            0x100,                  //背景分割
            0x200,                  //手势识别
            0x10000,                //哈哈镜
//            0x4000,                 //人像光效
            0x8000                  //人像驱动
    };

    private static final String[] home_function_name = {
            "美颜",
            "Animoji",
            "道具贴纸",
            "AR面具",
            "换脸",
            "表情识别",
            "音乐滤镜",
            "背景分割",
            "手势识别",
            "哈哈镜",
//            "人像光效",
            "人像驱动"
    };

    private static final int[] home_function_res = {
            R.drawable.main_beauty_enable,
            R.drawable.main_avatar_enable,
            R.drawable.main_effect_enable,
            R.drawable.main_ar_mask_enable,
            R.drawable.main_change_face_enable,
            R.drawable.main_expression_enable,
            R.drawable.main_music_fiter_enable,
            R.drawable.main_background_enable,
            R.drawable.main_gesture_enable,
            R.drawable.main_face_warp_enable,
//            R.drawable.main_portrait_light_enable,
            R.drawable.main_portrait_drive_enable
    };

    private static final int[] home_function_res_unable = {
            R.drawable.main_beauty_unable,
            R.drawable.main_avatar_unable,
            R.drawable.main_effect_unable,
            R.drawable.main_ar_mask_unable,
            R.drawable.main_change_face_unable,
            R.drawable.main_expression_unable,
            R.drawable.main_music_fiter_unable,
            R.drawable.main_background_unable,
            R.drawable.main_gesture_unable,
            R.drawable.main_face_warp_unable,
//            R.drawable.main_portrait_light_unable,
            R.drawable.main_portrait_drive_unable
    };

    private List<Integer> hasFaceUnityPermissionsList = new ArrayList<>();
    private final boolean[] hasFaceUnityPermissions = new boolean[home_function_name.length];

    private RecyclerView mRecyclerView;
    private HomeRecyclerAdapter mHomeRecyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        fullScreen(this);
        MiscUtil.checkPermission(this);

        String version = FURenderer.getVersion();
        boolean isLite = version.contains("lite");
        int moduleCode = FURenderer.getModuleCode();
        Log.e(TAG, "ModuleCode " + moduleCode);
        int count = 0;
        for (int i = 0; i < home_function_name.length; i++) {
            hasFaceUnityPermissions[i] = moduleCode == 0 || (home_function_permissions_code[i] & moduleCode) > 0;
            if (isLite && (home_function_type[i] == Effect.EFFECT_TYPE_BACKGROUND || home_function_type[i] == Effect.EFFECT_TYPE_GESTURE)) {
                hasFaceUnityPermissions[i] = false;
            }
            if (hasFaceUnityPermissions[i]) {
                hasFaceUnityPermissionsList.add(count++, i);
            } else {
                hasFaceUnityPermissionsList.add(i);
            }
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.home_recycler);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(mHomeRecyclerAdapter = new HomeRecyclerAdapter());
    }

    class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.HomeRecyclerHolder> {

        @Override
        public HomeRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new HomeRecyclerHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_main_recycler, parent, false));
        }

        @Override
        public void onBindViewHolder(HomeRecyclerHolder holder, int pos) {
            final int position = hasFaceUnityPermissionsList.get(pos);

            holder.homeFunctionImg.setImageResource(hasFaceUnityPermissions[position] ? home_function_res[position] : home_function_res_unable[position]);
            holder.homeFunctionName.setTextColor(getResources().getColor(hasFaceUnityPermissions[position] ? R.color.main_color : R.color.main_color_gray));
            holder.homeFunctionName.setText(home_function_name[position]);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!hasFaceUnityPermissions[position]) {
                        Toast.makeText(MainActivity.this, "抱歉，你所使用的证书权限或SDK不包括该功能。", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent;
                    if (home_function_res[position] == R.drawable.main_beauty_enable) {
                        intent = new Intent(MainActivity.this, FUBeautyActivity.class);
                        startActivity(intent);
                    } else {
                        intent = new Intent(MainActivity.this, FUEffectActivity.class);
                        intent.putExtra("EffectType", home_function_type[position]);
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return hasFaceUnityPermissionsList.size();
        }

        class HomeRecyclerHolder extends RecyclerView.ViewHolder {

            ImageView homeFunctionImg;
            TextView homeFunctionName;

            public HomeRecyclerHolder(View itemView) {
                super(itemView);
                homeFunctionImg = (ImageView) itemView.findViewById(R.id.home_recycler_img);
                homeFunctionName = (TextView) itemView.findViewById(R.id.home_recycler_text);
            }
        }
    }

    private void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
//                attributes.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            window.setAttributes(attributes);
        }
    }
}
