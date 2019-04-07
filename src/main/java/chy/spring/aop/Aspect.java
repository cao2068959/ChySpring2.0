package chy.spring.aop;

import java.lang.reflect.Method;

public class Aspect {
    private Method after;
    private Method around;
    private Method before;
    private Object execTarget;
    private String match;

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public Aspect(Object execTarget) {
        this.execTarget = execTarget;
    }

    public Aspect() {
    }

    public Object getExecTarget() {
        return execTarget;
    }

    public void setExecTarget(Object execTarget) {
        this.execTarget = execTarget;
    }

    public Method getAfter() {
        return after;
    }

    public void setAfter(Method after) {
        this.after = after;
    }

    public Method getAround() {
        return around;
    }

    public void setAround(Method around) {
        this.around = around;
    }

    public Method getBefore() {
        return before;
    }

    public void setBefore(Method before) {
        this.before = before;
    }
}
