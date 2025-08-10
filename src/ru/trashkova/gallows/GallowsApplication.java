package ru.trashkova.gallows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GallowsApplication {

    private static final Pattern pattern = Pattern.compile("[а-яА-ЯёЁ]");
    private static final Scanner scanner = new Scanner(System.in);

    private static final String FILE_PATH = "resource/words.txt";
    private static String word;
    private static char[] wordLetters;
    private static char[] securityWordLetters;
    private static char inputLetter;
    private static final List<String> INPUT_LETTERS = new ArrayList<>();

    private static int countMistake = 0;
    private static final int MAX_COUNT_MISTAKE = 6;

    private static final String MESSAGE_ERROR = "ПРОИЗОШЛА ОШИБКА: ";
    private static final String MESSAGE_LETTER_INPUT = "ВВЕДИТЕ БУКВУ И НАЖМИТЕ ENTER: ";
    private static final String MESSAGE_EMPTY_INPUT = "ОШИБКА ВВОДА: ПУСТОЕ ЗНАЧЕНИЕ - ВЫ НИЧЕГО НЕ ВВЕЛИ! ";
    private static final String MESSAGE_REPEAT_INPUT = "ОШИБКА ВВОДА: ПОВТОР БУКВЫ - ВЫ УЖЕ ВВОДИЛИ ЭТУК БУКВУ! ";
    private static final String MESSAGE_NOT_RU_LETTER_INPUT = "ОШИБКА ВВОДА: НЕВЕРНЫЙ ФОРМАТ - ЭТО НЕ БУКВА РУССКОГО ЯЗЫКА! ";


    public static void main(String[] args) {
        startGameRound();
    }

    private static void startGameRound() {
        showGameDescription();
        showGameRules();
        showGameStart();
        startGameLoop();
    }

    private static void showGameDescription() {
        System.out.println("***");
        System.out.println("ОПИСАНИЕ ИГРЫ.");
        System.out.println("«Виселица» - это игра в слова.");
        System.out.println("Один игрок загадывает слово, а второй игрок пытается его отгадать, предлагая свой вариант по одной букве.");
        System.out.println("За каждую неверную букву, к виселице добавлять часть тела. Цель игры – отгадать слово до того, как висельник будет повешен.\n");
    }

    private static void showGameRules() {
        System.out.println("***");
        System.out.println("ПРАВИЛА ИГРЫ.");
        System.out.println("""
                1. ПРОГРАММА загадывает случайное слово русского языка.
                   Слово – имя существительное в единственном числе или множественном числе при отсутствии у слова формы единственного числа.
                   ПОЛЬЗОВАТЕЛЬ будет видеть количество букв загаданного слова (каждая буква загаданного слова засекречена и представлена в виде символа “_”).""");
        System.out.println("2. ПОЛЬЗОВАТЕЛЬ угадывает слово по буквам.");
        System.out.println("3. Если такая буква есть, ПРОГРАММА показывает ПОЛЬЗОВАТЕЛЮ позиции буквы в загаданном слове.");
        System.out.println("""
                4. Всего у ПОЛЬЗОВАТЕЛЯ будет 6 попыток отгадать слово.
                   За каждый неправильный ответ счетчик попыток увеличивается, и ПРОГРАММА добавлять к виселице одну из 6 части тела: голова, тело, 2 руки и 2 ноги.
                   Если на виселице тело нарисовано полностью, ПОЛЬЗОВАТЕЛЬ проигрывает и считается повешенным.
                """);
    }

    private static void showGameStart() {
        System.out.println("***");
        System.out.println("Начало игры.");
        System.out.println("Подождите, сейчас программа загадает слово…");
        System.out.println("***");
    }

    private static void startGameLoop() {
        word = getWord();
        wordLetters = toLetters(word);
        initSecurityWordLetters(word);
        printSecurityWordLetters();

        while(!isGameOver()) {
            System.out.print(MESSAGE_LETTER_INPUT);
            setLetterUser();
            addUsedLetters(inputLetter);
            setCountMistake(inputLetter);
            addLetterSecurityWordLetters(inputLetter);
            printMessageInfo();
            Picture.printPicture(countMistake);

            if(isWin()) {
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

    private static long getRandomCountLineFile (long value) {
        Random rand = new Random();
        return rand.nextLong(value) + 1;
    }

    private  static char[] toLetters(String word) {
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

    private static void setLetterUser() {
        try {
            while(true) {
                String input = scanner.next().toUpperCase();
                if (isValidInput(input)) {
                    inputLetter = input.charAt(0);
                    break;
                } else {
                    printMessageInputError(input);
                }
            }
        } catch (Exception e) {
            System.out.println(MESSAGE_ERROR + e.getMessage());
        }
    }

    private static boolean isValidInput(String value) {
        return !isEmptyInput(value) && isRussianLetterInput(value) && !isRepeatInput(value);
    }

    private static boolean isEmptyInput(String value) {
        return value.isBlank();
    }

    private static boolean isRepeatInput(String value) {
        return INPUT_LETTERS.contains(value);
    }

    private static boolean isRussianLetterInput(String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    private static void printMessageInputError(String value) {
        if(isEmptyInput(value)) {
            System.out.print(MESSAGE_EMPTY_INPUT + MESSAGE_LETTER_INPUT);
        } else if(isRepeatInput(value)) {
            System.out.print(MESSAGE_REPEAT_INPUT + MESSAGE_LETTER_INPUT);
        } else if(!isRussianLetterInput(value)) {
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