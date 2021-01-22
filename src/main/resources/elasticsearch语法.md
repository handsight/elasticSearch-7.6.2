倒排序索引包含两个部分：

	》单词词典：记录所有文档单词，记录单词到倒排列表的关联关系
	
	》倒排列表：记录单词与对应文档结合，由倒排索引项组成

倒排索引项：

	》文档
	
	》词频 TF - 单词在文档中出现的次数，用于相关性评分
	
	》位置（Position）- 单词在文档中分词的位置，用于phrase query
	
	》偏移（Offset）- 记录单词开始结束的位置，实现高亮显示


原始数据：

{ "id" : 1,"content":"关注我,系统学编程" } 

{ "id" : 2,"content":"系统学编程,关注我" } 

{ "id" : 3,"content":"系统编程,关注我" } 

{ "id" : 4,"content":"关注我,间隔系统学编程" }

![img](https://mmbiz.qpic.cn/mmbiz_png/WO9qeUgIowLXVeVsapojLrmMs7ibtibLicJiaq10doVpKudBTOepQbT6MUJqm964sjmhgEibh8BicqpMEwstrtv9ia0ZA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

ES的数据类型汇总
	String类型
		keyword 不分词 常用于商品的品牌字段等，	       中国我爱你-->中国我爱你
		text	     分词  常用于商品的标题，内容字段等           中国我爱你-->中国,我，爱，你
	数值数据类型
		long
		integer
		short
		byte
		double
		float
		half_float
		scaled_float
	时间类型
		date  常用于商品的创建时间字段等，支持yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis（毫秒值）3种格式，
	复杂类型
		array   存数组，可以包含零个或多个值，必须具有相同的数据类型
		object 存对象  适用于1对1 也可以存多个
		nested 嵌套类型  适用于1对多，比如商品的属性字段，一个商品有多个属性
	特定类型
		geo_point 地理位置类型   距离查询：距离某个点方圆200km，指定区域的酒店

当前坐标的几个范围内的酒店的数量，比如说举例我0~100m有几个酒店，100m~300m有几个酒店，300m以上有几个酒店



（5）filter大部分情况下来说，在query之前执行，先尽量过滤掉尽可能多的数据

query：是会计算doc对搜索条件的relevance score，还会根据这个score去排序
filter：只是简单过滤出想要的数据，不计算relevance score，也不排序



PUT /my_index
{
  "settings": {  # 索引设置
    "index": {
      "number_of_shards": 1, # 分片数量设置为1，默认为5
      "number_of_replicas": 1 # 副本数量设置为1，默认为1
    },
    "analysis": {
      "analyzer": {
        "custom_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase"
          ]
        }
      }
    }
  },
  "mappings": {  # 映射配置
      "dynamic": false, # 是动态映射的开关，有3种状态：true 动态添加新的字段--缺省；推荐使用）false 忽略新的字段,不会添加字段映射，但是会存在于_source中；（strict 如果遇到新字段抛出异常；
      "properties": {
        "自定义title字段": {
          "type": "ES的数据类型汇总",
          "analyzer": "ik_max_word", # 存储时的分词器
          "search_analyzer": "ik_smart"  # 查询时的分词器
        },
        "my_text": {
          "type": "text",
          "analyzer": "custom_analyzer",
        }
        "content": {
          "type": "keyword", # 默认为 keyword类型
          "fields": {
            "ik_max_analyzer": { # 创建名为 ik_max_analyzer 的子字段
              "type": "text",
              "analyzer": "ik_max_word", # 字段ik_max_analyzer 的倒排序索引分词器为ik_max_word
              "search_analyzer": "ik_max_word" # 检索关键词的分词器为ik_max_word
            },
            "ik_smart_analyzer": {  # 创建名为 ik_smart_analyzer的子字段
              "type": "text",
              "analyzer": "ik_smart" # 字段ik_smart_analyzer 的倒排序索引分词器为ik_smart
            }
          }
        }
      }
  }
}

GET /tehero_index/_doc/_search
{
  "query":{
    "match":{
      "content":"关注我，系统学编程"
       "operator":"or"
    }
  }
}
如果content的type=keyword,是不会分词的，所以检索词需要和内容完全一样 (关注我，系统学编程)
如果content的type=text会分词 关注我，系统学编程-->关注，我，系统学，编程 
控制Token之间的逻辑关系，默认or/and  (select * from 表 where Token = 系统学 or Token = 编程,select * from 表 where Token = 系统学 and Token = 编程  )



