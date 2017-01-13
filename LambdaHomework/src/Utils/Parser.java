package Utils;


import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static class TermParser {
        private String expression;
        private int position;

        private Token currentToken;
        private String currentString;

        public Expression parse(String string) throws ParseException {
            expression = string;
            position = 0;
            currentToken = nextToken();
            return readTerm();
        }

        private Token nextToken() throws ParseException {
            while (Character.isWhitespace(currentChar())) {
                position++;
            }
            if (position >= expression.length()) {
                return Token.END;
            }
            char oldChar = currentChar();
            position++;
            for (Token token : Token.values()) {
                if (token.getName().equals(Character.toString(oldChar))) {
                    return token;
                }
            }
            if (Character.isLowerCase(oldChar)) {
                currentString = String.valueOf(oldChar);
                while (Character.isLowerCase(currentChar()) || Character.isDigit(currentChar())) {
                    currentString += currentChar();
                    position++;
                }
                return Token.LOWER_LETTER;
            }
            if (Character.isUpperCase(oldChar)) {
                currentString = String.valueOf(oldChar);
                while (Character.isUpperCase(currentChar()) || Character.isDigit(currentChar())) {
                    currentString += currentChar();
                    position++;
                }
                return Token.UPPER_LETTER;
            }
            throw new ParseException("IllegalCharacterException", position);
        }

        private char currentChar() {
            //$ - impossible symbol in parsing
            return position >= expression.length() ? '$' : expression.charAt(position);
        }

        private Expression readTerm() throws ParseException {
            //(Терм) = (Функция) '(' (Терм) {',' (Терм) }* | (Переменная)
            if (currentToken != Token.LOWER_LETTER) {
                throw new ParseException("Expected identifier but found " + currentString);
            }
            char firstChar = currentString.charAt(0);
            if (firstChar < 'a' || firstChar > 'z') {
                throw new ParseException("Expected lower letter but found" + currentString);
            }
            if ('a' <= firstChar && firstChar <= 'h') {
                String functionName = currentString;
                currentToken = nextToken();
                List<Expression> args = new ArrayList<>();
                if (currentToken == Token.LEFT_PARENT) {
                    currentToken = nextToken();
                    Expression nextArg = readTerm();
                    args.add(nextArg);
                    while (currentToken == Token.COMMA) {
                        currentToken = nextToken();
                        nextArg = readTerm();
                        args.add(nextArg);
                    }
                    if (currentToken != Token.RIGHT_PARENT) {
                        throw new ParseException("Expected ')'");
                    }
                    currentToken = nextToken();
                }
                return new Function(args, functionName);
            }
            String variableName = currentString;
            currentToken = nextToken();
            return new Variable(variableName);
        }
    }


    public static class LambdaParser {
        private String expression;
        private int position;

        private Token currentToken;
        private String currentString;

        public Expression parse(String string) throws ParseException {
            expression = string;
            position = 0;
            currentToken = nextToken();
            return readExpression();
        }

        private Token nextToken() throws ParseException {
            while (Character.isWhitespace(currentChar())) {
                position++;
            }
            if (position >= expression.length()) {
                return Token.END;
            }
            char oldChar = currentChar();
            position++;
            for (Token token : Token.values()) {
                if (token.getName().equals(Character.toString(oldChar))) {
                    return token;
                }
            }
            if (Character.isLowerCase(oldChar)) {
                currentString = String.valueOf(oldChar);
                while (Character.isLowerCase(currentChar()) || Character.isDigit(currentChar())) {
                    currentString += currentChar();
                    position++;
                }
                return Token.LOWER_LETTER;
            }
            if (Character.isUpperCase(oldChar)) {
                currentString = String.valueOf(oldChar);
                while (Character.isUpperCase(currentChar()) || Character.isDigit(currentChar())) {
                    currentString += currentChar();
                    position++;
                }
                return Token.UPPER_LETTER;
            }
            throw new ParseException("IllegalCharacterException", position);
        }

        private char currentChar() {
            //$ - impossible symbol in parsing
            return position >= expression.length() ? '$' : expression.charAt(position);
        }

        private Expression readVariable() throws ParseException {
            //Переменная = ('a'..'z'){'a'...'z'| '0'..'9' | \'}*
            if (currentToken != Token.LOWER_LETTER) {
                throw new ParseException("IllegalCharacterException", position);
            }
            String varName = currentString;
            currentToken = nextToken();
            return new Variable(varName);
        }

        private Expression readAtom() throws ParseException {
            //(атом) = '(' (выражение) ')' | (Переменная)
            if (currentToken == Token.LEFT_PARENT) {
                currentToken = nextToken();
                Expression result = readExpression();
                if (currentToken != Token.RIGHT_PARENT) {
                    throw new ParseException(") expected but found " + currentString);
                }
                currentToken = nextToken();
                return result;
            }
            return readVariable();
        }

        private Expression readApplicative() throws ParseException {
            //(применение) ::= (применение) (атом) | (атом)
            Expression result = readAtom();
            while (currentToken == Token.LOWER_LETTER || currentToken == Token.LEFT_PARENT) {
                Expression nextAtom = readAtom();
                result = new Applicative(result, nextAtom);
            }
            return result;
        }

        private Expression readAbstraction() throws ParseException {
            //(абстракция) ::= (переменная) '.' (Выражение)
            Expression variable = readVariable();
            if (currentToken != Token.DOT) {
                throw new ParseException("Expected dot '.' but found " + currentString);
            }
            currentToken = nextToken();
            Expression expression = readExpression();
            return new Abstraction(variable, expression);
        }

        private Expression readExpression() throws ParseException {
            //(выражение)  ::= [(применение)] '\' (абстракция) | (применение)
            if (currentToken == Token.LAMBDA) {
                currentToken = nextToken();
                return readAbstraction();
            }
            Expression result = readApplicative();
            if (currentToken == Token.LAMBDA) {
                currentToken = nextToken();
                Expression abstraction = readAbstraction();
                result = new Applicative(result, abstraction);
            }
            return result;
        }
    }
}
