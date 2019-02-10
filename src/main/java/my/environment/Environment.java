package my.environment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuzefan  2019/2/10 15:57
 */
public class Environment {

    private Environment parent;
    private Map<String,String> vars;

    public Environment(Environment parent) {
        this.parent = parent;
        this.vars = parent != null? parent.vars : new HashMap<>();
    }

    public Environment lookup(String name) {
        Environment scope = this;
        while (scope!=null) {
            if (scope.vars.get(name)!=null) {
                return scope;
            }
            scope = scope.parent;
        }
        return null;
    }

    public  String get(String name) {
        if (this.vars.get(name)!=null) {
            return this.vars.get(name);
        }
        throw new Error("Undefined variable " + name);
    }

    public String set(String name, String value) {
        Environment scope = this.lookup(name);
        if (!notNull(scope) && notNull(this.parent)) {
            throw new Error("Undefined variable " + name);
        }
        if (notNull(scope)) {
            return scope.vars.get(name);
        } else {
            return this.vars.get(name);
        }
    }

    public String def(String name,String value) {
        return this.vars.put(name,value);
    }


    private boolean notNull(Environment op) {
        return op != null;
    }
}