GET /tehero_index/_doc/_search
{
    "query": {
        "match_phrase": {
            "content.ik_smart_analyzer": {
            	"query": "关注我,系统学",
	"slop":1
            }
        }
    }
}

检索词“关注我，系统学”会被分词为3个Token【关注、我、系统学】，文档的Token的顺序和检索词分词【关注、我、系统学】后一致，且连续才能查询到


GET tehero_index/_doc/_search
{
  "query": {
    "match_phrase_prefix": {
      "content.ik_smart_analyzer": {
        "query": "系",
        "max_expansions": 2
      }
    }
  }
}

GET /tehero_index/_doc/_search
{
  "query": {
    "multi_match": {
      "query": "系统",
      "fields": [
        "content",
        "label.^3",
        "title.^3"
      ]
    }
  }
}

1）match query：用于执行全文查询的标准查询，包括模糊匹配和短语或接近查询。重要参数：控制Token之间的布尔关系：operator：or/and  控制搜索结果精准度：，minimum_should_match  75%
2）match_phrase query：与match查询类似，但用于匹配确切的短语或单词接近匹配。重要参数：Token之间的位置距离：slop 参数
	检测词分词后与文档的分词后的一模一样，包括顺序都一样 才能查到 
	如文档1ABC,文档2ABD  检测词ABC分词后A,B,C匹配到文档1 检测词ABE分词后A,B,E一个都没有匹配,
	如果配置参数slop=1 检测词ABE分词后A,B,E能匹配文档1，文档2

   一般结合edge ngram使用，用切分后的ngram来实现前缀搜索推荐功能

3）match_phrase_prefix query：与match_phrase查询类似，但是会对最后一个Token在倒排序索引列表中进行通配符搜索。重要参数：模糊匹配数控制：max_expansions 默认值50，最小值为1

因为，最后一个前缀始终要去扫描大量的索引，性能可能会很差

4）multi_match query：match查询 的多字段版本。该查询在实际中使用较多，可以降低DSL语句的复杂性。同时该语句有多个查询类型
	如搜索词放到商品的标题，内容，标签中去查，一般而言标签权重3，标题权重2，内容权重1 
	where 标题字段1=“检索词”or 内容字段2 = “检索词” or  标签字段3 = “检索词”

GET /forum/article/_search
{
  "query": {
    "multi_match": {
        "query":                "java solution",
        "type":                 "best_fields",   将某一个field匹配尽可能多的关键词的doc优先返回回来
        "fields":               [ "title^2", "content" ],    标题的权重2 内容权重1
        "tie_breaker":          0.3,  确保满足多个条件的文档的相关性得分一定比只满足单个条件的文档的得分要高
        "minimum_should_match": "50%" 
    }
  } 
}

GET /forum/article/_search
{
   "query": {
        "multi_match": {
            "query":  "learning courses",
            "type":   "most_fields",  尽可能返回更多field匹配到某个关键词的doc，优先返回回来
            "fields": [ "sub_title", "sub_title.std" ]
        }
    }
}



5）common terms query：对于中文检索意义不大
6）query_string query 和 simple_query_string query，其实就是以上 query语句的合集，使用非常灵活，DSL编写简单。但是，TeHero认为这两个查询语句，有一个很明显的弊端：类似于sql注入【建议使用flags参数进行控制】。如果用户在检索词输入了对应的“关键字”【比如OR、*】等，用户将获取到本不应该被查询到的数据。慎用
7）term查询，terms查询  适用于部分词的字段查询，如商品的分类，商品的品牌
	term查询一个，terms查询多个 类似mysql where author in （“"方才兄","方才"）

POST /blogs_index/_doc/_search
{
  "query": {
    "term" : { "author" : "方才兄" }
  }

POST /blogs_index/_doc/_search
{
  "query": {
    "terms" : { "author" : ["方才兄","方才"]}
  }
}

1、 所有的 Term-level queries 的检索关键词都不会分词；

2、term query 等价于sql【where Token = “检索词”】；

3、terms query 等价于sql【where Token in ( 检索词List )】；

4、range query 掌握Date Math 和对 range类型字段检索的 relation参数；

5、掌握 wildcard query、prefix query、fuzzy query 这3种模糊查询；

6、terms_set query 用于检索Array类型的字段，但文档中必须定义一个数字字段——表示最低匹配的term数量；

7、exists query 用于检索为null的字段，检索不为null的字段使用 must_not + exists。


1) 数据准备
PUT /term_set_index
{
  "mappings": {
    "_doc": {
      "properties": {
        "codes": {
          "type": "keyword"
        },
        "required_matches": {
          "type": "integer"
        }
      }
    }
  }
}

