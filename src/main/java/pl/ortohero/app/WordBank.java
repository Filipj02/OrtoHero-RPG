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
            // 1. Otwórz plik z zasobów (resources)
            InputStream inputStream = getClass().getResourceAsStream("/words.json");

            if (inputStream == null) {
                System.err.println("BŁĄD: Nie znaleziono pliku words.json!");
                allWords = new ArrayList<>(); // Pusta lista, żeby program się nie wywalił
                return;
            }

            // 2. Przygotuj czytnik (Gson)
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Gson gson = new Gson();

            // 3. Określ typ danych: Lista obiektów Word
            Type listType = new TypeToken<ArrayList<Word>>(){}.getType();

            // 4. Zamień JSON na listę w Javie
            allWords = gson.fromJson(reader, listType);

            System.out.println("Załadowano słów: " + allWords.size());

        } catch (Exception e) {
            e.printStackTrace();
            allWords = new ArrayList<>();
        }
    }

    public List<Word> getRandomWords(int count, int difficultyLevel) {
        if (allWords == null || allWords.isEmpty()) {
            return new ArrayList<>();
        }

        List<Word> filtered;
        if (difficultyLevel > 0) {
            filtered = allWords.stream()
                    .filter(w -> w.getDifficulty() <= difficultyLevel)
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>(allWords);
        }

        Collections.shuffle(filtered);
        return filtered.stream().limit(count).collect(Collectors.toList());
    }
}