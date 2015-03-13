package fm.jihua.smartlock;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/**
 * Created by sunwei on 3/9/15.
 */
public class UnLockServices extends Service {

    private IUnLock.Stub mBinder = new IUnLock.Stub() {
    };

    private static final int NOTIFY_ID = 0x1214;
    private static final String TAG = UnLockServices.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerInstallReceiver();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentText("检测到程序安装时会自动点亮屏幕");
        builder.setAutoCancel(false);
        builder.setSmallIcon(R.drawable.ic_stat_notif);
        builder.setContentTitle(getString(R.string.app_name));

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        startForeground(NOTIFY_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterInstallReceiver();
        Log.d(TAG, "service destroy");
    }

    private void registerInstallReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterInstallReceiver() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            KeyguardManager manager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock lock = manager.newKeyguardLock("unLock");
            lock.disableKeyguard();

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wakeLock.acquire();
            wakeLock.release();
        }
    };
}
