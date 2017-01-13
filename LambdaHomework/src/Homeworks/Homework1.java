package Homeworks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import Utils.*;

/**
 * Created by eXetrum on 28.12.2016.
 */
public class Homework1 {
    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("TestCases/HW1/task1.in"));
                PrintWriter out = new PrintWriter(new File("TestCases/HW1/task1.out"))
        ) {
            String input = getInput(in);
            Parser.LambdaParser parser = new Parser.LambdaParser();
            Expression expression = parser.parse(input);
            out.print(expression);
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static String getInput(Scanner in) {
        StringBuilder input = new StringBuilder();
        while (in.hasNextLine()) {
            input.append(in.nextLine());
        }
        return input.toString().trim();
    }
}
