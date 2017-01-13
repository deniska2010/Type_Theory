package Utils;

enum Token {
    LEFT_PARENT("("),
    RIGHT_PARENT(")"),
    DOT("."),
    COMMA(","),
    SINGLE_QUOTE("\'"),
    LAMBDA("\\"),
    EQUALS("::="),
    LOWER_LETTER("low"),
    UPPER_LETTER("high"),
    END("eof");

    Token(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getName() {
        return tokenName;
    }

    private String tokenName;
}
