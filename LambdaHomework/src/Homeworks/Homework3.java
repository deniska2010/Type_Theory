package Homeworks;

import Utils.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Homework3 {
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

    private static Expression substitute(Expression expression,
                                               Variable oldLambdaVariable,
                                               Expression replacement,
                                               Map<Expression, Integer> counter,
                                               Set<Variable> replacementFreeVars) throws ParseException {
        if (expression instanceof Variable) {
            if (!counter.containsKey(oldLambdaVariable) && expression.equals(oldLambdaVariable)) {
                for (Variable freeVar : replacementFreeVars) {
                    if (counter.containsKey(freeVar)) {
                        throw new ParseException("Нет свободы для подстановки для переменной " + freeVar);
                    }
                }
                return replacement;
            }
            return expression;
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            Expression itVariable = it.getVariable();
            Expression itStatement = it.getStatement();
            if(!counter.containsKey(itVariable))
                counter.put(itVariable, 0);
            Integer oldValue = counter.get(itVariable);
            counter.put(itVariable, oldValue + 1);
            Expression statementSub = substitute(itStatement, oldLambdaVariable, replacement, counter, replacementFreeVars);
            Expression result = new Abstraction(itVariable, statementSub);
            counter.put(itVariable, oldValue);
            if (oldValue == 0) {
                counter.remove(itVariable);
            }
            return result;
        } else if (expression instanceof Applicative) {
            Applicative it = (Applicative) expression;
            Expression left = it.getLeft();
            Expression right = it.getRight();
            Expression leftSub = substitute(left, oldLambdaVariable, replacement, counter, replacementFreeVars);
            Expression rightSub = substitute(right, oldLambdaVariable, replacement, counter, replacementFreeVars);
            return new Applicative(leftSub, rightSub);
        }
        throw new ParseException("SubstituteException. Unknown type");
    }

    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("TestCases/HW3/task3.in"));
                PrintWriter out = new PrintWriter(new File("TestCases/HW3/task3.out"))
        ) {

            while (in.hasNextLine()) {
                String input = in.nextLine().trim();

                int indexOf = input.indexOf('[');
                String exprStr = input.substring(0, indexOf);
                String subExpr = input.substring(indexOf);

                int index = subExpr.indexOf(":=");
                String varStr = subExpr.substring(1, index).trim();
                String subStr = subExpr.substring(index + 2, subExpr.length() - 1);

                Variable var = new Variable(varStr);

                Parser.LambdaParser parser = new Parser.LambdaParser();
                Expression substitution = parser.parse(subStr);

                Expression expression = parser.parse(exprStr);
                try {
                    Set<Variable> freeVars = new HashSet<>();
                    getFreeVariables(substitution, new HashMap<Expression, Integer>(), freeVars);
                    Expression result = substitute(expression, var, substitution, new HashMap<Expression, Integer>(), freeVars);
                    out.println(result);
                } catch (ParseException e) {
                    out.print(e.getMessage());
                }
            }


        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }
}
