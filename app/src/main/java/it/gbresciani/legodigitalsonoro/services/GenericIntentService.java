package it.gbresciani.legodigitalsonoro.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;
import java.util.concurrent.TimeUnit;

import it.gbresciani.legodigitalsonoro.R;
import it.gbresciani.legodigitalsonoro.helper.GmailSender;
import it.gbresciani.legodigitalsonoro.model.GameStat;
import it.gbresciani.legodigitalsonoro.model.WordStat;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class GenericIntentService extends IntentService {

    private static final String SEND_ONE_GAME_STAT = "it.gbresciani.legodigitalsonoro.services.action.SEND_ONE_GAME_STAT";

    private static final String GAME_STAT_ID = "it.gbresciani.legodigitalsonoro.services.extra.GAME_STAT_ID";

    private static final String senderEmail = "legosound2@gmail.com";
    private static final String password = "giocosolegosound2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void sendOneGameStat(Context context, Long gameStatId) {
        Intent intent = new Intent(context, GenericIntentService.class);
        intent.setAction(SEND_ONE_GAME_STAT);
        intent.putExtra(GAME_STAT_ID, gameStatId);
        context.startService(intent);
    }

    public GenericIntentService() {
        super("GenericIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (SEND_ONE_GAME_STAT.equals(action)) {
                final long gameStatId = intent.getLongExtra(GAME_STAT_ID, 0);
                handleActionFoo(gameStatId);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(long gameStatId) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String email = pref.getString(getString(R.string.setting_email_key, ""), "");

        GameStat gameStat = GameStat.findById(GameStat.class, gameStatId);
        List<WordStat> wordStats = Select.from(WordStat.class).where(Condition.prop("game_stat").eq(gameStatId)).list();
        long gameTime = gameStat.getEndDate().getTime() - gameStat.getStartDate().getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(gameTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(gameTime);
        long relativeSeconds = seconds % 60;
        String subject = "Partita " + String.valueOf(gameStatId);
        String body;
        body = "Dispositivo 1: " + gameStat.getDeviceId1() + "\n";
        if (gameStat.getDeviceId2() != null) {
            body += "Dispositivo 2: " + gameStat.getDeviceId2() + "\n";
        }
        body += "Tempo totale: " + String.format("%02d", minutes) + ":" + String.format("%02d", relativeSeconds) + "\n";
        for (WordStat ws : wordStats) {
            long wordTime = ws.getFoundDate().getTime() - gameStat.getStartDate().getTime();
            long wordSeconds = TimeUnit.MILLISECONDS.toSeconds(wordTime);
            long wordMinutes = TimeUnit.MILLISECONDS.toMinutes(wordTime);
            long wordRelativeSeconds = wordSeconds % 60;
            String device;
            if (ws.getDeviceId() != null) {
                device = (ws.getDeviceId().equals(gameStat.getDeviceId1())) ? "1" : "2";
            } else {
                device = "1";
            }
            body += device + " - " + ws.getWord() + ": " + String.format("%02d", wordMinutes) + ":" + String.format("%02d", wordRelativeSeconds) + " (" + String.valueOf(ws.getPageNumber()) + ")" + "\n";
        }

        try {
            GmailSender sender = new GmailSender(senderEmail, password);
            sender.sendMail(subject, body, senderEmail, email);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
