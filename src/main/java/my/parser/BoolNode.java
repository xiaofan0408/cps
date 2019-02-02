package my.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xuzefan  2019/2/2 14:00
 */
@Data
public class BoolNode extends Node {
    private Boolean value;

    public BoolNode() {

    }
    public BoolNode(String type, Boolean value) {
        this.setType(type);
        this.setValue(value);
    }

}
