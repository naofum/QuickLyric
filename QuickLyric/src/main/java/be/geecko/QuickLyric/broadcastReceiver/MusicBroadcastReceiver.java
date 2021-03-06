package be.geecko.QuickLyric.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import be.geecko.QuickLyric.App;
import be.geecko.QuickLyric.fragment.LyricsViewFragment;

public class MusicBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /** Google Play Music
         //bool streaming				long position
         //long albumId					String album
         //bool currentSongLoaded		String track
         //long ListPosition			long ListSize
         //long id						bool playing
         //long duration				int previewPlayType
         //bool supportsRating			int domain
         //bool albumArtFromService		String artist
         //int rating					bool local
         //bool preparing				bool inErrorState
         */

        Bundle extras = intent.getExtras();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean lengthFilter = sharedPref.getBoolean("filter_20min", true);

        if (extras == null || extras.getInt("state") > 1 || (lengthFilter && (extras.get("duration") instanceof Long && extras.getLong("duration") > 1200000) || (extras.get("duration") instanceof Double && extras.getDouble("duration") > 1200000) || (extras.get("duration") instanceof Integer && extras.getInt("duration") > 1200))) //Tracks longer than 20min are presumably not songs
            return;

        String artist = extras.getString("artist");
        String track = extras.getString("track");

        if ((artist == null || "".equals(artist) || artist.contains("Unknown")) || (track == null || "".equals(track) || track.contains("Unknown"))) //Could be problematic
            return;

        SharedPreferences current = context.getSharedPreferences("current_music", Context.MODE_PRIVATE);

        String currentArtist = current.getString("artist", "Michael Jackson");
        String currentTrack = current.getString("track", "Bad");

        if (!currentArtist.equals(artist) || !currentTrack.equals(track)) {

            SharedPreferences.Editor editor = current.edit();
            editor.putString("artist", artist);
            editor.putString("track", track);
            editor.apply();

            if (App.isActivityVisible() && sharedPref.getBoolean("pref_auto_refresh", false)) {
                Intent internalIntent = new Intent("Broadcast");
                internalIntent.putExtra("artist", artist).putExtra("track", track);
                LyricsViewFragment.sendIntent(context, internalIntent);
            }
        }
    }
}