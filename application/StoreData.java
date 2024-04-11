package application;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StoreData {
	public static void storeData(String level, int score) {
    	JsonObject levelsData = null;
        try (FileReader reader = new FileReader("levels.json")) {
            levelsData = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
           
        }
        
        if (levelsData != null) {
            JsonObject level1 = levelsData.getAsJsonObject(level);
            if (level1 != null) {
                int currentHighScore = level1.get("high_score").getAsInt();
                int newScore = score;
                if (newScore > currentHighScore) {
                    level1.addProperty("high_score", newScore);
                    try (FileWriter writer = new FileWriter("../space-shooter-group-7-backup/data.json")) {
                        new Gson().toJson(levelsData, writer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
