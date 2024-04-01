package Server; // jeo29/March31,2024: Package declaration.

import java.util.List;

public class QuestionPayload extends Payload { // jeo29/March31,2024: Question payload for trivia.
    private String question;
    private List<String> answers;

    public QuestionPayload(String question, List<String> answers) { // jeo29/March31,2024: Constructor.
        super(PayloadType.QUESTION);
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() { // jeo29/March31,2024: Get question text.
        return question;
    }

    public List<String> getAnswers() { // jeo29/March31,2024: Get possible answers.
        return answers;
    }
}
