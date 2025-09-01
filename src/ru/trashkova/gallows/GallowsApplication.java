package ru.trashkova.gallows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GallowsApplication {

    private static final Pattern PATTERN = Pattern.compile("[а-яА-ЯёЁ]");
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String FILE_PATH = "resource/words.txt";
    private static final List<Character> USED_LETTERS = new ArrayList<>();
    private static final int MAX_COUNT_MISTAKE = 6;
    private static final String MESSAGE_ERROR = "ПРОИЗОШЛА ОШИБКА: ";

    private static String word;
    private static char[] wordLetters;
    private static char[] securityWordLetters;
    private static int countMistake = 0;

    public static void main(String[] args) {
        startGameRound();
    }

    private static void startGameRound() {
        DescriptionGame.printGameInfo();
        startGameLoop();
    }

    private static void startGameLoop() {
        word = getWord();
        wordLetters = toLetters(word);
        initSecurityWordLetters(word);

        while (!isGameOver()) {
            printTurnInfo();
            char letter = inputRuLetter();
            if(isUsedLetter(letter)) {
                System.out.println("ОШИБКА ВВОДА: ПОВТОР БУКВЫ - ВЫ УЖЕ ВВОДИЛИ ЭТУК БУКВУ! ");
                continue;
            }
            addUsedLetter(letter);
            if(!isWordLetter(letter)) {
                System.out.println("ТАКОЙ БУКВЫ НЕТ В СЛОВЕ!");
                countMistake++;
            } else {
                openLetter(letter);
            }

            if (isWin()) {
                printWinMessage();
            } else if (isLose()) {
                printTurnInfo();
                printLoseMessage();
            }
        }
    }

    private static boolean isGameOver() {
        return isWin() || isLose();
    }

    private static boolean isWin() {
        return Arrays.equals(securityWordLetters, wordLetters);
    }

    private static boolean isLose() {
        return countMistake == MAX_COUNT_MISTAKE;
    }

    private static void printWinMessage() {
        System.out.println("ВЫ ВЫЙГРАЛИ! ПОЗДРАВЛЯЕМ!");

    }

    private static void printLoseMessage() {
        System.out.printf("ПОПЫТКИ ЗАКОНЧАЛИСЬ, ВЫ ПРОИГРАЛИ! ЗАГАДАННОЕ СЛОВО - %s.", word);
    }

    private static void printTurnInfo() {
        Picture.printPicture(countMistake);
        printSecurityWordLetters();
        printMessageInfo();
    }

    private static String getWord() {
        long countLine = getAllCountLineFile();
        long randomCountLine = getRandomCountLineFile(countLine);
        int targetLineNumber = Math.toIntExact(randomCountLine);
        String word = "";
        try (LineNumberReader reader = new LineNumberReader(new FileReader(FILE_PATH))) {
            while ((word = reader.readLine()) != null) {
                if (reader.getLineNumber() == targetLineNumber) {
                    word = word.toUpperCase();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(MESSAGE_ERROR + e.getMessage());
        }
        return word;
    }

    private static long getAllCountLineFile() {
        long countLine = 0;
        try (Stream<String> lines = Files.lines(Paths.get(FILE_PATH))) {
            countLine = lines.count();
        } catch (IOException e) {
            System.out.println(MESSAGE_ERROR + e.getMessage());
        }
        return countLine;
    }

    private static long getRandomCountLineFile(long value) {
        Random rand = new Random();
        return rand.nextLong(value) + 1;
    }

    private static char[] toLetters(String word) {
        return word.toUpperCase().toCharArray();
    }

    private static void initSecurityWordLetters(String word) {
        securityWordLetters = new char[word.length()];
        Arrays.fill(securityWordLetters, '_');
    }

    private static void printSecurityWordLetters() {
        String s = new String(securityWordLetters);
        System.out.println("ЗАГАДАННОЕ СЛОВО: " + s);
    }

    private static void openLetter(char letter) {
        for(int i = 0; i < wordLetters.length; i++) {
            if (wordLetters[i] == letter) {
                securityWordLetters[i] = letter;
            }
        }
    }

    private static char inputRuLetter() {

        while (true) {
            System.out.print("ВВЕДИТЕ БУКВУ И НАЖМИТЕ ENTER: ");
            String input = SCANNER.next().toUpperCase();
            if(!isSingleSymbol(input)) {
                System.out.println("ОШИБКА ВВОДА: ЭТО НЕОДИЧНАЯ БУКВА ");
            } else if(!isRuLetter(input.charAt(0))) {
                System.out.println("ОШИБКА ВВОДА: ЭТО НЕ БУКВА РУССКОГО ЯЗЫКА! ");
            } else {
                return input.charAt(0);
            }
        }
    }

    private static boolean isSingleSymbol(String value) {
        return value.length() == 1;
    }

    private static boolean isUsedLetter(char value) {
        return USED_LETTERS.contains(value);
    }

    private static boolean isWordLetter(char letter) {

        for(char c : wordLetters) {
            if (c == letter) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRuLetter(char value) {
        Matcher matcher = PATTERN.matcher(Character.toString(value));
        return matcher.find();
    }

    private static void addUsedLetter(char letter) {
        USED_LETTERS.add(letter);
    }

    private static void printMessageInfo() {
        System.out.printf("ОШИБКИ : %d/%d. ИСПОЛЬЗОВАННЫЕ БУКВЫ: %s\n", countMistake, MAX_COUNT_MISTAKE, toUserLetters());
    }

    private static String toUserLetters() {
        StringJoiner usedLetters = new StringJoiner(",");
        for (Character letter : USED_LETTERS) {
            usedLetters.add(letter.toString());
        }
        return usedLetters.toString();
    }

}