PUT /term_set_index/_doc/1?refresh
{
    "codes": ["系统学习", "es","关注我"],
    "required_matches": 2
}
PUT /term_set_index/_doc/2?refresh
{
    "codes": ["系统", "学习"],
    "required_matches": 1
}

GET /term_set_index/_search
{
    "query": {
        "match": {
            "codes" : {
                "query":  "系统学习 关注我",
                "analyzer": "whitespace",
                "minimum_should_match": 2
            }
        }
    }
}
分析：DSL语句使用 "analyzer": "whitespace", 所以 query会被分词两个Token/term【系统学习】【关注我】。"minimum_should_match": 2，所以可以检索到文档1。



1）filter [] and
必须匹配，子句在过滤器上下文中执行，这意味着计分被忽略，并且子句被视为用于缓存。

2）must [] []每个对象是and ,对象里面是=
子句（查询）必须出现在匹配的文档中，并将有助于得分。

3）must_not []每个对象是and ,对象里面是！=
子句（查询）不得出现在匹配的文档中。子句在过滤器上下文中执行，这意味着计分被忽略，并且子句被视为用于缓存。

4）should []每个对象是or ,对象里面是=
子句（查询）应出现在匹配的文档中。【注意should的最小匹配数】



影响力 influence在范围12~20；文章标签tag包含3或者4，同时不能包含1；发布时间createAt一周内；标题title或内容content 包含“es”、“编程”、“必看”【3选2】且需要相关性评分。

参考答案如下：【ps：实现需求的DSL语句有多种可能，建议初学者自己练习下，以下仅供参考】
GET /blogs_index/_search
{
  "query": {
    "bool": {
      "filter": {
        "bool": {
          "must": [
            {
              "range": {
                "influence": {
                  "gte": 12,
                  "lte": 20,
                  "relation": "WITHIN"
                }
              }
            },
            {
              "range": {
                "createAt": {
                  "gte": "now-1w/d"
                }
              }
            },
            {
              "terms": {
                "tag": [
                  3,
                  4
                ]
              }
            }
          ],
          "must_not": [
            {
              "term": {
                "tag": 1
              }
            }
          ]
        }
      },
      "should": [
        {
          "multi_match": {
            "query": "es",
            "fields": [
              "title",
              "content"
            ]
          }
        },
        {
          "multi_match": {
            "query": "编程",
            "fields": [
              "title",
              "content"
            ]
          }
        },
        {
          "multi_match": {
            "query": "必看",
            "fields": [
              "title",
              "content"
            ]
          }
        }
      ],
      "minimum_should_match": 2
    }
  }
}



**boost  参数【常用】**

我们检索博客时，我们一般会认为标题 title 的权重应该比内容 content 的权重大，那么这个时候我们就可以使用 boost 参数进行控制

GET /blogs_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": {
              "query": "es",
              **"boost": 2**
            }
          }
        },
        {
          "match": {
            "content": "es"
          }
        }
      ]
    }
  },
  "explain": true
}



***\*1）constant_score\** query**

嵌套一个 filter 查询，**为任意一个匹配的文档****指定一个常量评分****，常量值****为 boost 的参数值****【默认值为1】 ，忽略 TF-IDF 信息**

GET /blogs_index/_search
{
    "query": {
        **"constant_score"** : {
            "filter" : {
                "term" : { "title": "es"}
            },
            "boost" : 1.2
        }
    }
}



**Function Score Query 允许我们修改通过 query 检索出来的文档的分数。**

在使用时，我们必须**定义一个查询**和**一个或多个函数**，这些函数为查询返回的每个文档**计算一个新分数。**

GET /blogs_index/_search
{
  "query": {
    "function_score": {
      "query": {
        "match_all": {}
      },
      "boost": "5",
      "functions": [
        {
          "filter": {
            "match": {
              "title": "es"
            }
          },
          "random_score": {},
          "weight": 23
        },
        {
          "filter": {
            "match": {
              "title": "相关度"
            }
          },
          "weight": 42
        }
      ],
      "max_boost": 42, 限制计算出来的分数不要超过max_boost指定的值
      "score_mode": "max",
      "boost_mode": "multiply", 可以决定分数与指定字段的值如何计算，multiply，sum，min，max，replace
      "min_score": 10  
    }
  },
  "explain": true
}