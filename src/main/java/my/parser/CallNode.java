package my.parser;

import lombok.Data;

import java.util.List;

/**
 * @author xuzefan  2019/2/2 14:04
 */

@Data
public class CallNode extends Node{

    private Node func;

    private List<Node> args;

}
