package cn.langpy.test;

public class TestObject {
    private Integer se;
    private String name;
    private Integer age;
    private Double income;

    public Integer getSe() {
        return se;
    }

    public void setSe(Integer se) {
        this.se = se;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }

    @Override
    public String toString() {
        return "TestObject{" +
                "se=" + se +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", income=" + income +
                '}';
    }
}
