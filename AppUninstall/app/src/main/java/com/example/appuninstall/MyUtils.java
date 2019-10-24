package com.example.appuninstall;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyUtils {
    public static final  String ACTION_INSTALL_COMPLETE = "cm.android.intent.action.INSTALL_COMPLETE";

    public static boolean installPackage(Context context, InputStream in, String packageName)
            throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        OutputStream out = session.openWrite("COSU", 0, -1);
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createIntentSender(context, sessionId));
        return true;
    }


    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }

    public static void uninstallApp(Activity activity) {
        String appPackage = "com.example.appuninstall";
        Intent intent = new Intent(activity,activity.getClass());
        PendingIntent sender = PendingIntent.getActivity(activity, 0, intent, 0);
        PackageInstaller mPackageInstaller = activity.getPackageManager().getPackageInstaller();
        mPackageInstaller.uninstall(appPackage, sender.getIntentSender());
    }
}
