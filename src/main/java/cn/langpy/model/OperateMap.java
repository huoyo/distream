package cn.langpy.model;

import cn.langpy.constant.Functions;
import cn.langpy.constant.ParamType;

import java.util.List;

public class OperateMap {
    private ParamType paramType;
    private Functions func;
    private List<Object> params;

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }


    public Functions getFunc() {
        return func;
    }

    public void setFunc(Functions func) {
        this.func = func;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}
