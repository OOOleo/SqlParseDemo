package com.sql.parse;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.ArrayList;
import java.util.HashMap;
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
public class Main {

    public static SQLComposition sql = new SQLComposition();
    public static void main(String[] args) {
        SqlParser.Config config = SqlParser.configBuilder()
                .setLex(Lex.MYSQL)
                //.setConformance(SqlConformanceEnum.MYSQL_5)
                .build();

        //SqlParser sqlParser = SqlParser.create("select avg(logout_time - login_time)  from log_model where eduction='本科' and level=1 group by dept_id",config);
        //SqlParser sqlParser = SqlParser.create("select * from log_model where eduction='本科' and level=1 group by dept_id",config);
        SqlParser sqlParser = SqlParser.create("select bb from log_model order by aa ",config);
        //SqlParser sqlParser = SqlParser.create("SELECT distinct e.first_name AS FirstName, s.salary AS Salary,test AS t from employee AS e join salary AS s on e.emp_id=s.emp_id where e.organization = 'Tesla' and s.organization = 'Tesla'",config);

        SqlNode sqlNode = null;

        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            e.printStackTrace();
        }
        //select [select选项] 字段列表 [字段别名] /* from 数据源 [where条件子句] [group by子句] [having子句] [order by子句] [limit子句]


        if (SqlKind.ORDER_BY.equals(sqlNode.getKind())) {   //存在OrderBy  要先处理
            SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
            sql.setOrderByColName(sqlOrderBy.orderList);
            sqlNode = ((SqlOrderBy) sqlNode).query;     //sqlNode赋为SqlSelect
        }

