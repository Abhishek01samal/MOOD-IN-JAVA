package com.moodtune.service;

import com.moodtune.db.DatabaseConfig;
import com.moodtune.model.Recommendation;

import java.sql.*;
import java.util.*;

/**
 * RecommendationService
 * ─────────────────────
 * • Tries MySQL first (real mode).
 * • Falls back to hardcoded demo data if MySQL is unavailable.
 */
public class RecommendationService {

    // ── Demo-mode data ──────────────────────────────────────────────────────
    // mood → list of Recommendation objects
    private static final Map<String, List<Recommendation>> DEMO_DATA = new HashMap<>();

    static {
        // happy
        DEMO_DATA.put("happy", Arrays.asList(
            new Recommendation(1,"happy","music","Happy – Pharrell Williams","","https://open.spotify.com/track/60nZcImufyMA1MKQY3dcCO",9.1,"Spotify"),
            new Recommendation(2,"happy","movie","The Secret Life of Walter Mitty","","https://www.imdb.com/title/tt0359950/",8.7,"IMDb"),
            new Recommendation(3,"happy","anime","Spy x Family","","https://myanimelist.net/anime/50265",9.2,"Crunchyroll"),
            new Recommendation(4,"happy","book","The Hitchhiker's Guide to the Galaxy","","https://www.goodreads.com/book/show/11.The_Hitchhiker_s_Guide_to_the_Galaxy",9.5,"Goodreads"),
            new Recommendation(5,"happy","game","Stardew Valley","","https://store.steampowered.com/app/413150/",9.4,"Steam"),
            new Recommendation(6,"happy","podcast","The Happiness Lab","","https://www.happinesslab.fm/",8.8,"Pushkin")
        ));
        // sad
        DEMO_DATA.put("sad", Arrays.asList(
            new Recommendation(7,"sad","music","Someone Like You – Adele","","https://open.spotify.com/track/4kflIGfjdZJW4ot2ioixTB",9.3,"Spotify"),
            new Recommendation(8,"sad","movie","Eternal Sunshine of the Spotless Mind","","https://www.imdb.com/title/tt0338013/",9.0,"IMDb"),
            new Recommendation(9,"sad","anime","Clannad: After Story","","https://myanimelist.net/anime/4181",9.6,"Funimation"),
            new Recommendation(10,"sad","book","The Fault in Our Stars","","https://www.goodreads.com/book/show/11870085",9.1,"Goodreads"),
            new Recommendation(11,"sad","game","Journey","","https://store.steampowered.com/app/638230/",9.2,"Steam"),
            new Recommendation(12,"sad","podcast","Griefcast","","https://www.cariadlloyd.com/griefcast",8.5,"Spotify")
        ));
        // angry
        DEMO_DATA.put("angry", Arrays.asList(
            new Recommendation(13,"angry","music","Break Stuff – Limp Bizkit","","https://open.spotify.com/track/5GL4W4w2ZRQX3EHKJ0JKGe",8.5,"Spotify"),
            new Recommendation(14,"angry","movie","John Wick","","https://www.imdb.com/title/tt2911666/",9.1,"IMDb"),
            new Recommendation(15,"angry","anime","Attack on Titan","","https://myanimelist.net/anime/16498",9.8,"Crunchyroll"),
            new Recommendation(16,"angry","book","The Count of Monte Cristo","","https://www.goodreads.com/book/show/7126.The_Count_of_Monte_Cristo",9.7,"Goodreads"),
            new Recommendation(17,"angry","game","DOOM Eternal","","https://store.steampowered.com/app/782330/",9.3,"Steam"),
            new Recommendation(18,"angry","podcast","Stuff They Don't Want You to Know","","https://iheartradio.com/",8.6,"iHeart")
        ));
        // surprised
        DEMO_DATA.put("surprised", Arrays.asList(
            new Recommendation(19,"surprised","music","WTF (Where They From) – Missy Elliott","","https://open.spotify.com",8.7,"Spotify"),
            new Recommendation(20,"surprised","movie","Parasite","","https://www.imdb.com/title/tt6751668/",9.5,"IMDb"),
            new Recommendation(21,"surprised","anime","Death Note","","https://myanimelist.net/anime/1535",9.4,"Netflix"),
            new Recommendation(22,"surprised","book","Gone Girl","","https://www.goodreads.com/book/show/19288043",9.0,"Goodreads"),
            new Recommendation(23,"surprised","game","The Stanley Parable","","https://store.steampowered.com/app/221910/",9.3,"Steam"),
            new Recommendation(24,"surprised","podcast","Radiolab","","https://radiolab.org/",9.1,"WNYC")
        ));
        // neutral
        DEMO_DATA.put("neutral", Arrays.asList(
            new Recommendation(25,"neutral","music","Lo-Fi Hip Hop Mix","","https://open.spotify.com",8.8,"Spotify"),
            new Recommendation(26,"neutral","movie","Interstellar","","https://www.imdb.com/title/tt0816692/",9.5,"IMDb"),
            new Recommendation(27,"neutral","anime","Fullmetal Alchemist: Brotherhood","","https://myanimelist.net/anime/5114",9.9,"Funimation"),
            new Recommendation(28,"neutral","book","Sapiens","","https://www.goodreads.com/book/show/23692271",9.3,"Goodreads"),
            new Recommendation(29,"neutral","game","Civilization VI","","https://store.steampowered.com/app/289070/",9.0,"Steam"),
            new Recommendation(30,"neutral","podcast","Lex Fridman Podcast","","https://lexfridman.com/podcast/",9.2,"YouTube")
        ));
        // fearful
        DEMO_DATA.put("fearful", Arrays.asList(
            new Recommendation(31,"fearful","music","Sound of Silence – Disturbed","","https://open.spotify.com",9.0,"Spotify"),
            new Recommendation(32,"fearful","movie","A Quiet Place","","https://www.imdb.com/title/tt6644200/",9.0,"IMDb"),
            new Recommendation(33,"fearful","anime","Demon Slayer","","https://myanimelist.net/anime/38000",9.5,"Crunchyroll"),
            new Recommendation(34,"fearful","book","It – Stephen King","","https://www.goodreads.com/book/show/830502.It",9.2,"Goodreads"),
            new Recommendation(35,"fearful","game","Resident Evil Village","","https://store.steampowered.com/app/1196590/",9.3,"Steam"),
            new Recommendation(36,"fearful","podcast","Casefile True Crime","","https://casefilepodcast.com/",8.9,"Self-pub")
        ));
        // disgusted
        DEMO_DATA.put("disgusted", Arrays.asList(
            new Recommendation(37,"disgusted","music","Losing My Religion – R.E.M.","","https://open.spotify.com",8.8,"Spotify"),
            new Recommendation(38,"disgusted","movie","Amélie","","https://www.imdb.com/title/tt0211915/",9.3,"IMDb"),
            new Recommendation(39,"disgusted","anime","Neon Genesis Evangelion","","https://myanimelist.net/anime/30/",9.4,"Netflix"),
            new Recommendation(40,"disgusted","book","Brave New World","","https://www.goodreads.com/book/show/5129",9.1,"Goodreads"),
            new Recommendation(41,"disgusted","game","Katana ZERO","","https://store.steampowered.com/app/460950/",9.2,"Steam"),
            new Recommendation(42,"disgusted","podcast","Hidden Brain","","https://hiddenbrain.org/",9.0,"NPR")
        ));
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Fetch up to {@code limit} recommendations for the given mood and categories.
     * Uses MySQL when available; otherwise returns demo data.
     */
    public List<Recommendation> getRecommendations(String mood,
                                                    List<String> categories,
                                                    int limit) {
        if (DatabaseConfig.isAvailable()) {
            try {
                return fetchFromDB(mood, categories, limit);
            } catch (Exception e) {
                System.err.println("[Service] DB query failed, using demo mode: " + e.getMessage());
            }
        }
        return fetchFromDemo(mood, categories, limit);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private List<Recommendation> fetchFromDB(String mood, List<String> categories, int limit)
            throws SQLException {
        Connection conn = DatabaseConfig.getConnection();
        if (conn == null) throw new SQLException("No connection");

        StringBuilder sb = new StringBuilder(
                "SELECT id, mood, category, title, image_url, link, rating, platform " +
                "FROM recommendations WHERE mood = ?");

        if (categories != null && !categories.isEmpty()) {
            sb.append(" AND category IN (");
            for (int i = 0; i < categories.size(); i++) {
                sb.append(i == 0 ? "?" : ",?");
            }
            sb.append(")");
        }
        sb.append(" ORDER BY rating DESC LIMIT ?");

        PreparedStatement ps = conn.prepareStatement(sb.toString());
        int idx = 1;
        ps.setString(idx++, mood.toLowerCase());
        if (categories != null) {
            for (String cat : categories) ps.setString(idx++, cat.toLowerCase());
        }
        ps.setInt(idx, limit);

        ResultSet rs = ps.executeQuery();
        List<Recommendation> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Recommendation(
                rs.getInt("id"),
                rs.getString("mood"),
                rs.getString("category"),
                rs.getString("title"),
                rs.getString("image_url"),
                rs.getString("link"),
                rs.getDouble("rating"),
                rs.getString("platform")
            ));
        }
        rs.close(); ps.close();
        return list;
    }

    private List<Recommendation> fetchFromDemo(String mood, List<String> categories, int limit) {
        List<Recommendation> all = DEMO_DATA.getOrDefault(mood.toLowerCase(),
                DEMO_DATA.get("neutral"));
        List<Recommendation> filtered = new ArrayList<>();
        for (Recommendation r : all) {
            boolean catMatch = (categories == null || categories.isEmpty() ||
                    categories.contains(r.getCategory().toLowerCase()));
            if (catMatch) filtered.add(r);
        }
        // If nothing matches the category filter, return all
        if (filtered.isEmpty()) filtered.addAll(all);
        return filtered.subList(0, Math.min(limit, filtered.size()));
    }
}
