// dthomas18@hawk.iit.edu
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static ArrayList<String> index;

    public static void main(String[] args) throws IOException {
        // Attempts to read a port number as the first argument, otherwise defaults to port 8888
        int PORT = 8888;
        if (args.length == 1) {
            PORT = Integer.parseInt(args[0]);
        }
        else if (args.length != 0) {
            System.out.println("Usage: java Server [<port>]");
            System.out.println("If port is not included, defaults to 8888.");
            System.exit(1);
        }

        System.out.println("Indexing words file...");
        index = indexFile();
        System.out.printf("%d words found.%n", index.size());

        try (ServerSocket server = new ServerSocket(PORT)) {
            server.setReuseAddress(true);

            System.out.println("Hangman server active! Listening on port " + PORT);

            while (true) {
                Socket client = server.accept();

                GameState state = startGame();

                System.out.printf("[%s] New client connected: %s%n", state.getUuid(), client.getInetAddress().getHostAddress());

                ClientHandler clientSocket = new ClientHandler(client, state);

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
     * Read all the words from the file.
     */
    private static ArrayList<String> indexFile() throws IOException {
        ArrayList<String> words = new ArrayList<>();

        // I didn't want to load the entire file into memory, but this seems to be the best way.
        // The RandomAccessFile method I had tried before was biased towards larger words.
        try (Scanner sc = new Scanner(new File("words-1.txt"))) {
            while (sc.hasNextLine()) {
                words.add(sc.nextLine());
            }
        }

        return words;
    }

    /**
     * Select a random word.
     */
    private static String getRandomWord() {
        Random rand = new Random();
        return index.get(rand.nextInt(index.size()));
//        // RandomAccessFile allows us to efficiently access a random position in a file
//        try (RandomAccessFile file = new RandomAccessFile(new File("words-1.txt"), "r")) {
//            // pick a random byte in the file to select from and seek to it
//            long randomLocation = (long) (Math.random() * file.length());
//            file.seek(randomLocation);
//            // seek back to the first newline (or the beginning of the file, whichever comes first)
//            while (file.getFilePointer() != 0 && (char) file.readByte() != '\n') {
//                randomLocation--;
//                file.seek(randomLocation);
//            }
//            // read the word from the file
//            return file.readLine();
//        }
    }
}

class ClientHandler implements Runnable {
    private final Socket client;
    private GameState state;

    public ClientHandler(Socket client, GameState state) {
        this.client = client;
        this.state = state;
    }

    public void run() {
        try (PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            sendStatus(out, -2);

            String line;
            int status;
            while ((line = in.readLine()) != null) {
                status = -2;
                if (line.length() == 1) {
                    char letter = line.charAt(0);
                    if (letter >= 'a' && letter <= 'z') {
                        status = state.guess(letter);
                    }
                }
                if (state.wordGuessed() || state.getGuesses() == 0) {
                    status = -3;
                }
                sendStatus(out, status);
                if (status == -3) { break; }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.printf("[%s] Client disconnected.%n", state.getUuid());
        }
    }

    private void sendStatus(PrintWriter out, int status) {
        out.printf("%d;%d;%s%n", status, state.getGuesses(), state.getWord());
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
                return word.length() - word.replaceAll(String.valueOf(letter),"").length();
            } else {
                guesses--;
                return 0;
            }
        }
    }
}