package my.parser;

import lombok.Data;

/**
 * @author xuzefan  2019/2/2 14:10
 */
@Data
public class BinaryNode extends Node{
    private  String operator;
    private  Node left;
    private  Node right;
}
