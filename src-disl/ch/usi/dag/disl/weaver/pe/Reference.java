package ch.usi.dag.disl.weaver.pe;

public class Reference {
    private Object obj;
    private boolean isValid;

    public Reference() {
        this.obj = null;
        this.isValid = false;
    }

    public Reference(Object obj) {
        this.obj = obj;
        this.isValid = true;
    }

    public Object getObj() {

        if (isValid) {
            return obj;
        } else {
            return null;
        }
    }

    public void setObj(Object obj) {
        this.obj = obj;
        this.isValid = true;
    }
}
