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
    private static final List<String> INPUT_LETTERS = new ArrayList<>();//TODO Character
    private static final int MAX_COUNT_MISTAKE = 6;
    private static final String MESSAGE_ERROR = "ПРОИЗОШЛА ОШИБКА: ";
    private static final String MESSAGE_LETTER_INPUT = "ВВЕДИТЕ БУКВУ И НАЖМИТЕ ENTER: ";
    private static final String MESSAGE_REPEAT_INPUT = "ОШИБКА ВВОДА: ПОВТОР БУКВЫ - ВЫ УЖЕ ВВОДИЛИ ЭТУК БУКВУ! ";
    private static final String MESSAGE_NOT_RU_LETTER_INPUT = "ОШИБКА ВВОДА: НЕВЕРНЫЙ ФОРМАТ - ЭТО НЕ БУКВА РУССКОГО ЯЗЫКА! ";

    private static String word;
    private static char[] wordLetters; //TODO излишне, обращаться к word.charAt(index)
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
        printSecurityWordLetters();

        while (!isGameOver()) {
            System.out.print(MESSAGE_LETTER_INPUT);
            char letter = inputRuLetter();
            addUsedLetters(letter);
            setCountMistake(letter);
            addLetterSecurityWordLetters(letter);
            printMessageInfo();
            Picture.printPicture(countMistake);

            if (isWin()) {
                printWinMessage();
            } else if (isLose()) {
                printLoseMessage();
            } else {
                printSecurityWordLetters();
            }
        }
    }

    private static boolean isGameOver() {
        return isWin() || isLose(); // countMistake > MAX_COUNT_MISTAKE;
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
        String securityWord = new String(securityWordLetters);
        System.out.println("ЗАГАДАННОЕ СЛОВО: " + securityWord);
    }

    private static void addLetterSecurityWordLetters(char inputLetter) {
        for (int i = 0; i < wordLetters.length; i++) {
            if (wordLetters[i] == inputLetter) {
                securityWordLetters[i] = inputLetter;
            }
        }
    }

    private static char inputRuLetter() {
        //TODO должен возвращать любую ру одиночную букву - строка состоит из 1 символа и символ ру буква
        while (true) {
            String input = SCANNER.next().toUpperCase();

            if (isValidInput(input)) {
                return input.charAt(0);
            }
            printMessageInputError(input);
        }
    }

    private static boolean isValidInput(String value) {
        return isRussianStringInput(value) && !isRepeatInput(value);
    }

    private static boolean isRepeatInput(String value) {
        return INPUT_LETTERS.contains(value);
    }

    private static boolean isRussianStringInput(String value) {
        Matcher matcher = PATTERN.matcher(value);
        return matcher.find();
    }

    private static void printMessageInputError(String value) {
        if (isRepeatInput(value)) {
            System.out.print(MESSAGE_REPEAT_INPUT + MESSAGE_LETTER_INPUT);
        } else if (!isRussianStringInput(value)) {
            System.out.print(MESSAGE_NOT_RU_LETTER_INPUT + MESSAGE_LETTER_INPUT);
        }
    }

    private static void setCountMistake(char letter) {
        boolean isLetterContainsWord = false;
        for (char wordLetters : wordLetters) {
            if (wordLetters == Character.toUpperCase(letter)) {
                isLetterContainsWord = true;
            }
        }
        if (!isLetterContainsWord) {
            countMistake++;
        }
    }

    private static void addUsedLetters(char inputLetter) {
        INPUT_LETTERS.add(Character.toString(inputLetter));
    }

    private static void printMessageInfo() {
        System.out.printf("ОШИБКИ : %d/%d. ИСПОЛЬЗОВАННЫЕ БУКВЫ: %s\n", countMistake, MAX_COUNT_MISTAKE, showUsedLetters());
    }

    private static String showUsedLetters() {
        return String.join(", ", INPUT_LETTERS);
    }

}