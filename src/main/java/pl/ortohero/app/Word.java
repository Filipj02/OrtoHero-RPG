package pl.ortohero.app;

public class Word {
    // Pola (muszą pasować do JSON)
    private String fullWord;
    private String target;
    private String rule;
    private int difficulty;

    public Word(String fullWord, String target, String rule, int difficulty) {
        this.fullWord = fullWord;
        this.target = target;
        this.rule = rule;
        this.difficulty = difficulty;
    }


    public String getMaskedWord() {
        if (target != null && fullWord != null) {
            return fullWord.replaceFirst(target, "_");
        }
        return "???";
    }


    public boolean checkAnswer(String answer) {
        if (target == null || answer == null) return false;
        return target.equalsIgnoreCase(answer);
    }




    // Gettery
    public String getFullWord() { return fullWord; }
    public String getTarget() { return target; }
    public String getRule() { return rule; }
    public int getDifficulty() { return difficulty; }
}