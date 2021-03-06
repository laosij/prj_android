package com.gy.utils.audio.mpdplayer.cover;

import android.content.SharedPreferences;

import com.gy.utils.audio.mpdplayer.mpd.AlbumInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

import static android.text.TextUtils.isEmpty;
import static android.util.Log.e;
import static android.util.Log.w;

public class GracenoteCover extends AbstractWebCover {

    private static String CLIENT_ID = "15942656-184B7C5BA04F0D8709F6E2A808C9ECD4";
    public static final String USER_ID = "GRACENOTE_USERID";
    public static final String CUSTOM_CLIENT_ID_KEY = "gracenoteClientId";
    private static final String API_URL = "https://c" + getClientIdPrefix() + ".web.cddbp.net/webapi/xml/1.0/";
    private SharedPreferences sharedPreferences;
    private String userId;
    public static final String URL_PREFIX = "web.content.cddbp.net";

    public GracenoteCover(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    private void initializeUserId() {
        try {

            if (sharedPreferences != null) {
                String customClientId = sharedPreferences.getString(CUSTOM_CLIENT_ID_KEY, null);
                if (!isEmpty(customClientId)) {
                    CLIENT_ID = customClientId;
                }
                userId = sharedPreferences.getString(USER_ID, null);
            }

            if (userId == null) {
                userId = register();
                if (sharedPreferences != null && userId != null) {
                    SharedPreferences.Editor editor;
                    editor = sharedPreferences.edit();
                    editor.putString(USER_ID, userId);
                    editor.commit();
                }
            }
        } catch (Exception e) {
            e(GracenoteCover.class.getName(), "Gracenote initialisation failure : " + e.getMessage());
            this.userId = null;
            if (sharedPreferences != null) {
                if (sharedPreferences != null && userId != null) {
                    SharedPreferences.Editor editor;
                    editor = sharedPreferences.edit();
                    editor.remove(USER_ID);
                    editor.commit();
                }
            }
        }

    }

    @Override
    public String[] getCoverUrl(AlbumInfo albumInfo) throws Exception {
        String coverUrl;

        if (userId == null) {
            initializeUserId();
        }

        if (userId == null) {
            return new String[0];
        }
        try {
            coverUrl = getCoverUrl(albumInfo.getArtist(), albumInfo.getAlbum());
            if (coverUrl != null) {
                return new String[]{coverUrl};
            }
        } catch (Exception ex) {
            e(GracenoteCover.class.getName(), "GracenoteCover fetch failure : " + ex);
        }
        return new String[0];
    }

    @Override
    public String getName() {
        return "Gracenote";
    }

    // Will register your clientID and Tag in order to get a userID. The userID should be stored
    // in a persistent form (filesystem, db, etc) otherwise you will hit your user limit.
    public String register() {
        return this.register(CLIENT_ID);
    }

    public String register(String clientID) {
        String request = "<QUERIES><QUERY Cmd=\"REGISTER\"><CLIENT>" + clientID + "</CLIENT></QUERY></QUERIES>";
        String response = this.executePostRequest(API_URL, request);
        return extractUserID(response);
    }

    public String getCoverUrl(String artist, String album) {
        // Make sure user doesn't try to register again if they already have a userID in the ctor.
        // Do the register request
        String request = "<QUERIES>\n" +
                "  <LANG>eng</LANG>\n" +
                "  <AUTH>\n" +
                "    <CLIENT>" + CLIENT_ID + "</CLIENT>\n" +
                "    <USER>" + userId + "</USER>\n" +
                "  </AUTH>\n" +
                "  <QUERY Cmd=\"ALBUM_SEARCH\">\n" +
                "    <MODE>SINGLE_BEST_COVER</MODE>\n" +
                "    <TEXT TYPE=\"ARTIST\">" + artist + "</TEXT>\n" +
                "    <TEXT TYPE=\"ALBUM_TITLE\">" + album + "</TEXT>\n" +
                "    <OPTION><PARAMETER>COVER_SIZE</PARAMETER><VALUE>LARGE</VALUE></OPTION>\n" +
                "  </QUERY>\n" +
                "</QUERIES>";

        String response = this.executePostRequest(API_URL, request);
        return extractCoverUrl(response);
    }

    private String extractUserID(String response) {

        String userId;
        String elementName = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(response));
            int eventType;
            eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    if (elementName != null && elementName.equals("USER")) {
                        userId = xpp.getText();
                        return userId;
                    }
                } else if (eventType == XmlPullParser.START_TAG) {
                    elementName = xpp.getName();
                }
                eventType = xpp.next();
            }
        } catch (Exception e)

        {
            e(GracenoteCover.class.getName(), "Cannot extract userID from Gracenote response : " + e);
        }

        return null;
    }

    private String extractCoverUrl(String response) {

        String coverUrl;
        String elementName = null;
        String attribute = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(response));
            int eventType;
            eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.TEXT) {
                    if (elementName != null && elementName.equals("URL")) {
                        if (attribute != null && attribute.equals("COVERART")) {
                            coverUrl = xpp.getText();
                            return coverUrl;
                        }
                    }
                } else if (eventType == XmlPullParser.START_TAG) {
                    elementName = xpp.getName();
                    attribute = xpp.getAttributeValue(null, "TYPE");
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e(GracenoteCover.class.getName(), "Cannot extract coverArt URL from Gracenote response : " + e);
        }
        return null;
    }

    private static String getClientIdPrefix() {
        String[] splittedString;
        splittedString = CLIENT_ID.split("-");
        if (splittedString.length == 2) {
            return splittedString[0];
        } else {
            w(GracenoteCover.class.getSimpleName(), "Invalid GraceNote User ID (must be XXXX-XXXXXXXXXXXXX) : " + CLIENT_ID);
            return "";
        }
    }
}

