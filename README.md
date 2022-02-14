# distream

---

[英文文档/ENGLISH](README-EN.md)

<div >
    <img src='https://shields.io/badge/version-1.0.0-green.svg'>
    <img src='https://shields.io/badge/author-Chang Zhang-dbab09.svg'>
    <h4>一个为Java语言开发的List扩展工具库，可用于list对象流式数据处理，包括自定义数据处理器、lambda表达式和等式计算等</h4>
</div>

* 真正的数据流式丝滑处理

```java
lines = lines
        .handle("value=format(value,2)")
        .handle(line->line.getName()==null,"name=''") //if(line.getName()==null){line.setName('');}
        .handle("name=replace(name,'#','')")
        .handle("percent=value/"+sum)
        .groupBy("name").sum("percent");
```

* 便携数据库读取


```java
Datasource datesource = xxx;
ListFrame list = new ListFrame();
list.initDataSource(datesource);

lines = list.readSql("select * from xxx").handle(a->...).handle(a->...)...;
```

> 注意：读取数据库的时候需要引入对应的连接驱动，比如Mysql:

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.22</version>
</dependency>
```

#### 引入

引入maven依赖

```
 <dependency>
    <groupId>cn.langpy</groupId>
    <artifactId>distream</artifactId>
    <version>1.0.2</version>
 </dependency>
```


#### 数据读取与转换

##### 0.假设有如下文件

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

##### 1.按行读取文件

按行读取文件,并在每一行的结尾添加";"，每一行的开头添加"=>"

```java
ListFrame<String> lines = ListFrame.readString("test.txt");
lines = lines
    .handle(line -> line + ";") //add ";" at the end of every line
    .handle(line -> "=>"+line ); //add "=>" at the front of every line
```

##### 2.按map读取csv文件

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


自定义一个数据处理器，需实现DataHandler<E>，E的类型为list中每一个对象的类型

```java
public class MapHandler implements DataHandler<Map<String, Object>> {
    @Override
    public Map<String, Object> handle(Map<String, Object> line) {
        line.put("newKey",1);
        return line;
    }
}
```

##### 3.按列获取数据

按列读取数据并求最大最小值以及平均值

```java
/*obtain data by column name*/
ListFrame<Double> indexs = lines.get("收入");
/*you can user ObjectName::getXX if ListFrame's elements are java objects*/
//ListFrame<Integer> indexs = lines.get(User::getAge);
double maxIncome = indexs.max();
double minIncome = indexs.min();
double avgIncome = indexs.avg();
```

##### 4.分组求和

```java
MapFrame<Object, ListFrame> agesGroup = lines.groupBy("年龄");
Map<Object, Integer> count = agesGroup.count();
Map<Object, Double> incomeAvg = agesGroup.avg("收入");
Map<Object, Double> incomeSum = agesGroup.sum("收入");
Map<Object, ListFrame> incomeConcat = agesGroup.concat("收入");
```

##### 5.保存成文件

```java
/*save to file*/
lines.toFile("save.txt");
```


##### 6.List与ListFrame的转换

```java
List<Object> list = ...;
ListFrame<Object> lines = ListFrame.fromList(list);
list = lines.toList();
```

##### 7.Map转对象

```java

ListFrame<Map> lines = ListFrame.readMap(path);
ListFrame<User> users = lines.toObject(User.class);
```

##### 8.数据替换

```java
/*replace "xxx" to "yyy"*/
lines = lines.replace("需要替换的列","xxx","yyy");
```

##### 9.常用函数

```java

ListFrame<Map<String, Object>> lines = xxx;
/*convert code to int*/
lines = lines.handle("id=int(code)");

/*convert value to double*/
lines = lines.handle("percent=double(value)");

/*convert value to string*/
lines = lines.handle("name=string(value)");

/*substring like java substring*/
lines = lines.handle("name=substring(name,1,2)");

/*replace "xxx" to "yyy"*/
lines = lines.handle("name=replace(name,'xxx','yyy')");

/*index like java indexof*/
lines = lines.handle("id=index(name,'xxx')");

/*round to the nearest hundredth*/
lines = lines.handle("percent=format(percent,2)");
```


#### 版权说明

> 1.本项目版权属作者所有，并使用 Apache-2.0进行开源；
>
> 2.您可以使用本项目进行学习、商用或者开源，但任何使用了本项目的代码的软件和项目请尊重作者的原创权利；
>
> 3.如果您使用并修改了本项目的源代码，请注明修改内容以及出处；
>
> 4.其他内容请参考Apache-2.0