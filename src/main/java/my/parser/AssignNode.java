package my.parser;

import lombok.Data;

/**
 * @author xuzefan  2019/2/2 14:08
 */
@Data
public class AssignNode extends Node{
    private  Node operator;
    private  Node left;
    private  Node right;
}
