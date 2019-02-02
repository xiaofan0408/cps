package my.parser;

import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.experimental.var;
import my.enu.LexerException;
import my.lexer.Lexer;
import my.lexer.Token;

import java.util.*;
import java.util.function.Function;

import static java.io.File.separator;

/**
 * @author xuzefan  2019/2/2 14:18
 */
public class Parser {

    private final BoolNode FALSE = new BoolNode("", false);
    private Lexer input;
    private Map<String, Integer> PRECEDENCE;

    public Parser(Lexer lexer) {
        this.input = lexer;
        this.PRECEDENCE = new HashMap<>();
        this.PRECEDENCE.put("=", 1);
        this.PRECEDENCE.put("||", 2);
        this.PRECEDENCE.put("&&", 3);
        this.PRECEDENCE.put("<", 7);
        this.PRECEDENCE.put(">", 7);
        this.PRECEDENCE.put("<=", 7);
        this.PRECEDENCE.put(">=", 7);
        this.PRECEDENCE.put("==", 7);
        this.PRECEDENCE.put("!=", 7);
        this.PRECEDENCE.put("+", 10);
        this.PRECEDENCE.put("-", 10);
        this.PRECEDENCE.put("*", 20);
        this.PRECEDENCE.put("/",20);
        this.PRECEDENCE.put("%", 20);
    }

