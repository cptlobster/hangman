// dthomas18@hawk.iit.edu
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 1 && args.length != 2) {
            System.out.println("Usage: java Client <hostname> [<port>]");
            System.out.println("If port is not included, defaults to 8888.");
            System.exit(1);
        }

        String HOST = args[0];
        int PORT = 8888;

        if (args.length == 2) {
            PORT = Integer.parseInt(args[1]);
        }

        boolean p = true;

        IOManager sp = new IOManager();
        while (p) {
            try (Socket socket = new Socket(HOST, PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

                String line;
                boolean c = true;
                while ((line = in.readLine()) != null) {
                    c = sp.handleState(line);

                    if (c) {
                        String response = String.valueOf(sp.prompt());
                        out.println(response);
                    }
                }
                if (c) {
                    System.out.println("Lost connection to server.");
                }
            }
            p = sp.promptContinue();
        }
    }
}

class IOManager {
    String[] states = {
            "\n\n\n\n\n",
            "  O\n\n\n\n\n",
            "  O\n  |\n  |\n\n\n",
            "  O\n  |\n  |\n /\n |\n",
            "  O\n  |\n  |\n / \\\n | |\n",
            "  O\n /|\n  |\n / \\\n | |\n",
            "  O\n /|\\\n  |\n / \\\n | |\n",
            "  O\n /|\\\n| |\n / \\\n | |\n",
            "  O\n /|\\\n| | |\n / \\\n | |\n",
            "  O\n /|\\\n| | |\n / \\\n_| |\n",
            "  O\n /|\\\n| | |\n / \\\n_| |_\n"
    };

    void showState(String word, int guesses) {
        // System.out.println(states[10 - guesses]);
        StringBuilder sb = new StringBuilder();
        sb.append(word.charAt(0));
        for (int i = 1; i < word.length(); i++) {
            sb.append(' ');
            sb.append(word.charAt(i));
        }
        System.out.println(sb.toString());
        System.out.printf("%nYou have %d attempts left.%n", guesses);
    }

    void win(String word, int guesses) {
        // System.out.println(states[10 - guesses]);
        System.out.printf("You win! You guessed the word \"%s\" with %d attempts left.%n", word, guesses);
    }

    void lose(String word) {
        // System.out.println(states[10]);
        System.out.printf("You lose. The word was \"%s\".%n", word);
    }

    boolean handleState(String response) {
        // System.out.println(response);
        String[] segments = response.split(";");
        int retval = Integer.parseInt(segments[0]);
        int guesses = Integer.parseInt(segments[1]);
        String word = segments[2];
        switch (retval) {
            case 0: {
                System.out.println("Letter not present.");
                break;
            }
            case -1: {
                System.out.println("You already guessed this letter.");
                break;
            }
            case -3: {
                if (guesses == 0) {
                    lose(word);
                }
                else {
                    win(word, guesses);
                }
                return false;
            }
            default: {
                System.out.printf("Letter present %d times.%n", retval);
                break;
            }
        }
        showState(word, guesses);
        return true;
    }

    char prompt() throws IOException {
        InputStreamReader br = new InputStreamReader(System.in);
        boolean validChar = false;
        char letter = ' ';
        while (!validChar) {
            System.out.print("Enter a letter: ");
            letter = (char)br.read();
            if (letter >= 'a' && letter <= 'z') {
                validChar = true;
            }
            // if an uppercase character is appended, replace with lowercase equivalent
            else if (letter >= 'A' && letter <= 'Z') {
                letter = (char) (letter - 'A' + 'a');
                validChar = true;
            }
            else {
                System.out.println("\nInvalid letter.");
            }
        }
        return letter;
    }

    boolean promptContinue() throws IOException {
        InputStreamReader br = new InputStreamReader(System.in);
        boolean validResponse = false;
        char letter = ' ';
        while (!validResponse) {
            System.out.print("Play again? (Yes/No): ");
            letter = (char)br.read();
            if (letter >= 'A' && letter <= 'Z') {
                letter = (char) (letter - 'A' + 'a');
            }
            if (letter == 'y') {
                return true;
            }
            else if (letter == 'n') {
                return false;
            }
            else {
                System.out.println("\nPlease enter a valid response ('yes' or 'no', or just 'y' or 'n').");
            }
        }
        return false;
    }
}