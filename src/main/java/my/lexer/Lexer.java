package my.lexer;

import my.enu.LexerException;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xuzefan  2019/2/2 11:49
 */
public class Lexer {

    private TokenInputStream tokenInputStream;

    private Token current = null;
    private String keywords = " if then else lambda λ true false ";

    public Lexer(TokenInputStream tokenInputStream) {
        this.tokenInputStream = tokenInputStream;
    }

    private boolean is_keyword(String x) {
        return keywords.indexOf(" " + x + " ") >= 0;
    }
    private boolean is_digit(String ch) {
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(ch);
        return matcher.matches();
    }
    private boolean is_id_start(String ch) {
        Pattern pattern = Pattern.compile("[a-zλ_]");
        Matcher matcher = pattern.matcher(ch);
        return matcher.matches();
    }
    private boolean is_id(String ch) {
        return is_id_start(ch) || "?!-<>=0123456789".indexOf(ch) >= 0;
    }
    private boolean is_op_char(String ch) {
        return "+-*/%=&|<>!".indexOf(ch) >= 0;
    }
    private boolean is_punc(String ch) {
        return ",;(){}[]".indexOf(ch) >= 0;
    }
    private boolean is_whitespace(String ch) {
        return " \t\n".indexOf(ch) >= 0;
    }
    private String read_while(Function<String,Boolean> predicate) {
        String str = "";
        while (!tokenInputStream.eof() && predicate.apply(tokenInputStream.peek())) {
            str += tokenInputStream.next();
        }
        return str;
    }
    private Token read_number() {
        boolean has_dot = false;
        String number = read_while((String ch) -> {
            if (ch.equals(".")) {
                if (has_dot) {
                    return false;
                }
                return true;
            }
            return is_digit(ch);
        });
        return new Token("num", number);
    }
    private Token read_ident() {
        String id = read_while(this::is_id);
        return new Token(is_keyword(id) ? "kw" : "var", id);
    }
    private String read_escaped(String end) {
        boolean escaped = false;
        String str = "";
        tokenInputStream.next();
        while (!tokenInputStream.eof()) {
            String ch = tokenInputStream.next();
            if (escaped) {
                str += ch;
                escaped = false;
            } else if (ch.equals("\\")) {
                escaped = true;
            } else if (ch.equals(end)) {
                break;
            } else {
                str += ch;
            }
        }
        return str;
    }
    private Token read_string() {
        return new Token("str", read_escaped("\""));
    }

    private void  skip_comment() {
        read_while((ch) ->{ return !ch.equals("\n");});
        tokenInputStream.next();
    }
    private Token read_next() throws Exception {
        read_while(this::is_whitespace);
        if (tokenInputStream.eof()) {
            return null;
        }
        String ch = tokenInputStream.peek();
        if (ch.equals("#")) {
            skip_comment();
            return read_next();
        }
        if (ch.equals("\"")){
            return read_string();
        }
        if (is_digit(ch)) {
            return read_number();
        }
        if (is_id_start(ch)) {
            return read_ident();
        }
        if (is_punc(ch)) {
            return new Token("punc", tokenInputStream.next());
        }

        if (is_op_char(ch)) {
            return new Token("op", read_while(this::is_op_char));
        }
        tokenInputStream.croak("Can't handle character: " + ch);
        return null;
    }
    public Token peek() throws Exception{
        if (current == null) {
            current = read_next();
            return current;
        }
        return null;
    }
    public Token next() throws Exception{
        Token tok = current;
        current = null;
        if (tok == null) {
            tok = read_next();
        }
        return tok;
    }
    public boolean eof()  throws Exception{
        return peek() == null;
    }
    public void croak(String msg) throws LexerException {
        throw new LexerException(msg);
    }
}
