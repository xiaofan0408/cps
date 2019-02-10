package my.parser;

import lombok.Data;

/**
 * @author xuzefan  2019/2/2 14:06
 */
@Data
public class IfNode extends Node {

    private Node cond;

    private Node then;

    private Node elsen;
}
