package my;

import my.lexer.Lexer;
import my.lexer.TokenInputStream;
import my.parser.Parser;
import my.parser.ProgNode;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        String code = "sum = lambda(x, y) x + y; println(sum(2, 3));";
        TokenInputStream tokenInputStream = new TokenInputStream(code);
        Lexer lexer = new Lexer(tokenInputStream);
        Parser parser = new Parser(lexer);
        ProgNode progNode = parser.parse_toplevel();
        System.out.println(progNode);
    }
}
