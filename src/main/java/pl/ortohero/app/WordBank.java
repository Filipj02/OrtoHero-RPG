package pl.ortohero.app;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WordBank {

    private List<Word> allWords;

    public WordBank() {
        loadWordsFromJson();
    }

    private void loadWordsFromJson() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/words.json");
            if (inputStream == null) {
                System.err.println("BŁĄD: Nie znaleziono pliku words.json!");
                allWords = new ArrayList<>();
                return;
            }
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Word>>(){}.getType();
            allWords = gson.fromJson(reader, listType);
            System.out.println("Załadowano słów: " + allWords.size());
        } catch (Exception e) {
            e.printStackTrace();
            allWords = new ArrayList<>();
        }
    }

    public List<Word> getRandomWords(int count, int difficultyLevel) {
        if (allWords == null || allWords.isEmpty()) return new ArrayList<>();
        List<Word> filtered = new ArrayList<>(allWords); // Uproszczone
        Collections.shuffle(filtered);
        return filtered.stream().limit(count).collect(Collectors.toList());
    }

    // --- TO JEST TA NOWA METODA, KTÓREJ BRAKOWAŁO ---
    public List<Word> getWordsByDifficulty(int count, List<Integer> allowedDifficulties) {
        if (allWords == null || allWords.isEmpty()) return new ArrayList<>();

        List<Word> filtered = new ArrayList<>();
        for (Word w : allWords) {
            if (allowedDifficulties.contains(w.getDifficulty())) {
                filtered.add(w);
            }
        }

        Collections.shuffle(filtered);
        if (filtered.size() < count) return filtered;
        return filtered.subList(0, count);
    }
}