<div align="center">
    <h1 >Distream</h1>  
</div>
---

[中文文档/CHINESE](README.md)

<div >
    <img src='https://shields.io/badge/version-1.1.3-green.svg'>
    <img src='https://shields.io/badge/author-Chang Zhang-dbab09.svg'>
    <h4>An extended tool of List about how to process data fluently by lambda,expressions and custom class.</h4>
</div>

* Process data stream fluently

```java
lines = lines
        .handle("value=format(value,2)") //round to the nearest hundredth
        .handle(line->line.getName()==null,"name=''") //if(line.getName()==null){line.setName('');}
        .handle(line->line.getValue()==null,"value=0","value=value+2") //value = line.getValue()==null?0:line.getValue()+2;
        .handle("name=replace(name,'#','')") //replace '#' to ''
        .handle("percent=double(value)/"+sum) //converting value's tyle to double and computing percent
        .groupBy("name").sum("percent"); //groupBy 'name'
```

* Read data easily by sql 

```java
Datasource datesource = xxx;
ListFrame list = new ListFrame();
list.initDataSource(datesource);

lines = list.readSql("select * from xxx").handle(a->...).handle(a->...)...;
```

> Notice：you need add a dependency about connecting database such as Mysql:

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.22</version>
</dependency>
```


#### Maven dependency


```
 <dependency>
    <groupId>cn.langpy</groupId>
    <artifactId>distream</artifactId>
    <version>1.1.1<version>
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

You can also use DataHandler like :

```java
lines = lines(map->{
  map.put("newKey",1);
  return map;
});
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
/*continuous groupBy*/
MapFrame<Object, MapFrame<Object, ListFrame>> incomeAgeConcat = lines.groupBy("收入").groupBy("年龄");
```
##### 5.Save data to a txt file

```java
/*save to file*/
lines.toFile("save.txt");
```


##### 6.Convert data between List and ListFrame

```java
List<Object> list = ...;
ListFrame<Object> lines = ListFrame.fromList(list);
list = lines.toList();
```

##### 7.Convert data between Map and Object

```java

ListFrame<Map> lines = ListFrame.readMap(path);
ListFrame<User> users = lines.toObjectList(User.class);
ListFrame<Map> maps = users.toMapList();
```

##### 8.Replace data

```java
/*replace "xxx" to "yyy"*/
lines = lines.replace("需要替换的列","xxx","yyy");
```


##### 9.Convert types

```java
List<String> list = Arrays.asList("1","2","3","4");
ListFrame<Integer> listFrame = ListFrame.fromList(list );
ListFrame<Integer> listInt= listFrame.asInteger();
ListFrame<Double> listDouble= listFrame.asDouble();
ListFrame<Float> listFloat= listFrame.asFloat();
ListFrame<String> listString= listFloat.asString();

```

##### 10.Elements's frequencies

```java
List<Integer> list = Arrays.asList(2,2,2,4);
MapFrame<Integer,Integer> listFrame = ListFrame.fromList(list).frequency()
/*map {2=3,4=1}*/

```

##### 11.Variance and standardDeviation

```java
List<Integer> list = Arrays.asList(2,2,2,4);
ListFrame<Integer> listFrame = ListFrame.fromList(list );
listFrame.variance();//方差
listFrame.standardDeviation();//标准差

```


##### 12.Remove the null


```java
List<Integer> list = Arrays.asList(2,null,2,null,6);
ListFrame<Integer> listFrame = ListFrame.fromList(list );
listFrame = listFrame.dropNull();

//[2,null,2,null,6]->[2,2,6]

```

##### 13.Remove the repeated


```java
List<Integer> list = Arrays.asList(2,2,2,6,6);
ListFrame<Integer> listFrame = ListFrame.fromList(list );
listFrame = listFrame.distinct();
//[2,2,2,6,6]->[2,6]

```


##### 14.Common functions

```java
/*replace "xxx" to "yyy"*/
ListFrame<Map<String, Object>> lines = xxx;
/*convert code to int*/
lines = lines.handle("id=int(code)");

/*convert value to double*/
lines = lines.handle("percent=double(value)");

/*convert value to string*/
lines = lines.handle("name=string(value)");

/*substring is like java substring*/
lines = lines.handle("name=substring(name,1,2)");

/*replace "xxx" to "yyy"*/
lines = lines.handle("name=replace(name,'xxx','yyy')");
/*you can alse use '-' to replace if you only want to replace 'xxx' */
lines = lines.handle("name=name-'xxx'");

/*index is like java indexof*/
lines = lines.handle("id=index(name,'xxx')");

/*round to the nearest hundredth*/
lines = lines.handle("percent=format(percent,2)");
```


#### Copyright

> 1.This project belongs to Chang Zhang,and Its open source protocol is Apache-2.0；
>
> 2.You can use it freely  but please respect copyright;
>
> 3.Please specify the differences if you update codes；
>
