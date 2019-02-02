package my.parser;

import lombok.Data;

import java.util.List;

/**
 * @author xuzefan  2019/2/2 14:10
 */
@Data
public class ProgNode extends Node {
    private List<Node> prog;
}