    private Token is_punc(String ch) throws Exception{
        Token tok = input.peek();
        if (tok!=null) {
            if ( tok.getType().equals("punc") && (ch!=null || tok.getValue().equals(ch))){
                return tok;
            }
        }
        return null;
    }
    private Token is_kw(String kw) throws Exception{
        Token tok = input.peek();
        if (tok!=null) {
            if ( tok.getType().equals("kw") && (kw!=null || tok.getValue().equals(kw))){
                return tok;
            }
        }
        return null;
    }
    private Token is_op(String op) throws Exception{
        Token tok = input.peek();
        if (tok!=null) {
            if ( tok.getType().equals("op") && (op!=null || tok.getValue().equals(op))){
                return tok;
            }
        }
        return null;
    }
    private void  skip_punc(String ch)throws Exception {
        if (is_punc(ch)!=null) {
            input.next();
        }
        else {
            input.croak("Expecting punctuation: \"" + ch + "\"");
        }
    }
    private void skip_kw(String kw) throws Exception{
        if (is_kw(kw)!=null) {
            input.next();
        }
        else{
            input.croak("Expecting keyword: \"" + kw + "\"");
        }
    }
    private void  skip_op(String op) throws Exception {
        if (is_op(op)!=null) {
            input.next();
        }
        else {
            input.croak("Expecting operator: \"" + op + "\"");
        }
    }
    private void unexpected()  throws Exception{
        input.croak("Unexpected token: " + JSON.toJSONString(input.peek()));
    }
    private Node maybe_binary(Node left, int my_prec) throws Exception{
        Token tok = is_op(null) ;
        if (tok!=null) {
            int his_prec = PRECEDENCE.get(tok.getValue());
            if (his_prec > my_prec) {
                input.next();
                BinaryNode binaryNode = new BinaryNode();
                binaryNode.setType(tok.getValue().equals("=") ? "assign" : "binary");
                binaryNode.setOperator(tok.getValue());
                binaryNode.setLeft(left);
                binaryNode.setRight(maybe_binary(parse_atom(), his_prec));
                return maybe_binary(binaryNode, my_prec);
            }
        }
        return left;
    }
    private List<Node> delimited (String start,String stop,String separator, NoArgsFunction<Node> parser) throws Exception{
        List<Node> a = new ArrayList<>();
        boolean first = true;
        skip_punc(start);
        while (!input.eof()) {
            if (is_punc(stop)!=null){
                break;
            }
            if (first) {
                first = false;
            } else{
                skip_punc(separator);
            }
            if (is_punc(stop)!=null){
                break;
            }
            a.add(parser.apply());
        }
        skip_punc(stop);
        return a;
    }
    private CallNode parse_call(Node func) {
        CallNode callNode = new CallNode();
        callNode.setType("call");
        callNode.setFunc(func);
        try {
            callNode.setArgs(delimited("(", ")", ",", this::parse_expression));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return callNode;
    }

    public Node parse_varname(){
        Token name = null;
        try {
            name = input.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!name.getType().equals("var")){
            try {
                input.croak("Expecting variable name");
            } catch (LexerException e) {
                e.printStackTrace();
            }
        }
        VarNode varNode = new VarNode();
        varNode.setType("var");
        varNode.setValue(name.getValue());
        return varNode;
    }
    private IfNode parse_if() throws Exception {
        skip_kw("if");
        Node cond = parse_expression();
        if (is_punc("{") == null){
            skip_kw("then");
        }
        Node then = parse_expression();
        IfNode ret = new IfNode();
        ret.setType("if");
        ret.setCond(cond);
        ret.setThen(then);
        if (is_kw("else")!=null) {
            input.next();
            ret.setElsen(parse_expression());
        }
        return ret;
    }
    private LambdaNode parse_lambda() throws Exception {
        LambdaNode lambdaNode = new LambdaNode();
        lambdaNode.setType("lambda");
        lambdaNode.setVars(delimited("(", ")", ",", this::parse_varname));
        lambdaNode.setBody(parse_expression());
        return lambdaNode;
    }
    private BoolNode parse_bool() throws Exception {
        BoolNode boolNode = new BoolNode();
        boolNode.setType("bool");
        boolNode.setValue(Boolean.valueOf(input.next().getValue()));
        return boolNode;
    }
    private Node maybe_call(NoArgsFunction<Node> expr) throws Exception {
        Node exprNode = expr.apply();
        return is_punc("(")!=null ? parse_call(exprNode) : exprNode;
    }
    private Node parse_atom() throws Exception {
        return maybe_call(() ->{
            try {
                if (is_punc("(")!=null) {
                    input.next();
                    Node exp = parse_expression();
                    skip_punc(")");
                    return exp;
                }
                if (is_punc("{")!=null){
                    return parse_prog();
                }
                if (is_kw("if")!=null) {
                    return parse_if();
                }
                if (is_kw("true")!=null || is_kw("false")!=null) {
                    return parse_bool();
                }
                if (is_kw("lambda")!=null || is_kw("λ")!=null) {
                    input.next();
                    return parse_lambda();
                }
                Token tok = input.next();
                if (tok.getType().equals("var") || tok.getType().equals("num") || tok.getType().equals("str")) {
                    return parse_var_num_str(tok);
                }
                unexpected();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }
    private ProgNode parse_toplevel() throws Exception{
        List<Node> prog = new ArrayList<>();
        while (!input.eof()) {
            prog.add(parse_expression());
            if (!input.eof()){
                skip_punc(";");
            }
        }
        ProgNode progNode = new ProgNode();
        progNode.setType("prog");
        progNode.setProg(prog);
        return progNode;
    }
    private Node parse_prog() {
        List<Node> prog = null;
        try {
            prog = delimited("{", "}", ";", this::parse_expression);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (prog.size() == 0) {
            return FALSE;
        }
        if (prog.size() == 1) {
            return prog.get(0);
        }
        ProgNode progNode = new ProgNode();
        progNode.setType("prog");
        progNode.setProg(prog);
        return progNode;
    }
    private Node parse_expression(){
        try {
            return maybe_call(() ->{
                try {
                    return maybe_binary(parse_atom(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Node parse_var_num_str(Token token) {
        switch (token.getType()){
            case "var":{
                VarNode varNode = new VarNode();
                varNode.setType(token.getType());
                varNode.setValue(token.getValue());
                return varNode;
            }case "num": {
                NumNode numNode = new NumNode();
                numNode.setType(token.getType());
                numNode.setValue(Double.parseDouble(token.getValue()));
                return numNode;
            }case "str":{
                StrNode strNode = new StrNode();
                strNode.setType(token.getType());
                strNode.setValue(token.getValue());
                return strNode;
            }
            default:
                return null;
        }
    }

}
