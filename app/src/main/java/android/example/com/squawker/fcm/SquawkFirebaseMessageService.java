package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkDatabase;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class SquawkFirebaseMessageService extends FirebaseMessagingService {

    private static final String author = SquawkContract.COLUMN_AUTHOR;
    private static final String author_key = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String message = SquawkContract.COLUMN_MESSAGE;
    private static final String date = SquawkContract.COLUMN_DATE;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> receivedMsg = remoteMessage.getData();

        if (receivedMsg.size() > 0) {
            insertMsg(receivedMsg);
            sendNotification(receivedMsg);
        }
    }

    private void sendNotification(Map<String, String> receivedMsg) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String not_Author = receivedMsg.get(author);
        String not_Message = receivedMsg.get(message);

        if (not_Message.length() > 30) {
            not_Message = not_Message.substring(0, 30) + "\u2026";
        }

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder not_builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), not_Author))
                .setContentText(not_Message)
                .setAutoCancel(true)
                .setSound(defaultUri)
                .setContentIntent(pendingIntent);

        NotificationManager not_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        not_manager.notify(0, not_builder.build());

    }

    private void insertMsg(final Map<String, String> receivedMsg) {

        AsyncTask<Void, Void, Void> insertMsg = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues insertMessage = new ContentValues();

                insertMessage.put(SquawkContract.COLUMN_AUTHOR, receivedMsg.get(author));
                insertMessage.put(SquawkContract.COLUMN_AUTHOR_KEY, receivedMsg.get(author_key));
                insertMessage.put(SquawkContract.COLUMN_MESSAGE, receivedMsg.get(message));
                insertMessage.put(SquawkContract.COLUMN_DATE, receivedMsg.get(date));

                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, insertMessage);
                return null;
            }
        };

        insertMsg.execute();
    }


}
