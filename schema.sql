-- ============================================================
--  MoodTune Database Schema + Seed Data
--  Run this file in MySQL before launching the application.
--
--  Usage:
--    mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS moodtune
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE moodtune;

-- ── Table ─────────────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS recommendations;

CREATE TABLE recommendations (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    mood      VARCHAR(50)  NOT NULL,
    category  VARCHAR(50)  NOT NULL,
    title     VARCHAR(255) NOT NULL,
    image_url TEXT,
    link      TEXT,
    rating    FLOAT        DEFAULT 0,
    platform  VARCHAR(100),
    INDEX idx_mood     (mood),
    INDEX idx_category (category),
    INDEX idx_mood_cat (mood, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Seed: HAPPY ──────────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('happy','music',   'Happy – Pharrell Williams',         '', 'https://open.spotify.com/track/60nZcImufyMA1MKQY3dcCO', 9.1, 'Spotify'),
('happy','music',   'Good as Hell – Lizzo',              '', 'https://open.spotify.com/track/3Kkjo3cT83cw8WVa3MTRSi', 8.9, 'Spotify'),
('happy','movie',   'The Secret Life of Walter Mitty',   '', 'https://www.imdb.com/title/tt0359950/', 8.7, 'IMDb'),
('happy','movie',   'La La Land',                        '', 'https://www.imdb.com/title/tt3783958/', 9.2, 'IMDb'),
('happy','anime',   'Spy x Family',                      '', 'https://myanimelist.net/anime/50265',   9.2, 'Crunchyroll'),
('happy','anime',   'Nichijou',                          '', 'https://myanimelist.net/anime/10165',   9.3, 'Funimation'),
('happy','book',    'The Hitchhiker\'s Guide to the Galaxy','','https://www.goodreads.com/book/show/11.The_Hitchhiker_s_Guide_to_the_Galaxy', 9.5, 'Goodreads'),
('happy','book',    'The Alchemist',                     '', 'https://www.goodreads.com/book/show/865.The_Alchemist', 9.0, 'Goodreads'),
('happy','game',    'Stardew Valley',                    '', 'https://store.steampowered.com/app/413150/', 9.4, 'Steam'),
('happy','game',    'Animal Crossing: New Horizons',     '', 'https://www.nintendo.com/us/store/products/animal-crossing-new-horizons-switch/', 9.1, 'Nintendo'),
('happy','podcast', 'The Happiness Lab',                 '', 'https://www.happinesslab.fm/',           8.8, 'Pushkin'),
('happy','podcast', 'Conan O\'Brien Needs a Friend',     '', 'https://conanobrien.com/podcast/',       8.9, 'Earwolf');

-- ── Seed: SAD ────────────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('sad','music',   'Someone Like You – Adele',             '', 'https://open.spotify.com/track/4kflIGfjdZJW4ot2ioixTB', 9.3, 'Spotify'),
('sad','music',   'The Night Will Always Win – Manchester Orchestra','','https://open.spotify.com', 8.8, 'Spotify'),
('sad','movie',   'Eternal Sunshine of the Spotless Mind','', 'https://www.imdb.com/title/tt0338013/', 9.0, 'IMDb'),
('sad','movie',   'Her',                                  '', 'https://www.imdb.com/title/tt1798709/', 9.1, 'IMDb'),
('sad','anime',   'Clannad: After Story',                 '', 'https://myanimelist.net/anime/4181',    9.6, 'Funimation'),
('sad','anime',   'Your Lie in April',                    '', 'https://myanimelist.net/anime/23273',   9.4, 'Netflix'),
('sad','book',    'The Fault in Our Stars',               '', 'https://www.goodreads.com/book/show/11870085', 9.1, 'Goodreads'),
('sad','book',    'A Little Life',                        '', 'https://www.goodreads.com/book/show/22822858', 9.3, 'Goodreads'),
('sad','game',    'Journey',                              '', 'https://store.steampowered.com/app/638230/', 9.2, 'Steam'),
('sad','game',    'Gris',                                 '', 'https://store.steampowered.com/app/683320/', 9.4, 'Steam'),
('sad','podcast', 'Griefcast',                            '', 'https://www.cariadlloyd.com/griefcast/',8.5, 'Spotify'),
('sad','podcast', 'On Being with Krista Tippett',         '', 'https://onbeing.org/series/podcast/',   9.0, 'PRX');

-- ── Seed: ANGRY ──────────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('angry','music',   'Break Stuff – Limp Bizkit',          '', 'https://open.spotify.com', 8.5, 'Spotify'),
('angry','music',   'Killing in the Name – RATM',         '', 'https://open.spotify.com', 9.0, 'Spotify'),
('angry','movie',   'John Wick',                          '', 'https://www.imdb.com/title/tt2911666/', 9.1, 'IMDb'),
('angry','movie',   'Mad Max: Fury Road',                 '', 'https://www.imdb.com/title/tt1392190/', 9.4, 'IMDb'),
('angry','anime',   'Attack on Titan',                    '', 'https://myanimelist.net/anime/16498',   9.8, 'Crunchyroll'),
('angry','anime',   'Vinland Saga',                       '', 'https://myanimelist.net/anime/37521',   9.5, 'Amazon'),
('angry','book',    'The Count of Monte Cristo',          '', 'https://www.goodreads.com/book/show/7126', 9.7, 'Goodreads'),
('angry','book',    '1984 – George Orwell',               '', 'https://www.goodreads.com/book/show/40961427', 9.5, 'Goodreads'),
('angry','game',    'DOOM Eternal',                       '', 'https://store.steampowered.com/app/782330/', 9.3, 'Steam'),
('angry','game',    'God of War',                         '', 'https://store.steampowered.com/app/1593500/', 9.7, 'Steam'),
('angry','podcast', 'Stuff They Don\'t Want You to Know', '', 'https://iheartradio.com/', 8.6, 'iHeart'),
('angry','podcast', 'The Daily',                         '', 'https://www.nytimes.com/column/the-daily', 8.9, 'NYT');

-- ── Seed: SURPRISED ──────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('surprised','music',   'Bohemian Rhapsody – Queen',    '', 'https://open.spotify.com', 9.6, 'Spotify'),
('surprised','music',   'WTF – Missy Elliott',          '', 'https://open.spotify.com', 8.7, 'Spotify'),
('surprised','movie',   'Parasite',                     '', 'https://www.imdb.com/title/tt6751668/', 9.5, 'IMDb'),
('surprised','movie',   'The Prestige',                 '', 'https://www.imdb.com/title/tt0482571/', 9.4, 'IMDb'),
('surprised','anime',   'Death Note',                   '', 'https://myanimelist.net/anime/1535',    9.4, 'Netflix'),
('surprised','anime',   'Steins;Gate',                  '', 'https://myanimelist.net/anime/9253',    9.7, 'Funimation'),
('surprised','book',    'Gone Girl',                    '', 'https://www.goodreads.com/book/show/19288043', 9.0, 'Goodreads'),
('surprised','book',    'The Girl with the Dragon Tattoo','','https://www.goodreads.com/book/show/2429135', 9.1, 'Goodreads'),
('surprised','game',    'The Stanley Parable',          '', 'https://store.steampowered.com/app/221910/', 9.3, 'Steam'),
('surprised','game',    'Outer Wilds',                  '', 'https://store.steampowered.com/app/753640/', 9.5, 'Steam'),
('surprised','podcast', 'Radiolab',                     '', 'https://radiolab.org/',                 9.1, 'WNYC'),
('surprised','podcast', 'Freakonomics Radio',           '', 'https://freakonomics.com/',             8.8, 'Stitcher');

-- ── Seed: NEUTRAL ────────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('neutral','music',   'Lo-Fi Hip Hop Beats',                  '', 'https://open.spotify.com', 8.8, 'Spotify'),
('neutral','music',   'Kind of Blue – Miles Davis',           '', 'https://open.spotify.com', 9.5, 'Spotify'),
('neutral','movie',   'Interstellar',                         '', 'https://www.imdb.com/title/tt0816692/', 9.5, 'IMDb'),
('neutral','movie',   'The Grand Budapest Hotel',             '', 'https://www.imdb.com/title/tt2278388/', 9.0, 'IMDb'),
('neutral','anime',   'Fullmetal Alchemist: Brotherhood',     '', 'https://myanimelist.net/anime/5114',    9.9, 'Funimation'),
('neutral','anime',   'Mushishi',                             '', 'https://myanimelist.net/anime/457',     9.3, 'Crunchyroll'),
('neutral','book',    'Sapiens',                              '', 'https://www.goodreads.com/book/show/23692271', 9.3, 'Goodreads'),
('neutral','book',    'Atomic Habits',                        '', 'https://www.goodreads.com/book/show/40121378', 9.1, 'Goodreads'),
('neutral','game',    'Civilization VI',                      '', 'https://store.steampowered.com/app/289070/', 9.0, 'Steam'),
('neutral','game',    'Minecraft',                            '', 'https://www.minecraft.net/', 9.4, 'Mojang'),
('neutral','podcast', 'Lex Fridman Podcast',                  '', 'https://lexfridman.com/podcast/', 9.2, 'YouTube'),
('neutral','podcast', 'How I Built This',                     '', 'https://www.npr.org/podcasts/510313/', 8.9, 'NPR');

-- ── Seed: FEARFUL ────────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('fearful','music',   'Sound of Silence – Disturbed',   '', 'https://open.spotify.com', 9.0, 'Spotify'),
('fearful','music',   'Creep – Radiohead',               '', 'https://open.spotify.com', 8.9, 'Spotify'),
('fearful','movie',   'A Quiet Place',                   '', 'https://www.imdb.com/title/tt6644200/', 9.0, 'IMDb'),
('fearful','movie',   'Hereditary',                      '', 'https://www.imdb.com/title/tt7784604/', 8.8, 'IMDb'),
('fearful','anime',   'Demon Slayer',                    '', 'https://myanimelist.net/anime/38000',   9.5, 'Crunchyroll'),
('fearful','anime',   'Tokyo Ghoul',                     '', 'https://myanimelist.net/anime/22319',   8.8, 'Funimation'),
('fearful','book',    'It – Stephen King',               '', 'https://www.goodreads.com/book/show/830502.It', 9.2, 'Goodreads'),
('fearful','book',    'The Shining',                     '', 'https://www.goodreads.com/book/show/11588', 9.3, 'Goodreads'),
('fearful','game',    'Resident Evil Village',           '', 'https://store.steampowered.com/app/1196590/', 9.3, 'Steam'),
('fearful','game',    'Subnautica',                      '', 'https://store.steampowered.com/app/264710/', 9.2, 'Steam'),
('fearful','podcast', 'Casefile True Crime',             '', 'https://casefilepodcast.com/', 8.9, 'Self-pub'),
('fearful','podcast', 'My Favorite Murder',              '', 'https://myfavoritemurder.com/', 8.7, 'Exactly Right');

-- ── Seed: DISGUSTED ──────────────────────────────────────────────────────────
INSERT INTO recommendations (mood, category, title, image_url, link, rating, platform) VALUES
('disgusted','music',   'Losing My Religion – R.E.M.',      '', 'https://open.spotify.com', 8.8, 'Spotify'),
('disgusted','music',   'Karma Police – Radiohead',         '', 'https://open.spotify.com', 9.2, 'Spotify'),
('disgusted','movie',   'Amélie',                           '', 'https://www.imdb.com/title/tt0211915/', 9.3, 'IMDb'),
('disgusted','movie',   'V for Vendetta',                   '', 'https://www.imdb.com/title/tt0434409/', 9.2, 'IMDb'),
('disgusted','anime',   'Neon Genesis Evangelion',          '', 'https://myanimelist.net/anime/30/',     9.4, 'Netflix'),
('disgusted','anime',   'Parasyte: The Maxim',              '', 'https://myanimelist.net/anime/22535',   9.1, 'Crunchyroll'),
('disgusted','book',    'Brave New World',                  '', 'https://www.goodreads.com/book/show/5129', 9.1, 'Goodreads'),
('disgusted','book',    'Animal Farm',                      '', 'https://www.goodreads.com/book/show/7613', 9.0, 'Goodreads'),
('disgusted','game',    'Katana ZERO',                      '', 'https://store.steampowered.com/app/460950/', 9.2, 'Steam'),
('disgusted','game',    'Hades',                            '', 'https://store.steampowered.com/app/1145360/', 9.6, 'Steam'),
('disgusted','podcast', 'Hidden Brain',                     '', 'https://hiddenbrain.org/', 9.0, 'NPR'),
('disgusted','podcast', 'Philosophize This!',               '', 'https://philosophizethis.org/', 9.1, 'Self-pub');

-- ── Verify ────────────────────────────────────────────────────────────────────
SELECT mood, COUNT(*) AS count FROM recommendations GROUP BY mood ORDER BY mood;
