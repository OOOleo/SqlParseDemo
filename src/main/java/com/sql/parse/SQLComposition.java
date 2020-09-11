package com.sql.parse;


import lombok.Data;
import lombok.ToString;
import org.apache.calcite.sql.SqlNodeList;

import java.util.List;
import java.util.Map;

/**
 * sql查询语句基本结构
 * SELECT [ALL | DISTINCT] <目标列表达式> [[AS] <新列名>] [,...n]
 * FROM <表名或试图名> [[AS] <别名>] [...n]
 * [WHERE <条件表达式>]
 * [GROUP BY <分组依据列>]
 * [HAVING <条件表达式>]
 * [ORDER BY <排序依据列> [ASC | DESC]] [...n]
 * [LIMIT N,M]
 */
@Data
@ToString
public class SQLComposition {
    private List<String> select;         //目标列表
    private Map<String,String> selectMapAS;  //若有AS
    private boolean isDistinct;              //Distinct
    private String fromSource;               //from
    private List<String> joinList;           //join表
    private Map<String,String> fromMapAS;    //表名别名
    private List<String> whereExpression;    //where条件
    private SqlNodeList groupByColName;      //group by 列
    private List<String> havingExpression;   //having条件
    private SqlNodeList orderByColName;      //order by 列
}
