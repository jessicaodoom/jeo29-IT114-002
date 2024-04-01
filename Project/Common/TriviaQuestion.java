package Common;

import java.util.List;

public class TriviaQuestion {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;

    public TriviaQuestion(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText; // jeo29/March31,2024: Question text initialization.
        this.options = options; // jeo29/March31,2024: Possible answers initialization.
        this.correctAnswerIndex = correctAnswerIndex; // jeo29/March31,2024: Correct answer index.
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
}