        if (SqlKind.SELECT.equals(sqlNode.getKind())) {
            SqlSelect sqlSelect = (SqlSelect) sqlNode;
            SqlNode from = sqlSelect.getFrom();
            SqlNode where = sqlSelect.getWhere();                     //where条件
            SqlNodeList selectList = sqlSelect.getSelectList();
            //获取group by 字段
            SqlNode having = sqlSelect.getHaving();                   //having条件
            SqlNodeList orderList = sqlSelect.getOrderList();         //oderby字段

            /**
             * 获取from数据源   即表名
             */

            if (SqlKind.IDENTIFIER.equals(from.getKind())) {


            }

            /**
             *  select选项   1.正常项   2.*   3.函数
             *  字段可能来源于多表
             */

            for (SqlNode s : selectList.getList()) {
                //AS
                if (SqlKind.AS.equals(s.getKind())) {
                    System.out.println(((SqlBasicCall)s).operand(0)+" "+((SqlBasicCall)s).operand(1));
                }

                if (SqlKind.IDENTIFIER.equals(s.getKind())) {
                    System.out.println(s.toString());
                }
                if (SqlKind.OTHER_FUNCTION.equals(s.getKind())) {
                    System.out.println(s.toString());
                    List<String> res = getParaOfFun(s.toString());
                    for (String ss : res) {
                        System.out.println(ss);
                    }

                }
            }

            /**
             * 关于where
             * 1. 条件  AND  OR  ()  NOT
             * 2. 相等    =  !=   <>   >   <   between
             * 3. IN   NOT IN
             * 4. 匹配   like
             * 5. NULL
             */

            List<String> fieldList_where = new ArrayList<>();
            processWhere(where,fieldList_where);
            printTree(fieldList_where);
            //having   同where
            List<String> fieldList_having = new ArrayList<>();
            processWhere(having,fieldList_having);
            printTree(fieldList_having);


            /**
             * order by列表
             */
            if(orderList!=null){
                for (SqlNode s : orderList.getList()) {
                    System.out.println(s);
                    if (SqlKind.IDENTIFIER.equals(s.getKind())) {
                        System.out.println(s.toString());
                    }

                }
            }




         }



    }

    private static List<String> extractFromClauses(SqlNode node) {
        final List<String> tables = new ArrayList<>();
        final Map<String, String> map = new HashMap<>();
        // 只有一个数据集
        if (node.getKind().equals(SqlKind.AS)) {
            tables.add(((SqlBasicCall) node).operand(1).toString()+"  "+(((SqlBasicCall) node).operand(0).toString()));

            return tables;
        }

        // 超过一个数据集.
        if (node.getKind().equals(SqlKind.JOIN)) {
            final SqlJoin from = (SqlJoin) node;

            // 只有两个数据集
            if (from.getLeft().getKind().equals(SqlKind.AS)) {
                tables.add(((SqlBasicCall) from.getLeft()).operand(1).toString()+"  "+((SqlBasicCall) from.getLeft()).operand(0).toString());
            } else {
                // 超过两个数据集
                SqlJoin left = (SqlJoin) from.getLeft();
                while (!left.getLeft().getKind().equals(SqlKind.AS)) {
                    tables.add(((SqlBasicCall) left.getRight()).operand(1).toString()+"  "+((SqlBasicCall) left.getRight()).operand(0).toString());
                    left = (SqlJoin) left.getLeft();
                }
                tables.add(((SqlBasicCall) left.getLeft()).operand(1).toString()+"  "+((SqlBasicCall) left.getLeft()).operand(0).toString());
                tables.add(((SqlBasicCall) left.getRight()).operand(1).toString()+"  "+((SqlBasicCall) left.getRight()).operand(0).toString());
            }

            tables.add(((SqlBasicCall) from.getRight()).operand(1).toString()+"  "+((SqlBasicCall) from.getRight()).operand(0).toString());
            return tables;
        }

        return tables;
    }

    private static Map<String, String> extractWhereClauses(SqlNode where) {
        final Map<String, String> tableToPlaceHolder = new HashMap<>();
        final SqlBasicCall newwhere = (SqlBasicCall) where;
        if (where != null) {
            // Case when there is only 1 where clause
            if (newwhere.operand(0).getKind().equals(SqlKind.IDENTIFIER)
                    && newwhere.operand(1).getKind().equals(SqlKind.LITERAL)) {
                tableToPlaceHolder.put(newwhere.operand(0).toString(),
                        newwhere.operand(1).toString());
                return tableToPlaceHolder;
            }

            final SqlBasicCall sqlBasicCallRight = newwhere.operand(1);
            SqlBasicCall sqlBasicCallLeft = newwhere.operand(0);

            // Iterate over left until we get a pair of identifier and literal.
            while (!sqlBasicCallLeft.operand(0).getKind().equals(SqlKind.IDENTIFIER)
                    && !sqlBasicCallLeft.operand(1).getKind().equals(SqlKind.LITERAL)) {
                tableToPlaceHolder.put(((SqlBasicCall) sqlBasicCallLeft.operand(1)).operand(0).toString(),
                        ((SqlBasicCall) sqlBasicCallLeft.operand(1)).operand(1).toString());
                sqlBasicCallLeft = sqlBasicCallLeft.operand(0); // Move to next where condition.
            }

            tableToPlaceHolder.put(sqlBasicCallLeft.operand(0).toString(),
                    sqlBasicCallLeft.operand(1).toString());
            tableToPlaceHolder.put(sqlBasicCallRight.operand(0).toString(),
                    sqlBasicCallRight.operand(1).toString());
            return tableToPlaceHolder;
        }

        return tableToPlaceHolder;
    }


    public static void processWhere(SqlNode node,List<String> res) {    //输出where子树的叶子结点   即标识符

        if(node==null) return;
        if(isOperator(node)) res.add(node.toString());
        //if(node.getKind().equals(SqlKind.IDENTIFIER) || node.getKind().equals(SqlKind.LITERAL)) return;
        else {
            SqlBasicCall sqlwhereBasicCall = (SqlBasicCall) node;
            for (SqlNode sqlNode1 : sqlwhereBasicCall.operands) {
                processWhere(sqlNode1,res);
            }
        }
    }


    public static void printTree(List<String> list) {
        for (String s : list) {
            System.out.println(s);
        }
    }

    public static boolean isOperator(SqlNode sqlNode) {  //函数 操作符  数据 停止查询
        switch (sqlNode.getKind().toString()) {
            case "EQUALS":
            case "NOT_EQUALS":
            case "GREATER_THAN":
            case "GREATER_THAN_OR_EQUAL":
            case "LESS_THAN":
            case "LESS_THAN_OR_EQUAL":
            case "IN":
            case "NOT_IN":
            case "BETWEEN":
            case "LIKE":
            case "IS NULL":
            case "IS NOT NULL":

                return true;
            default:
                return false;
        }
    }

    public static List<String> getParaOfFun(String fun){  //取出函数的参数 后面会根据参数所属表设置语句的条件
        List<String> res = new ArrayList<>();
        int left=0, right=-1;
        for (int i = 0; i < fun.length(); i++) {
            if(fun.charAt(i)=='(') left=i;
            if(fun.charAt(i)==')') right = i;
        }
        String temp = fun.substring(left, right + 1);
        String[] paras=temp.split(",");
        for (String str : paras) {
            res.add(getLetter(str));
        }
        return res;
    }

    public static String getLetter(String a) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i<a.length();i++){
            char c = a.charAt(i);

            if((c<='z'&&c>='a')||(c<='Z'&&c>='A')){
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
