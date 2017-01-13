package Homeworks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import Utils.*;

/**
 * Created by eXetrum on 28.12.2016.
 */
public class Homework2 {
    private static void getFreeVariables(Expression expression, Map<Expression, Integer> counter, Set<Variable> answer) {
        if (expression instanceof Variable) {
            if (!counter.containsKey(expression)) {
                answer.add((Variable) expression);
            }
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            Expression variable = it.getVariable();
            if(!counter.containsKey(variable))
                counter.put(variable, 0);
            Integer oldValue = counter.get(variable);
            counter.put(variable, oldValue + 1);
            getFreeVariables(it.getStatement(), counter, answer);
            counter.put(variable, oldValue);
            if (oldValue == 0) {
                counter.remove(variable);
            }
        } else if (expression instanceof Applicative) {
            Applicative it = (Applicative) expression;
            getFreeVariables(it.getLeft(), counter, answer);
            getFreeVariables(it.getRight(), counter, answer);
        }
    }

    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("TestCases/HW2/task2.in"));
                PrintWriter out = new PrintWriter(new File("TestCases/HW2/task2.out"))
        ) {
            StringBuilder input = new StringBuilder();
            while (in.hasNextLine()) {
                input.append(in.nextLine());
            }

            Parser.LambdaParser parser = new Parser.LambdaParser();
            Expression expression = parser.parse(input.toString().trim());

            Set<Variable> result = new HashSet<>();
            Map<Expression, Integer> counter = new HashMap<>();

            getFreeVariables(expression, counter, result);

            List<String> variables = new ArrayList<>();
            for (Variable var : result) {
                variables.add(var.getName());
            }
            Collections.sort(variables);

            for (String var : variables) {
                out.println(var);
            }
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }
}
