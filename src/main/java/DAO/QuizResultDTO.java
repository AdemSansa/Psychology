package DAO;

public class QuizResultDTO {

    private int stress;
    private int anxiety;
    private int depression;
    private int burnout;

    public QuizResultDTO(int stress, int anxiety, int depression, int burnout) {
        this.stress = stress;
        this.anxiety = anxiety;
        this.depression = depression;
        this.burnout = burnout;
    }
    public int getStress() { return stress; }
    public int getAnxiety() { return anxiety; }
    public int getDepression() { return depression; }
    public int getBurnout() { return burnout; }
}
