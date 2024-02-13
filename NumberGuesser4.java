import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class NumberGuesser4 {
    private int maxLevel = 1;
    private int level = 1;
    private int strikes = 0;
    private int maxStrikes = 5;
    private int number = -1;
    private boolean pickNewRandom = true;
    private Random random = new Random();
    private String fileName = "ng4.txt";
    private String[] fileHeaders = { "Level", "Strikes", "Number", "MaxLevel" };

    private void saveState() {
        String[] data = { level + "", strikes + "", number + "", maxLevel + "" };
        String output = String.join(",", data);
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(String.join(",", fileHeaders) + "\n" + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadState() {
        File file = new File(fileName);
        if (!file.exists()) {
            return;
        }
        try (Scanner reader = new Scanner(file)) {
            reader.nextLine(); // Skip header
            if (reader.hasNextLine()) {
                String text = reader.nextLine();
                String[] data = text.split(",");
                // Basic integrity check to detect data tampering - jeo29/February11,2024
                if (data.length != fileHeaders.length) {
                    System.out.println("Data tampering detected. Resetting game.");
                    resetGame();
                    return;
                }
                this.level = strToNum(data[0]);
                this.strikes = strToNum(data[1]);
                this.number = strToNum(data[2]);
                this.maxLevel = strToNum(data[3]);
                pickNewRandom = false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void generateNewNumber(int level) {
        int range = 10 + ((level - 1) * 5);
        number = random.nextInt(range) + 1;
    }

    private void win() {
        System.out.println("That's right!");
        level++;
        strikes = 0;
        pickNewRandom = true;
    }

    private void lose() {
        System.out.println("Uh oh, looks like you need to get some more practice.");
        level = Math.max(1, --level);
        strikes = 0;
        pickNewRandom = true;
    }

    private void processGuess(int guess) {
        System.out.println("You guessed " + guess);
        if (guess == number) {
            win();
        } else {
            System.out.println("That's wrong");
            int diff = Math.abs(guess - number);
            if (diff <= 2) {
                System.out.println("Hot"); // jeo29/February11,2024 
            } else if (diff <= 5) {
                System.out.println("Warm"); // jeo29/February11,2024 
            } else {
                System.out.println("Cold"); // jeo29/February11,2024 
            }
            if (++strikes >= maxStrikes) {
                lose();
            }
        }
        saveState();
    }

    private int strToNum(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return -1; // Return an invalid number to handle error
        }
    }

    private void resetGame() {
        level = 1;
        strikes = 0;
        number = -1;
        maxLevel = 1;
        pickNewRandom = true;
        System.out.println("Game reset due to data integrity issues."); // jeo29/February11,2024 - Anti-Data Tampering
        saveState(); // Save the reset state immediately
    }

    public void start() {
        System.out.println("Welcome to NumberGuesser4.0");
        loadState();
        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                if (pickNewRandom) {
                    generateNewNumber(level);
                    System.out.println("Welcome to level " + level + ". I picked a random number. Can you guess it?");
                    pickNewRandom = false;
                }
                System.out.println("Enter your guess (or type 'quit' to exit):");
                String
