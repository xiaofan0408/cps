package my.lexer;

import my.enu.LexerException;

/**
 * @author xuzefan  2019/2/2 11:40
 */
public class TokenInputStream {
    private int  pos = 0;

    private int line = 1;

    private int col = 0;

    private String input;

    public TokenInputStream(String input) {
        this.input = input;
    }

    public String next() {
        if (pos >= this.input.length()) {
            return "";
        }
        Character ch = this.input.charAt(pos++);
        if (ch == '\n'){
            line++;
            col = 0;
        }else{
            col++;
        }
        return ch.toString();
    }

    public String peek() {
        if (pos >= this.input.length()) {
            return "";
        }
        Character ch =this.input.charAt(pos);
        return ch.toString();
    }
    public boolean eof() {
        return peek() == "";
    }
    public void croak(String msg) throws LexerException {
        throw new LexerException(msg + " (" + line + ":" + col + ")");
    }
}
