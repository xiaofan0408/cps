package my.lexer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author xuzefan  2019/2/2 11:50
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    private String type;

    private String value;
}
