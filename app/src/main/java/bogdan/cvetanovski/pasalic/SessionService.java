package bogdan.cvetanovski.pasalic;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


public class SessionService extends Service {
    public SessionService() {
    }

    private Context context = this;

    // Handler that will be used to repeat the Runnable handling
    // the business logic, as well as update the UI from separate threads.
    private Handler handler;
    private static Runnable runnable;
    private boolean repeat = true;
    // Starting from Android Oreo, notifications are required to define a channel ID
    private final String channelId = "decideit";
    // Notification ID for the alert notification
    public static final int notificationId = 1;

    // How often the service should poll for expiring sessions
    private final int serviceDelay = 60000;

    // Binder subclass that the student activity will use to reach the service instance
    public class SessionBinder extends Binder {
        public SessionService getService() {
            return SessionService.this;
        }
    }

    // StudentViewActivity should call this as it connects and binds to the service.
    // This triggers the main polling mechanism that checks for expiring sessions.
    public void listenToSessions() {
        repeat = true;
        handler.postDelayed(runnable, serviceDelay);
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        // https://developer.android.com/develop/ui/views/notifications/build-notification#Priority
        CharSequence name = getString(R.string.NotificationName);
        String description = getString(R.string.NotificationDesc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /*
     * Notify the user about expiring sessions.
     * The Intent will open the current StudentViewActivity instance while passing
     * a new bundle. The activity will receive the new Intent instance and send the
     * user to the calendar fragment if fromNotification is bundled in the Intent and
     * set to true.
     */
    void notifyUser(String sessionList) {
        Intent notifIntent = new Intent(this, StudentViewActivity.class);
        Bundle params = new Bundle();
        params.putBoolean("fromNotification", true);
        notifIntent.putExtras(params);
        // Ensure that we don't spawn a new StudentViewActivity
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // Notification icon
                .setContentTitle(getString(R.string.NotificationTitle)) // Notification title
                .setContentText(sessionList) // Notification body
                .setContentIntent(intent) // This is what sends the user to the proper activity
                .setAutoCancel(true) // Clear notification after click
                .setOnlyAlertOnce(true); // Make the notification sound an alert only when it's created
        // Check notification permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // If the notification doesn't exist, calling this will create it and alert the user
        // Otherwise, the notification is simply updated. Since setOnlyAlertOnce is set to
        // true, calling this when the notification already exists won't sound an alert again.
        NotificationManagerCompat.from(this).notify(notificationId, builder.build());
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // Setup notification channel
        createNotificationChannel();
        handler = new Handler();
        runnable = () -> {
            // Prevents the runnable from repeating endlessly as the service is stopped
            if(repeat) {
                new Thread(() -> {
                    // Synchronize database first
                    DatabaseManager.getInstance(context).synchronizeDatabase();
                    try {
                        // Use SQLite to query sessions that are set to expire in less than x minutes
                        Session[] soonToEndSessions = (Session[])DatabaseFactory.getQueryResults(context, DatabaseManager.SESSIONS_TABLE,
                                "ROUND((JULIANDAY("+DatabaseManager.EndDate+") - JULIANDAY("+DatabaseManager.Date+")) * 1440) <= 15", null, null);

                        if(soonToEndSessions != null && soonToEndSessions.length > 0) {
                            handler.post(() -> {
                                // Form notification body
                                String notificationBody = "";
                                for(int i = 0; i < soonToEndSessions.length; i++) {
                                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                                    LocalDateTime sessionDateTime = LocalDateTime.parse(soonToEndSessions[i].getEndDate(), fmt);
                                    DateTimeFormatter readableFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                                    notificationBody += "• " + soonToEndSessions[i].getName() + " - " + sessionDateTime.format(readableFormat) + ((i == soonToEndSessions.length-1) ? "" : "\n");
                                }
                                notifyUser(notificationBody);
                            });
                        }
                    } catch (InvalidTableException e) {
                        throw new RuntimeException(e);
                    }
                    // Repeat this same runnable in serviceDelay milliseconds
                    handler.postDelayed(runnable, serviceDelay);
                }).start();
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SessionBinder();
    }
    /*
     * Bounded services stop once all the activities that were bound
     * to it have reached the end of their lifecycle and have unbounded
     * themselves from the service.
     * As this service is only used by the StudentViewActivity activity,
     * it's only active for as long as the currently loaded StudentViewActivity
     * is alive.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        repeat = false;
        // Cancel all notifications to prevent being sent to an activity with a possibly undefined login session
        NotificationManagerCompat.from(this).cancelAll();
        return super.onUnbind(intent);
    }
}