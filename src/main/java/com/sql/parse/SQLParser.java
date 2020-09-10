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
public class SQLParser {



    public static SQLComposition parse() {

        SQLComposition sql = new SQLComposition();    //SQL语句类

        Map<String, String> selectMapAS = new HashMap<>(); //存储select选项别名

        String fromSource = null;

        List<String> joinList = new ArrayList<>();

        Map<String, String> fromMapAS = new HashMap<>();



        SqlParser.Config config = SqlParser.configBuilder()
                .setLex(Lex.MYSQL)
                //.setConformance(SqlConformanceEnum.MYSQL_5)
                .build();

        //SqlParser sqlParser = SqlParser.create("select avg(logout_time - login_time)  from log_model where eduction='本科' and level=1 group by dept_id",config);
        //SqlParser sqlParser = SqlParser.create("select * from log_model where eduction='本科' and level=1 group by dept_id",config);
        //SqlParser sqlParser = SqlParser.create("select bb from log_model order by aa ",config);
        SqlParser sqlParser = SqlParser.create("SELECT distinct e.first_name AS FirstName, s.salary AS Salary,test AS t from employee AS e join salary AS s on e.emp_id=s.emp_id where e.organization = 'Tesla' and s.organization = 'Tesla'",config);

        SqlNode sqlNode = null;

        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            e.printStackTrace();
        }
        //select [select选项] 字段列表 [字段别名] /* from 数据源 [where条件子句] [group by子句] [having子句] [order by子句] [limit子句]


        if (SqlKind.ORDER_BY.equals(sqlNode.getKind())) {        //存在OrderBy  要先处理
            SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
            sql.setOrderByColName(sqlOrderBy.orderList);
            sqlNode = ((SqlOrderBy) sqlNode).query;              //sqlNode赋为SqlSelect
        }

        if (SqlKind.SELECT.equals(sqlNode.getKind())) {
            SqlSelect sqlSelect = (SqlSelect) sqlNode;
            sql.setDistinct(sqlSelect.isDistinct());              //是否distinct
            SqlNode from = sqlSelect.getFrom();
            SqlNode where = sqlSelect.getWhere();                 //where条件
            SqlNodeList selectList = sqlSelect.getSelectList();
            //获取group by 字段
            SqlNode having = sqlSelect.getHaving();               //having条件
            SqlNodeList orderList = sqlSelect.getOrderList();     //oderby字段

            /**
             * 获取from数据源   即表名
             */

            if (SqlKind.IDENTIFIER.equals(from.getKind())) {
                extractFromClauses(from,fromSource, joinList, fromMapAS);
            }

            /**
             *  select选项   1.正常项   2.*   3.函数
             *  字段可能来源于多表
             */

            for (SqlNode s : selectList.getList()) {
                //AS
                if (SqlKind.AS.equals(s.getKind())) {
                    selectMapAS.put(((SqlBasicCall) s).operand(0).toString(), ((SqlBasicCall) s).operand(1).toString());
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
            sql.setSelectList(selectList);
            sql.setSelectMapAS(selectMapAS);
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
            sql.setWhereExpression(fieldList_where);
            //having   同where
            List<String> fieldList_having = new ArrayList<>();
            processWhere(having,fieldList_having);
            sql.setHavingExpression(fieldList_having);

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
        return sql;
    }

    private static void extractFromClauses(SqlNode node, String whereSource, List<String> joinList, Map<String, String> fromMapAS) {
        // If order by comes in the query.
        if (node.getKind().equals(SqlKind.ORDER_BY)) {
            // Retrieve exact select.
            node = ((SqlSelect) ((SqlOrderBy) node).query).getFrom();
        } else {
            node = ((SqlSelect) node).getFrom();
        }

        if (node == null) {
            return;
        }

        // Case when only 1 data set in the query.
        if (node.getKind().equals(SqlKind.AS)) {
            whereSource = ((SqlBasicCall) node).operand(0).toString();
            fromMapAS.put(((SqlBasicCall) node).operand(0).toString(),((SqlBasicCall) node).operand(1).toString());
            return;
        }

        // Case when there are more than 1 data sets in the query.
        if (node.getKind().equals(SqlKind.JOIN)) {
            final SqlJoin from = (SqlJoin) node;

            // Case when only 2 data sets are in the query.
            if (from.getLeft().getKind().equals(SqlKind.AS)) {
                joinList.add(((SqlBasicCall) from.getLeft()).operand(0).toString());
                fromMapAS.put(((SqlBasicCall) from.getLeft()).operand(0).toString(), ((SqlBasicCall) from.getLeft()).operand(1).toString());
            } else if (from.getLeft().getKind().equals(SqlKind.IDENTIFIER)) {
                joinList.add(from.getLeft().toString());
            } else {
                // Case when more than 2 data sets are in the query.
                SqlJoin left = (SqlJoin) from.getLeft();

                // 树的遍历
                while (!left.getLeft().getKind().equals(SqlKind.AS)) {
                    joinList.add(((SqlBasicCall) left.getRight()).operand(0).toString());
                    fromMapAS.put(((SqlBasicCall) left.getRight()).operand(0).toString(), ((SqlBasicCall) left.getRight()).operand(1).toString());
                    left = (SqlJoin) left.getLeft();
                }
                joinList.add(((SqlBasicCall) left.getLeft()).operand(0).toString());
                joinList.add(((SqlBasicCall) left.getRight()).operand(0).toString());
                fromMapAS.put(((SqlBasicCall) left.getLeft()).operand(0).toString(), ((SqlBasicCall) left.getLeft()).operand(1).toString());
                fromMapAS.put(((SqlBasicCall) left.getRight()).operand(0).toString(), ((SqlBasicCall) left.getRight()).operand(1).toString());
            }
            if (from.getRight().getKind().equals(SqlKind.IDENTIFIER)) {
                joinList.add(from.getRight().toString());
            } else
                fromMapAS.put(((SqlBasicCall) from.getRight()).operand(0).toString(), ((SqlBasicCall) from.getRight()).operand(1).toString());;
        }
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
