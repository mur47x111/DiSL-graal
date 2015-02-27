package ch.usi.dag.disl.resolver;

import java.lang.reflect.Method;
import java.util.Set;

public class GuardMethod {

    private Method method;
    private Set<Class<?>> argTypes;

    public GuardMethod(Method method) {
        super();
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Set<Class<?>> getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(Set<Class<?>> argTypes) {
        this.argTypes = argTypes;
    }
}