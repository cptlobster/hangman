import java.io.IOException;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args) {
        // TODO: setup sockets
        IOManager sp = new IOManager();
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
        System.out.println(states[10 - guesses]);
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
        System.out.println(states[10 - guesses]);
        System.out.printf("You win! You guessed the word \"%s\" with %d attempts left.%n", word, guesses);
    }

    void lose(String word) {
        System.out.println(states[10]);
        System.out.printf("You lose. The word was \"%s\".%n", word);
    }

    void guessResult(int retval) {
        switch (retval) {
            case 0: {
                System.out.println("Letter not present.");
            }
            case -1: {
                System.out.println("You already guessed this letter.");
            }
            default: {
                System.out.printf("Letter present %d times.%n", retval);
            }
        }
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
}