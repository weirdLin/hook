package com.example.taolin.hellohook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @FileName: com.example.taolin.hellohook
 * @Desription: 描述功能
 * @Anthor: taolin
 * @Date: 2019/1/22
 * @Version V2.0 <描述当前版本功能>
 */
public class HookCallBack implements Handler.Callback {
    private static final String TAG = "HookCallBack";
    private Handler mHandler;

    public HookCallBack(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what==100){
            handleHookMsg(msg);
        }
        mHandler.handleMessage(msg);
        return false;
    }

    private void handleHookMsg(Message mMsg) {
        Object obj = mMsg.obj;
        try {
            Field intent = obj.getClass().getDeclaredField("intent");
            //这时候拿出之前存进来真正的intent
            intent.setAccessible(true);
            Intent proxyIntent = (Intent) intent.get(obj);
            Intent realIntent = proxyIntent.getParcelableExtra("realObj");
            proxyIntent.setComponent(realIntent.getComponent());
        } catch (Exception mE) {
            mE.printStackTrace();
        }
    }
}
