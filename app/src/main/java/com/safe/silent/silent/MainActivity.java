/*
 * Copyright (c) 2019. Parrot Faurecia Automotive S.A.S. All rights reserved.
 */

package com.safe.silent.silent;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.msc.MSC;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    //我操牛逼，启动安全模式，启动追踪模式

    TextView textView;

    TextView te_result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 将“12345678”替换成您申请的APPID，申请地址：http://www.xfyun.cn
        // 请勿在“=”与appid之间添加任何空字符或者转义符
        StringBuffer param = new StringBuffer();
        param.append("appid=5d1eb31b");
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());

        te_result = findViewById(R.id.te_result);
        textView = findViewById(R.id.te);
        VoiceWakeuper voiceWakeuper = VoiceWakeuper.createWakeuper(this, new InitListener() {
            @Override
            public void onInit(int i) {
                Log.e("---------","---onInit---");
            }
        });
        final String resPath = ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/5d1eb31b.jet");

        // 清空参数
        voiceWakeuper.setParameter(SpeechConstant.PARAMS, null);
        // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
        voiceWakeuper.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ 1000);
        // 设置唤醒模式
        voiceWakeuper.setParameter(SpeechConstant.IVW_SST, "wakeup");
        // 设置持续进行唤醒
        voiceWakeuper.setParameter(SpeechConstant.KEEP_ALIVE, "1");
        // 设置闭环优化网络模式
        voiceWakeuper.setParameter(SpeechConstant.IVW_NET_MODE, "0");
        // 设置唤醒资源路径
        voiceWakeuper.setParameter(SpeechConstant.IVW_RES_PATH, resPath);
        // 设置唤醒录音保存路径，保存最近一分钟的音频
        voiceWakeuper.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
        voiceWakeuper.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );
        voiceWakeuper.startListening(new WakeuperListener() {
            @Override
            public void onBeginOfSpeech() {
                textView.setText("begin speech.");
                Log.e("---------","---onBeginOfSpeech---");
            }

            @Override
            public void onResult(WakeuperResult wakeuperResult) {
                String resultString = null;
                try {
                    String text = wakeuperResult.getResultString();
                    JSONObject object;
                    object = new JSONObject(text);
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("【RAW】 "+text);
                    buffer.append("\n");
                    buffer.append("【操作类型】"+ object.optString("sst"));
                    buffer.append("\n");
                    buffer.append("【唤醒词id】"+ object.optString("id"));
                    buffer.append("\n");
                    buffer.append("【得分】" + object.optString("score"));
                    buffer.append("\n");
                    buffer.append("【前端点】" + object.optString("bos"));
                    buffer.append("\n");
                    buffer.append("【尾端点】" + object.optString("eos"));
                    resultString =buffer.toString();
                } catch (JSONException e) {
                    resultString = "结果解析出错";
                    e.printStackTrace();
                }
                te_result.setText("onResult--"+resultString);
                Log.e("---------","---onResult---" + wakeuperResult.getResultString());
            }

            @Override
            public void onError(SpeechError speechError) {
                textView.setText(speechError.getErrorCode() + "-onError--"+speechError.getErrorDescription()
                        +"---msg----"+speechError
                        .getMessage());
                Log.e("---------","---onError---");
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
                Log.e("---------","---onEvent---");
            }

            @Override
            public void onVolumeChanged(int i) {
                textView.setText("onVolumeChanged--");
                Log.e("---------","---onVolumeChanged---");
            }
        });

    }
}
