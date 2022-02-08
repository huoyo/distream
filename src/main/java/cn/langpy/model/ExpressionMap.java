package cn.langpy.model;
public class ExpressionMap {

    private String assignKey;

    private OperateMap operate1;

    private OperateMap operate2;

    private String operateSymbol;

    public String getAssignKey() {
        return assignKey;
    }

    public void setAssignKey(String assignKey) {
        this.assignKey = assignKey;
    }

    public OperateMap getOperate1() {
        return operate1;
    }

    public void setOperate1(OperateMap operate1) {
        this.operate1 = operate1;
    }

    public OperateMap getOperate2() {
        return operate2;
    }

    public void setOperate2(OperateMap operate2) {
        this.operate2 = operate2;
    }

    public String getOperateSymbol() {
        return operateSymbol;
    }

    public void setOperateSymbol(String operateSymbol) {
        this.operateSymbol = operateSymbol;
    }
}
