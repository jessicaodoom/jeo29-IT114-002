package Common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TriviaDataLoader {
    private Map<String, List<TriviaQuestion>> categories = new HashMap<>();
    private Random random = new Random();

    public TriviaDataLoader() {
        loadSampleData(); // jeo29/March31,2024: Sample data loading method call.
    }

    private void loadSampleData() {
        // jeo29/March31,2024: Sample questions and categories.
        categories.put("Science", Arrays.asList(
            new TriviaQuestion("What is the chemical symbol for Gold?", Arrays.asList("Au", "Ag", "Pt", "Pb"), 0),
            new TriviaQuestion("Water boils at 212 degrees on which temperature scale?", Arrays.asList("Celsius", "Fahrenheit", "Kelvin"), 1),
            new TriviaQuestion("What planet is known as the Red Planet?", Arrays.asList("Venus", "Saturn", "Mars"), 2)
        ));

        categories.put("Geography", Arrays.asList(
            new TriviaQuestion("What is the capital of France?", Arrays.asList("Paris", "London", "Berlin", "Madrid"), 0),
            new TriviaQuestion("Which country has the largest land area?", Arrays.asList("China", "USA", "Russia", "Canada"), 2),
            new TriviaQuestion("What is the longest river in the world?", Arrays.asList("Amazon", "Nile", "Yangtze"), 1)
        ));
    }

    public String pickRandomCategory() {
        Object[] keys = categories.keySet().toArray();
        return (String) keys[random.nextInt(keys.length)]; // jeo29/March31,2024: Random category selection.
    }

    public TriviaQuestion pickRandomQuestion(String category) {
        List<TriviaQuestion> questions = categories.get(category);
        return questions.get(random.nextInt(questions.size())); // jeo29/March31,2024: Random question selection within category.
    }
}
