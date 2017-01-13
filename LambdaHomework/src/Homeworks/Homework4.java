package Homeworks;
import Utils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Homework4 {
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

    private static Set<Variable> getFreeVariables(Expression expression) {
        Map<Expression, Integer> counter = new HashMap<>();
        Set<Variable> result = new HashSet<>();
        getFreeVariables(expression, counter, result);
        return result;
    }

    private static Variable getSomeVariable(Set<Variable> busy) {
        for (char c = 'a'; c <= 'z'; c++) {
            Variable it = new Variable(String.valueOf(c));
            if (!busy.contains(it)) {
                return it;
            }
            it = new Variable(String.valueOf(c) + "'");
            if (!busy.contains(it)) {
                return it;
            }
        }
        return new Variable("x''");
    }

    private static Expression substitute(Expression expression,
                                               Variable oldVariable,
                                               Expression replacement) {

        if (expression instanceof Variable) {
            Variable it = (Variable) expression;
            if (it.equals(oldVariable)) {
                return replacement;
            }
            return expression;
        }
        if (expression instanceof Applicative) {
            Applicative it = (Applicative) expression;
            Expression left = it.getLeft();
            Expression right = it.getRight();
            return new Applicative(substitute(left, oldVariable, replacement), substitute(right, oldVariable, replacement));
        }
        if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            Variable itVariable = (Variable) it.getVariable();
            Expression itStatement = it.getStatement();
            if (itVariable.equals(oldVariable) || !getFreeVariables(itStatement).contains(oldVariable)) {
                return expression;
            }
            if (!getFreeVariables(replacement).contains(itVariable)) {
                return new Abstraction(itVariable, substitute(itStatement, oldVariable, replacement));
            }
            Set<Variable> allBusyVars = getFreeVariables(itStatement);
            allBusyVars.addAll(getFreeVariables(replacement));
            Variable someFreeVar = getSomeVariable(allBusyVars);
            Expression afterChange = substitute(itStatement, itVariable, someFreeVar);
            return new Abstraction(someFreeVar, substitute(afterChange, oldVariable, replacement));
        }
        throw new IllegalArgumentException("Unknown type");
    }

    private static Map<Expression, Expression> memory = new HashMap<>();

    private static Map<Expression, Expression> headMemory = new HashMap<>();

    private static void remember(Expression from, Expression to) {
        if (!memory.containsKey(from)) {
            memory.put(from, to);
        }
    }

    private static void rememberHead(Expression from, Expression to) {
        if (!headMemory.containsKey(from)) {
            headMemory.put(from, to);
        }
    }

    private static Expression headNormalForm(Expression expression) {
        if (headMemory.containsKey(expression)) {
            return headMemory.get(expression);
        }
        if (expression instanceof Variable || expression instanceof Abstraction) {
            return expression;
        } else if (expression instanceof Applicative) {
            Applicative it = (Applicative) expression;
            Expression left = it.getLeft();
            Expression right = it.getRight();
            Expression leftNormal = headNormalForm(left);
            if (leftNormal instanceof Abstraction) {
                Abstraction leftIt = (Abstraction) leftNormal;
                Variable leftItLambdaVariable = (Variable) leftIt.getVariable();
                Expression leftItStatement = leftIt.getStatement();
                Expression substitution = substitute(leftItStatement, leftItLambdaVariable, right);

                Expression headNormalForm = /*norm*/headNormalForm(substitution);
                rememberHead(expression, headNormalForm);
                return headNormalForm;
            } else {
                Applicative applicative = new Applicative(/*not simple*/leftNormal, right);
                rememberHead(expression, applicative);
                return applicative;
            }
        }
        throw new IllegalArgumentException("Unknown type");
    }

    private static Expression normalForm(Expression expression) {
        if (memory.containsKey(expression)) {
            return memory.get(expression);
        }
        if (expression instanceof Variable) {
            return expression;
        } else if (expression instanceof Abstraction) {
            Abstraction it = (Abstraction) expression;
            Expression itVariable = it.getVariable();
            Expression itStatement = it.getStatement();
            Abstraction abstraction = new Abstraction(itVariable, normalForm(itStatement));
            remember(expression, abstraction);
            return abstraction;
        } else if (expression instanceof Applicative) {
            Applicative it = (Applicative) expression;
            Expression left = it.getLeft();
            Expression right = it.getRight();
            Expression headLeftNormalForm = headNormalForm(left);
            if (headLeftNormalForm instanceof Abstraction) {
                Abstraction leftIt = (Abstraction) headLeftNormalForm;
                Variable leftItLambdaVariable = (Variable) leftIt.getVariable();
                Expression leftItStatement = leftIt.getStatement();
                Expression substitution = substitute(leftItStatement, leftItLambdaVariable, right);
                Expression normalForm = normalForm(substitution);
                remember(expression, normalForm);
                return normalForm;
            } else {
                Applicative applicative = new Applicative(normalForm(headLeftNormalForm), normalForm(right));
                remember(expression, applicative);
                return applicative;
            }
        }
        throw new IllegalArgumentException("Unknown type");
    }

    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("TestCases/HW4/task4.in"));
                PrintWriter out = new PrintWriter(new File("TestCases/HW4/task4.out"))
        ) {
            StringBuilder input = new StringBuilder();
            while (in.hasNextLine()) {
                input.append(in.nextLine());
            }
            Parser.LambdaParser parser = new Parser.LambdaParser();
            Expression expression = parser.parse(input.toString().trim());
            Expression normalExpression = normalForm(expression);
            out.print(normalExpression);

        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }
}
