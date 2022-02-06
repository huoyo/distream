# distream

---

[中文文档/CHINESE](README.md)

<div >
    <img src='https://shields.io/badge/version-1.0.0-green.svg'>
    <img src='https://shields.io/badge/author-Chang Zhang-dbab09.svg'>
    <h4>a extended tool of List about how to process data by lambda,expressions and custom class.</h4>
</div>

* Process data stream fluently

```java
lines = lines
        .handle("value=format(value,2)")
        .handle("name=replace(name,'#','')")
        .handle("percent=value/"+sum)
        .groupBy("name").sum("value");
```

* Read data easily by sql 

```java
Datasource datesource = xxx;
ListFrame list = new ListFrame();
list.initDataSource(datesource);

lines = list.readSql("select * from xxx").handle(a->...).handle(a->...)...;
```


#### Maven dependency


```
 <dependency>
    <groupId>cn.langpy</groupId>
    <artifactId>distream</artifactId>
    <version>1.0.0-PRE</version>
 </dependency>
```


#### Read and process data

##### 0. File content

```
序号,姓名,年龄,收入
1,张三,23,5000.11
2,李四,22,4000.22
3,李二狗,20,5000.33
4,韦陀掌,23,3000.44
5,拈花指,23,2000.55
6,小六子,18,5000.66
7,杨潇,23,3000.77
8,李留,19,5000.55

```

##### 1.Read every line

```java
ListFrame<String> lines = ListFrame.readString("test.txt");
lines = lines
    .handle(line -> line + ";") //add ";" at the end of every line
    .handle(line -> "=>"+line ); //add "=>" at the front of every line
```

##### 2.Read csv

```java
/*read easily*/
//ListFrame<Map<String, Object>> lines = ListFrame.readMap(path);
/*read by split symbol*/
//ListFrame<Map<String, Object>> lines = ListFrame.readMap(path,",");
/*define data types*/
ListFrame<Map<String, Object>> lines = ListFrame.readMap(path,",",new Class[]{Integer.class,String.class,Integer.class,Double.class});
lines = lines
        .handle("收入=收入*0.8")
        .handle("序号='0'+序号;姓名=序号+姓名")//add "0" at the front of 序号;rename 姓名 by 序号+姓名
        .handle(new MapHandler());//add a key named "newKey" whose value is 1  ;MapHandler can be seen as follows
```

Define a data handler which needs implement DataHandler<E>

```java
public class MapHandler implements DataHandler<Map<String, Object>> {
    @Override
    public Map<String, Object> handle(Map<String, Object> line) {
        line.put("newKey",1);
        return line;
    }
}
```

##### 3.Obtain data by a column name

compute max,min and avg;

```java
/*obtain data by column name*/
ListFrame<Double> indexs = lines.get("收入");
/*you can user ObjectName::getXX if ListFrame's elements are java objects*/
//ListFrame<Integer> indexs = lines.get(User::getAge);
double maxIncome = indexs.max();
double minIncome = indexs.min();
double avgIncome = indexs.avg();
```

##### 4.Grouby

```java
MapFrame<Object, ListFrame> agesGroup = lines.groupBy("年龄");
Map<Object, Integer> count = agesGroup.count();
Map<Object, Double> incomeAvg = agesGroup.avg("收入");
Map<Object, Double> incomeSum = agesGroup.sum("收入");
Map<Object, ListFrame> incomeConcat = agesGroup.concat("收入");
```

##### 5.Save data to a txt file

```java
/*save to file*/
lines.toFile("save.txt");
```


##### 6.convert data between List and ListFrame

```java
List<Object> list = ...;
ListFrame<Object> lines = ListFrame.fromList(list);
list = lines.toList();
```

##### 6.Convert Map to Object

```java

ListFrame<Map> lines = ListFrame.readMap(path);
ListFrame<User> users = lines.toObject(User.class);
```

##### 8.Replace data

```java
/*replace "xxx" to "yyy"*/
lines = lines.replace("需要替换的列","xxx","yyy");
```


#### Copyright

> 1.This project belongs to Chang Zhang,and Its open source protocol is Apache-2.0；
>
> 2.You can use it freely  but please respect copyright;
>
> 3.Please specify the differences if you update codes；
>
