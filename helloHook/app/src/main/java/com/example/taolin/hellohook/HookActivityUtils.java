package com.example.taolin.hellohook;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @FileName: com.example.taolin.hellohook
 * @Desription: 描述功能
 * @Anthor: taolin
 * @Date: 2019/1/22
 * @Version V2.0 <描述当前版本功能>
 */
public class HookActivityUtils {
    private static final String TAG = "HookActivityUtils";
    private volatile static HookActivityUtils sHookActivityUtils;
    public static HookActivityUtils getInstance(){
        if (sHookActivityUtils==null){
            synchronized (HookActivityUtils.class){
                if (sHookActivityUtils==null){
                    sHookActivityUtils = new HookActivityUtils();
                }
            }
        }
        return sHookActivityUtils;
    }
    private HookActivityUtils(){
    }
    public void hooks(Context mContext){
        Object object;
        try {
            Log.e(TAG,Build.VERSION.SDK_INT+"");

            //寻找hook点，最好是静态或者单例，不容易发生改变,因为是静态，所以传入null即可
            //因为版本差异，所以要分开处理
            if (Build.VERSION.SDK_INT>=26){
                Field iActivityManagerSingleton = ActivityManager.class.getDeclaredField("IActivityManagerSingleton");
                iActivityManagerSingleton.setAccessible(true);
                object = iActivityManagerSingleton.get(null);
            }else{
                Field gDefault = Class.forName("android.app.ActivityManagerNative").getDeclaredField("gDefault");
                gDefault.setAccessible(true);
                object = gDefault.get(null);
            }

            //获取单例对象,实现IActivityManager接口的实现类
            Field mFieldInstance = Class.forName("android.util.Singleton").getDeclaredField("mInstance");
            mFieldInstance.setAccessible(true);
            Object mInstance = mFieldInstance.get(object);
            //寻找到hook点后，新建一个代理对象
            ActivityManagerDelegate managerDelegate = new ActivityManagerDelegate(mInstance,mContext);
            Class<?> aClass = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(aClass.getClassLoader(), new Class<?>[]{aClass}, managerDelegate);
            //替换动态代理对象
            mFieldInstance.set(object,proxy);
        } catch (Exception mE) {
            mE.printStackTrace();
        }
    }


    public void hookHanlder(){
        try {
            Class<?> aClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = aClass.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            //ActivityThread 本身对象
            Object invoke = currentActivityThread.invoke(null);
            Field mH = aClass.getDeclaredField("mH");
            mH.setAccessible(true);
            //获取handler对象
            Object handler = mH.get(invoke);
            //获取handler中的mCallback
            Field mCallback = Handler.class.getDeclaredField("mCallback");
            mCallback.setAccessible(true);
            mCallback.set(handler,new HookCallBack((Handler) handler));
        } catch (Exception mE) {
            mE.printStackTrace();
        }

    }

}
