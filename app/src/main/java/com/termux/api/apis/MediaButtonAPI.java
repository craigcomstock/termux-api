package com.termux.api.apis;

import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import android.media.MediaMetadata;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MediaButtonAPI {

    public static void onReceive(TermuxApiReceiver receiver, Context context, Intent intent) {
        ResultReturner.returnData(receiver, intent, new ResultReturner.WithInput() {
    @Override
    public void writeResult(PrintWriter out) throws Exception {
        BlockingQueue<String> q = new LinkedBlockingQueue<>();
        MediaSession s = new MediaSession(context, "termux-media-button");
        s.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent i) {
                KeyEvent e = i.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (e != null && e.getAction() == KeyEvent.ACTION_DOWN)
                    q.add(KeyEvent.keyCodeToString(e.getKeyCode()));
                return true;
            }
        }, new Handler(Looper.getMainLooper()));
        setState(s, true, 0);
        s.setActive(true);

        // stdin: "META\ttitle\tartist\talbum\tduration_ms" or "STATE\tplaying|paused\tpos_ms"
        Thread reader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] p = line.split("\t", -1);
                    if (p[0].equals("META") && p.length >= 5) {
                        s.setMetadata(new MediaMetadata.Builder()
                            .putString(MediaMetadata.METADATA_KEY_TITLE, p[1])
                            .putString(MediaMetadata.METADATA_KEY_ARTIST, p[2])
                            .putString(MediaMetadata.METADATA_KEY_ALBUM, p[3])
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(p[4]))
                            .build());
                    } else if (p[0].equals("STATE") && p.length >= 3) {
                        setState(s, p[1].equals("playing"), Long.parseLong(p[2]));
                    }
                }
            } catch (Exception ignored) {}
        });
        reader.setDaemon(true);
        reader.start();

        try {
            while (true) {
                out.println(q.take());
                out.flush();
                if (out.checkError()) break;
            }
        } finally {
            s.release();
        }
    }

    private void setState(MediaSession s, boolean playing, long pos) {
        s.setPlaybackState(new PlaybackState.Builder()
            .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE
                | PlaybackState.ACTION_PLAY_PAUSE)
            .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                pos, playing ? 1f : 0f).build());
    }
});
    }
}
