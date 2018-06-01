package com.example.sakura.websocketrxjava;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by sakura on 05/02/2018.
 * http://blog.takwolf.com/2017/06/29/android-application-foreground-and-background-switch-listener/index.html
 */

public abstract class GroundListener implements Application.ActivityLifecycleCallbacks {

    public abstract void onForeground(Activity activity);
    public abstract void onBackground(Activity activity);

    private int foregroundCount = 0; // 位于前台的 Activity 的数目
    private int bufferCount = 0; // 缓冲计数器，记录 configChanges 的状态

    @Override
    public void onActivityStarted(Activity activity) {
        if (foregroundCount <= 0) {
            // TODO 这里处理从后台恢复到前台的逻辑
            onForeground(activity);
        }
        if (bufferCount < 0) {
            bufferCount++;
        } else {
            foregroundCount++;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity.isChangingConfigurations()) { // 是 configChanges 的情况，操作缓冲计数器
            bufferCount--;
        } else {
            foregroundCount--;
            if (foregroundCount <= 0) {
                // TODO 这里处理从前台进入到后台的逻辑
                onBackground(activity);
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
