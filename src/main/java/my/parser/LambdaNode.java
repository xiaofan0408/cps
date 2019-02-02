package my.parser;

import lombok.Data;

import java.util.List;

/**
 * @author xuzefan  2019/2/2 14:02
 */
@Data
public class LambdaNode extends Node{

    private List<Node> vars;

    private Node body;
}
