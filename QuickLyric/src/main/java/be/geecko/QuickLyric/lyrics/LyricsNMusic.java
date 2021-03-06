package be.geecko.QuickLyric.lyrics;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;

import static be.geecko.QuickLyric.utils.Net.getUrlAsString;

@SuppressLint("NewApi")
public class LyricsNMusic {
    public static ArrayList<Lyrics> search(String query) {
        ArrayList<Lyrics> results = new ArrayList<>();
        try {
            query = Normalizer.normalize(query, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            URL queryURL = new URL(String.format("http://api.lyricsnmusic.com/songs?api_key=%s&q=%s", "e35aa198cf1a370a90f27be98ec2d4", URLEncoder.encode(query, "UTF-8")));
            JSONArray response = new JSONArray(getUrlAsString(queryURL));

            int processed = 0;
            while (processed < response.length()) {
                JSONObject song = response.getJSONObject(processed);
                if (song.getBoolean("viewable") && (song.isNull("instrumental") || !song.getBoolean("instrumental"))) {
                    String artist = song.getJSONObject("artist").getString("name");
                    String title = song.getString("title");
                    String url = song.getString("url");
                    Lyrics l = new Lyrics(Lyrics.SEARCH_ITEM);
                    l.setArtist(artist);
                    l.setTitle(title);
                    l.setURL(url);
                    l.setSource("LyricsNMusic");
                    results.add(l);
                }
                processed++;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }

    public static Lyrics fromURL(String url, String artist, String track) {
        try {
            String html = getUrlAsString(url);
            if (artist == null || track == null) {
                String endTag = " property='og:title' />";
                String startTag = "<meta content='";
                String cut = html.substring(0, html.indexOf(endTag));
                cut = cut.substring(cut.lastIndexOf(startTag));
                String[] metaData = cut
                        .substring(startTag.length())
                        .split(" - ", 2);
                artist = metaData[0].replaceAll("\\[.*\\]", "");
                track = metaData[1].replaceAll("\\[.*\\]", "");
            }
            String startTag = "<pre itemprop='description'>";
            if (!html.contains(startTag)) {
                Lyrics result = new Lyrics(Lyrics.NEGATIVE_RESULT);
                result.setTitle(track);
                result.setArtist(artist);
                return result;
            }
            String preceding = html.substring(html.indexOf(startTag));
            String text = preceding.substring(startTag.length(), preceding.indexOf("</pre>"))
                    .replace("\n", "<br />");
            Lyrics lyrics = new Lyrics(Lyrics.POSITIVE_RESULT);
            lyrics.setArtist(artist);
            lyrics.setTitle(track);
            lyrics.setText(text);
            lyrics.setURL(url);
            lyrics.setSource("LyricsNMusic");
            return lyrics;
        } catch (IOException e) {
            e.printStackTrace();
            return new Lyrics(Lyrics.ERROR);
        }
    }

    public static Lyrics fromMetaData(String artist, String track) {
        try {
            artist = Normalizer.normalize(artist, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            track = Normalizer.normalize(track, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            URL queryURL = new URL(String.format("http://api.lyricsnmusic.com/songs?api_key=%s&artist=%s&track=%s", "e35aa198cf1a370a90f27be98ec2d4", URLEncoder.encode(artist, "UTF-8"), URLEncoder.encode(track, "UTF-8")));
            JSONArray response = new JSONArray(getUrlAsString(queryURL));
            JSONObject song = response.getJSONObject(0);
            if (song.getBoolean("viewable") && (song.isNull("instrumental") || !song.getBoolean("instrumental")) && track.equals(song.getString("title"))) {
                String url = song.getString("url");
                return LyricsNMusic.fromURL(url, artist, track);
            } else
                return new Lyrics(Lyrics.NEGATIVE_RESULT);
        } catch (JSONException | IOException e1) {
            e1.printStackTrace();
        }
        return new Lyrics(Lyrics.ERROR);
    }
}