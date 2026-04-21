# 🎵 MoodTune — AI-Powered Emotion-Based Media Recommender
### Java AWT Desktop Edition  |  By Abhishek Samal

---

## Overview

MoodTune is a native Java desktop application that detects your mood (via camera or manual selection) and instantly recommends personalised **Music, Movies, Anime, Books, Games, and Podcasts** from a MySQL database — all wrapped in a sleek dark-mode AWT interface.

---

## Technology Stack

| Layer              | Technology                          |
|--------------------|-------------------------------------|
| Language           | Java (JDK 11+)                      |
| UI Framework       | Java AWT (Abstract Window Toolkit)  |
| Business Logic     | Plain Java (Service Layer)          |
| Database           | MySQL 8.x                           |
| DB Connectivity    | JDBC (mysql-connector-java)         |
| Camera (optional)  | Sarxos Webcam API                   |

---

## Project Structure

```
MoodTune/
├── src/
│   └── com/moodtune/
│       ├── ui/
│       │   ├── MoodTuneApp.java        ← Main window + CardLayout nav
│       │   ├── Styles.java             ← Global colours, fonts, helpers
│       │   ├── WelcomeScreen.java      ← Step 1: Name + category prefs
│       │   ├── MoodSelectScreen.java   ← Step 2: Camera + mood buttons
│       │   └── DetectionScreen.java    ← Step 3: Recommendation cards
│       ├── service/
│       │   ├── RecommendationService.java  ← MySQL + demo-mode fallback
│       │   └── CameraService.java          ← Real/mock webcam panel
│       ├── db/
│       │   └── DatabaseConfig.java     ← JDBC connection manager
│       └── model/
│           └── Recommendation.java     ← Data model
├── lib/                                ← Place mysql-connector-java.jar here
├── bin/                                ← Compiled .class files (auto-created)
├── schema.sql                          ← DB schema + 84 seed records
├── run.sh                              ← Build & run (Linux/macOS)
└── run.bat                             ← Build & run (Windows)
```

---

## Screens

### Screen 1 — Welcome
- Enter your name
- Choose content categories (Music / Movies / Anime / Books / Games / Podcasts)
- Dark gradient card UI with glow orbs

### Screen 2 — Mood Selection
- Animated camera preview (mock simulator or real Sarxos webcam)
- 7 mood buttons: 😄 Happy · 😢 Sad · 😠 Angry · 😲 Surprised · 😐 Neutral · 😨 Fearful · 🤢 Disgusted
- Click any mood to proceed

### Screen 3 — Results Dashboard
- Mood pill badge with colour-coded indicator
- Scrollable grid of recommendation cards
- Each card shows: category icon, title, platform, star rating bar
- Click any card to open the link in your browser

---

## Getting Started

### 1. Database Setup

```bash
# Log in to MySQL
mysql -u root -p

# Run the schema file
source /path/to/MoodTune/schema.sql

# Verify (should show 12 rows per mood × 7 moods = 84 total)
SELECT mood, COUNT(*) FROM moodtune.recommendations GROUP BY mood;
```

### 2. Configure DB Password

Edit `src/com/moodtune/db/DatabaseConfig.java`:

```java
public static final String PASSWORD = "your_mysql_password_here";
```

### 3. Add MySQL JDBC Driver

Download from: https://dev.mysql.com/downloads/connector/j/

Place the `.jar` file inside the `lib/` folder:
```
MoodTune/lib/mysql-connector-java-8.x.x.jar
```

### 4. Build & Run

**Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

**Windows:**
```
Double-click run.bat
```

**Manual (any OS):**
```bash
# Compile
javac -cp "lib/mysql-connector-java.jar" -d bin $(find src -name "*.java")

# Run
java -cp "bin:lib/mysql-connector-java.jar" com.moodtune.ui.MoodTuneApp
```

---

## Demo Mode (No MySQL Required)

If MySQL is not running or the JDBC jar is missing, MoodTune automatically switches to **Demo Mode** — all 7 moods still work with hardcoded sample data. No crash, no error dialog.

---

## Optional: Real Webcam

To enable live webcam capture:

1. Download the [Sarxos Webcam API](https://github.com/sarxos/webcam-capture/releases).
2. Place `webcam-capture-X.X.X.jar` and its dependencies in `lib/`.
3. Add them to the classpath when compiling and running.

Without the jar, the camera panel shows an animated simulator instead.

---

## Extending MoodTune

| Goal                          | What to change                                             |
|-------------------------------|-----------------------------------------------------------|
| Add a new mood                | Add row to `DEMO_DATA` map in `RecommendationService.java`, insert DB rows in `schema.sql`, add button in `MoodSelectScreen.java` |
| Add a new category            | Add to `catNames` / `catLabels` in `WelcomeScreen.java`, seed `schema.sql` |
| Change colours                | Edit constants in `Styles.java`                            |
| More recommendations shown    | Change `recommendationCount` default in `MoodTuneApp.java`|
| Add real AI mood detection    | Replace the mood buttons in `MoodSelectScreen` with calls to your AI endpoint using `CameraService.captureFrameAsBase64()` |

---

## Credits

**By Abhishek Samal**

Tech: Java · AWT · MySQL · JDBC
