import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class Server {
    public static void main(String[] args) throws IOException {
        ArrayList<GameState> states = new ArrayList<GameState>();
        try (ServerSocket server = new ServerSocket(8888)) {
            server.setReuseAddress(true);

            while (true) {
                Socket client = server.accept();

                System.out.printf("New client connected: %s%n", client.getInetAddress().getHostAddress());

                ClientHandler clientSocket = new ClientHandler(client);

                new Thread(clientSocket).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a game state with the defined word.
     */
    private static GameState startGame(String word) throws IOException {
        return new GameState(word);
    }

    /**
     * Create a game state with a randomly selected word.
     */
    private static GameState startGame() throws IOException {
        return startGame(getRandomWord());
    }

    /**
     * Select a random word from our words.txt file.
     */
    private static String getRandomWord() throws IOException {
        // RandomAccessFile allows us to efficiently access a random position in a file
        try (RandomAccessFile file = new RandomAccessFile(new File("words-1.txt"), "r")) {
            // pick a random byte in the file to select from and seek to it
            long randomLocation = (long) (Math.random() * file.length());
            file.seek(randomLocation);
            // seek back to the first newline (or the beginning of the file, whichever comes first)
            while (file.getFilePointer() != 0 && (char) file.readByte() != '\n') {
                randomLocation--;
                file.seek(randomLocation);
            }
            // read the word from the file
            return file.readLine();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket client;

    public ClientHandler(Socket client) {
        this.client = client;
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            // TODO: setup protocol
        }
    }
}

/**
 * Internal representation of game state. Provides functions for interacting with internal variables, and handles
 * obfuscating the word when read.
 */
class GameState {
    private final String uuid;
    // The word we want to guess
    private final String word;
    private int guesses = 10;
    // We represent guesses with an array of booleans; the index represents which letter. For example, 0 = a, 1 = b,
    // and so on. Letters are set to true if they have been guessed.
    boolean[] guessedLetters = new boolean[26];
    boolean[] lettersInWord = new boolean[26];

    public GameState(String word) {
        this.uuid = UUID.randomUUID().toString();
        this.word = word;
        // set our lettersInWord array properly
        for (int i = 0; i < lettersInWord.length; i++) {
            char letter = (char) ('a' + i);
            if (word.indexOf(letter) != -1) {
                lettersInWord[i] = true;
            }
        }
    }

    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the word stored in the GameState. If the game is ongoing, only show the solved letters, otherwise show
     * the entire word.
     */
    public String getWord() {
        // if the game is still ongoing, filter out letters we haven't guessed
        if (guesses != 0) {
            StringBuilder printedWord = new StringBuilder();
            // loop through the string and insert guessed letters
            for (int i = 0; i < this.word.length(); i++) {
                char letter = this.word.charAt(i);
                if (isGuessed(letter)) {
                    printedWord.append(letter);
                } else {
                    // if a letter is not used yet, replace it with '_'
                    printedWord.append('_');
                }
            }
            return printedWord.toString();
        }
        else {
            return this.word;
        }
    }

    /**
     * Get the current amount of guesses.
     */
    public int getGuesses() {
        return guesses;
    }

    /**
     * Get the array index for the guessed letter.
     */
    public int getPosForLetter(char letter) {
        return letter - 'a';
    }

    /**
     * Check if a letter has been guessed.
     */
    public boolean isGuessed(char letter) {
        return guessedLetters[getPosForLetter(letter)];
    }

    /**
     * Check if a letter is in the final word.
     */
    public boolean isInWord(char letter) {
        return lettersInWord[getPosForLetter(letter)];
    }

    /**
     * Check if the full word has been guessed.
     */
    public boolean wordGuessed() {
        for (int i = 0; i < lettersInWord.length; i++) {
            if (lettersInWord[i] && !guessedLetters[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Guess a letter. Update game state accordingly.
     * @return If the letter is in the word, 1. If the letter is not in the word, 0. In other cases, -1.
     */
    public int guess(char letter) {
        if (isGuessed(letter)) {
            return -1;
        }
        else {
            guessedLetters[getPosForLetter(letter)] = true;
            if (isInWord(letter)) {
                return 1;
            } else {
                guesses--;
                return 0;
            }
        }
    }
}