<div align="center">
    <h1 >Distream</h1>  
</div>

---

[英文文档/ENGLISH](README-EN.md)

<div align="center">
    <img src='https://shields.io/badge/version-1.1.0-green.svg'>
    <img src='https://shields.io/badge/author-Chang Zhang-dbab09.svg'>
    <h4>一个为Java语言开发的List扩展工具库，可用于list对象流式数据处理，包括自定义数据处理器、lambda表达式和等式计算等</h4>
    <h4>An extended tool of List about how to process data by lambda,expressions and custom class.</h4>
</div>

* 真正的数据流式丝滑处理

```java
double sum = lines.get("value").sum();
lines = lines
        .handle("value=format(value,2)") //round to the nearest hundredth
        .handle(line->line.getName()==null,"name=''") //if(line.getName()==null){line.setName('');}
        .handle(line->line.getValue()==null,"value=0","value=value+2") //value = line.getValue()==null?0:line.getValue()+2;
        .handle("name=replace(name,'#','')") //replace '#' to ''
        .handle("percent=double(value)/"+sum) //converting value's tyle to double and computing percent
        .groupBy("name").sum("percent"); //groupBy 'name'
```

* 性能更好的写法（v1.1.0支持）

```java
double sum = lines.get("value").sum();
lines = lines
        .addHandler("value=format(value,2)") 
        .addHandler("name=replace(name,'#','')")
        .addHandler("percent=double(value)/"+sum) 
        .execute()
        .groupBy("name").sum("percent"); //groupBy 'name'
```

* 更推荐的写法（v1.1.0支持，性能最好）


```java
lines = lines
        .addHandler(new DataHandler1()) 
        .addHandler(new DataHandler2()) 
        .addHandler(new DataHandler3()) 
        .addHandler(new DataHandler4()) 
        .execute();
//DataHandler1、2、3、4需要实现DataHandler接口
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
    <version>1.1.0</version>
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
ListFrame<Map<String, Object>> lines = ListFrame.readMap(path,new Class[]{Integer.class,String.class,Integer.class,Double.class});
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

也可以简写为：

```java
lines = lines(map->{
  map.put("newKey",1);
  return map;
});
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
/*the index of max*/
int argmax= listFrame.argmax();
/*the index of min*/
int argmin= listFrame.argmin();

```

##### 4.分组求和

```java
MapFrame<Object, ListFrame> agesGroup = lines.groupBy("年龄");
MapFrame<Object, Integer> count = agesGroup.count();
MapFrame<Object, Double> incomeAvg = agesGroup.avg("收入");
MapFrame<Object, Double> incomeSum = agesGroup.sum("收入");
MapFrame<Object, ListFrame> incomeConcat = agesGroup.concat("收入");
/*continuous groupBy*/
MapFrame<Object, MapFrame<Object, ListFrame>> incomeAgeConcat = lines.groupBy("收入").groupBy("年龄");
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

##### 7.Map与对象互转

```java

ListFrame<Map> lines = ListFrame.readMap(path);
ListFrame<User> users = lines.toObjectList(User.class);
ListFrame<Map> maps = users.toMapList();
```

##### 8.数据替换

```java
/*replace "xxx" to "yyy"*/
lines = lines.replace("需要替换的列","xxx","yyy");
```

##### 9.类型转化

```java
List<String> list = Arrays.asList("1","2","3","4");
ListFrame<Integer> listFrame = ListFrame.fromList(list );
ListFrame<Integer> listInt= listFrame.asInteger();
ListFrame<Double> listDouble= listFrame.asDouble();
ListFrame<Float> listFloat= listFrame.asFloat();
ListFrame<String> listString= listFloat.asString();

```

##### 10.统计元素个数

```java
List<Integer> list = Arrays.asList(2,2,2,4);
MapFrame<Integer,Integer> listFrame = ListFrame.fromList(list).frequency()
/*得到map {2=3,4=1}*/

```

##### 11.方差和标准差

```java
List<Integer> list = Arrays.asList(2,2,2,4);
ListFrame<Integer> listFrame = ListFrame.fromList(list );
listFrame.variance();//方差
listFrame.standardDeviation();//标准差

```


##### 12.剔除null值

如果一个list中存在为null的存在需要遍历剔除，可以直接使用如下函数：

```java
List<Integer> list = Arrays.asList(2,null,2,null,6);
ListFrame<Integer> listFrame = ListFrame.fromList(list );
listFrame = listFrame.dropNull();

//[2,null,2,null,6]->[2,2,6]

```

##### 13.去重


```java
List<Integer> list = Arrays.asList(2,2,2,6,6);
ListFrame<Integer> listFrame = ListFrame.fromList(list );
listFrame = listFrame.distinct();

//[2,2,2,6,6]->[2,6]

```


##### 14.常用函数

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
/*you can alse use '-' to replace if you only want to replace 'xxx' */
lines = lines.handle("name=name-'xxx'");

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