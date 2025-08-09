package ru.trashkova.gallows;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class GallowsApplication {

    private static final String FILE_PATH = "resource/modified_word.txt";
    private static String WORD;
    private static char[] WORD_LETTERS;
    private static char[] SECURITY_WORD_LETTERS; //securityWordArray
    private static char INPUT_LETTER;
    private static final List<String> INPUT_LETTERS = new ArrayList<>();
    private static int COUNT_MISTAKE = 0;
    private static final int MAX_COUNT_MISTAKE = 6;

    public static void main(String[] args) {
        //modifiedFile();
        startGameRound();
    }

    /**
     * Файл с топ-1000 существительных РЯ. В исходном файле он пронумерован по строкам -> перезаписываем слова в новый файл без порядковых номеров и пробелов (табуляции).
     */
    public static void modifiedFile () {
        String fileName = "resource/word.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int indexSpaceLine = line.indexOf("\t");
                if (indexSpaceLine != -1) {
                    String modifiedLine = line.substring(indexSpaceLine+1);
                    writer.write(modifiedLine);
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("ПРОИЗОШЛА ОШИБКА: " + e.getMessage());
        }
    }

    public static void startGameRound() {
        showGameDescription();
        showGameRules();
        showGameStart();
        startGameLoop();
    }

    /**
     * Описание игры
     */
    public static void showGameDescription() {
        System.out.println("***");
        System.out.println("ОПИСАНИЕ ИГРЫ.");
        System.out.println("«Виселица» - это игра в слова.");
        System.out.println("Один игрок загадывает слово, а второй игрок пытается его отгадать, предлагая свой вариант по одной букве.");
        System.out.println("За каждую неверную букву, к виселице добавлять часть тела. Цель игры – отгадать слово до того, как висельник будет повешен.\n");
    }

    /**
     * Правила игры
     */
    public static void showGameRules() {
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

    /**
     * Информация о начале игры
     */
    public static void showGameStart() {
        System.out.println("***");
        System.out.println("Начало игры.");
        System.out.println("Подождите, сейчас программа загадает слово…");
        //TODO добавить паузу 3сек
        System.out.println("***");
    }

    /**
     * Логика игры
     */
    public static void startGameLoop() {
        WORD = getWord();
        WORD_LETTERS = getWordLetters(WORD);
        SECURITY_WORD_LETTERS = getSecurityWordLetters(WORD);

        while(COUNT_MISTAKE < MAX_COUNT_MISTAKE) {
            System.out.print("ВВЕДИТЕ БУКВУ И НАЖМИТЕ ENTER: ");
            INPUT_LETTER = getLetterUser();
            addUsedLetters(INPUT_LETTER);
            COUNT_MISTAKE = getCountMistake(INPUT_LETTER);
            SECURITY_WORD_LETTERS = getSecurityWordLetters(INPUT_LETTER);
            printMessageInfo();
            drowGallows(COUNT_MISTAKE);

            if (COUNT_MISTAKE != MAX_COUNT_MISTAKE) {
                showSecurityWordLetters(); // чтобы не печатало засекреченное слово, когда кол-во ошибок 6, а сразу печать, что игра закончилась
            } else System.out.printf("ПОПЫТКИ ЗАКОНЧАЛИСЬ, ВЫ ПРОИГРАЛИ! ЗАГАДАННОЕ СЛОВО - %s.", WORD);

            if (Arrays.equals(SECURITY_WORD_LETTERS, WORD_LETTERS)) {
                System.out.println("ВЫ ВЫЙГРАЛИ! ПОЗДРАВЛЯЕМ!");
                break;
            }
        }
    }

    /**
     * Получение случайного слова из текстового файла: посчитать кол-во строк в файле -> сгенерировать случайное число из диапазона -> достать слово
     */
    public static String getWord() {
        long countLine = getAllCountLineFile();
        long randomCountLine = getRandomCountLineFile(countLine);
        int targetLineNumber = Math.toIntExact(randomCountLine);
        String word;
        try (LineNumberReader reader = new LineNumberReader(new FileReader(FILE_PATH))) {
            while ((word = reader.readLine()) != null) {
                if (reader.getLineNumber() == targetLineNumber) {
                    word = word.toUpperCase();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return word;
    }

    /**
     * Количество строк в текстовой файле
     */
    public static long getAllCountLineFile() {
        long countLine;
        try (Stream<String> lines = Files.lines(Paths.get(FILE_PATH))) {
            countLine = lines.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return countLine;
    }

    /**
     * Получение случайного номера строки (от 1 до countLine = 1000 в нашем файле)
     */
    public static long getRandomCountLineFile (long value) {
        Random rand = new Random();
        return rand.nextLong(value) + 1;
    }

    /**
     * Получения из слова массива символов
     */
    public  static char[] getWordLetters(String word) {
        return word.toUpperCase().toCharArray();
    }

    /**
     * Инициализация массива для пользователя: вместо букв символ нижнее подчеркивание
     */
    public  static char[] getSecurityWordLetters(String word) {
        char[] securityWordArray = new char[word.length()];
        Arrays.fill(securityWordArray, '_');
        System.out.print("ЗАГАДАННОЕ СЛОВО: ");
        System.out.println(securityWordArray);
        return securityWordArray;
    }

    /**
     * Заполнение массива для пользователя вводимыми пользователем букв
     */
    public  static char[] getSecurityWordLetters(char inputLetter) {
        for (int i = 0; i < WORD_LETTERS.length; i++) {
            if (WORD_LETTERS[i] == inputLetter) {
                SECURITY_WORD_LETTERS[i] = inputLetter;
            }
        }
        return SECURITY_WORD_LETTERS;
    }

    /**
     * Получение буквы из консоли от пользователя
     */
    public static char getLetterUser() {
        Scanner scanner = new Scanner(System.in);
        try {
            while(true) {
                String input = scanner.next().toUpperCase();
                if (isCorrectedInput(input)) {
                    INPUT_LETTER = input.charAt(0);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("ПРОИЗОШЛА ОШИБКА: " + e.getMessage());
        }
        return INPUT_LETTER;
    }

    /**
     * Проверка корректности ввода: 1.пустота 2.повтор 3.неверный формат
     */
    public static boolean isCorrectedInput(String value) {
        boolean inputResult = true;
        if (value.isBlank()) {
            System.out.print("Ошибка ввода. Пустое значение – вы ничего не ввели! Введите букву и нажмите enter: ");
            inputResult = false;
        } else if (INPUT_LETTERS.contains(value)) {
            System.out.print("Ошибка ввода. Повтор буквы – вы вводили уже эту букву! Введите букву и нажмите enter: ");
            inputResult = false;
        } else if (!value.matches("[а-яА-Я]")) {
            System.out.print("Ошибка ввода. Неверный формат - это не буква русского языка! Введите букву и нажмите enter: ");
            inputResult = false;
        }
        return inputResult;
    }

    /**
     * Счетчик ошибок пользователя
     */
    public static int getCountMistake(char letter) {
        boolean isMatchValueInArray = false;
        for (char wordLetters : WORD_LETTERS) {
            if (wordLetters == Character.toUpperCase(letter)) {
                isMatchValueInArray = true;
            }
        }
        if (!isMatchValueInArray) COUNT_MISTAKE++;
        return COUNT_MISTAKE;
    }

    /**
     * Добавление буквы в список уже использованных букв ввода пользователя
     */
    public static void addUsedLetters(char inputLetter) {
        INPUT_LETTERS.add(Character.toString(inputLetter));
    }

    /**
     * Информационное сообщение о кол-ве ошибок и букв, которые были использованы игроком
     */
    public static void printMessageInfo() {
        System.out.printf("ОШИБКИ : %d/%d. ИСПОЛЬЗОВАННЫЕ БУКВЫ: %s\n", COUNT_MISTAKE, MAX_COUNT_MISTAKE, showUsedLetters());
    }

    /**
     * Отображение использованных букв пользователя
     */
    public static String showUsedLetters() {
        return String.join(", ", INPUT_LETTERS);
    }

    /**
     * Вывод засекреченного слова для пользователя
     */
    public static void showSecurityWordLetters () {
        System.out.print("СЛОВО: ");
        System.out.println(SECURITY_WORD_LETTERS);
    }

    /**
     * Рисование виселицы
     */
    public static void drowGallows(int contMistake) {
        String conditionGallows = "_______\n|\n|\n|\n|\n";
        String conditionHead = "_______\n|   ()\n|\n|\n|\n";
        String conditionBody = "_______\n|   ()\n|   []\n|\n|\n";
        String conditionLeftHand = "_______\n|   ()\n|  /[]\n|\n|\n";
        String conditionRightHand = "_______\n|   ()\n|  /[]\\\n|\n|\n";
        String conditionLeftLeg = "_______\n|   ()\n|  /[]\\\n|  _/\n|\n";
        String conditionRightLeg = "_______\n|   ()\n|  /[]\\\n|  _/\\_\n|\n";
        switch(contMistake) {
            case 0:
                System.out.println(conditionGallows);
                break;
            case 1:
                System.out.println(conditionHead);
                break;
            case 2:
                System.out.println(conditionBody);
                break;
            case 3:
                System.out.println(conditionLeftHand);
                break;
            case 4:
                System.out.println(conditionRightHand);
                break;
            case 5:
                System.out.println(conditionLeftLeg);
                break;
            case 6:
                System.out.println(conditionRightLeg);
                break;
        }
    }


}