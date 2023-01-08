package test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Searcher {

    private final String directory;
    private final List<File> txtFilesList = new ArrayList<>();
    private final String resultPath;


    public Searcher(String directory) {
        this.directory = directory;
        this.resultPath = directory + "\\result.txt";
    }

    public static void main(String[] args) {

        String directory = consoleInput();

        Searcher searcher = new Searcher(directory);

        File file = new File(searcher.directory);
        searcher.search(file);


        searcher.readWrite();

        System.out.printf("Всего найдено текстовых файлов : %d, данные из которых были записаны " +
                "в результирующий файл: %s%n", searcher.txtFilesList.size(), searcher.resultPath);
    }


    public static String consoleInput() {
        String directory = null;
        try {
            BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Пожалуйста введите директорию для поиска!");
            directory = bufferedInputStream.readLine().trim();
            if (directory.equals("exit")) System.exit(0);

            File file = new File(directory);
            if (!file.exists()) throw new FileNotFoundException();


        } catch (Exception ex) {

            if (ex.getClass().getSimpleName().equals("FileNotFoundException")) {
                System.out.println("Вы ввели некорректную директорию, пожалуйста " +
                        "проверьте данные. Для завершения работы программы введите: exit");
                return consoleInput();
            } else ex.printStackTrace();
        }

        return directory;
    }

    public void search(File directory, boolean... low) {
        if (directory.isDirectory()) {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile() && (file.getName().endsWith(".txt") || file.getName().endsWith(".TXT"))) {
                    if (low.length == 0) {
                        txtFilesList.add(0, file);
                    } else {
                        txtFilesList.add(file);
                    }
                    Optional<String> path = read(file);
                    if (path.isPresent()) {
                        Optional<String> pathAnother = read(new File(path.get()));
                        if (pathAnother.isPresent() && pathAnother.get().equals(path.get())) {
                            System.out.println("Обнаружена циклическая зависимость");
                            return;
                        } else {
                            search(new File(path.get()), true);
                        }
                    }
                } else {
                    search(file);
                }
            }
        }
        if (txtFilesList.size() == 0) {
            System.out.println("В указанной вами директории файлов формата \".txt\" не найдено, " +
                    "файл результата не изменён.");
            System.exit(0);
        }
    }

    public Optional<String> read(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file.toString()))) {
            while (br.ready()) {
                String data = br.readLine();
                if (data.contains("require")) {
                    return Optional.of(data.split(" ")[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void readWrite() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.resultPath));
            for (File file : this.txtFilesList) {
                try (BufferedReader br = new BufferedReader(new FileReader(file.toString()))) {
                    while (br.ready()) {
                        bw.write(br.readLine());
                        bw.newLine();
                    }
                }
            }
            bw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}