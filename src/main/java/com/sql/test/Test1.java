package com.sql.test;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

public class Test1 {

    public static void main(String[] args) {
        SqlParser.Config config = SqlParser.configBuilder()
                .setLex(Lex.MYSQL)
                //.setConformance(SqlConformanceEnum.MYSQL_5)
                .build();
        String sql = "select * from aaa join bbb on a.dd=b.dd join ccc on a.dd=c.dd";
        SqlParser sqlParser = SqlParser.create(sql, config);

        SqlNode sqlNode = null;

        try {
            sqlNode = sqlParser.parseStmt();
        } catch (SqlParseException e) {
            e.printStackTrace();
        }


        if (SqlKind.ORDER_BY.equals(sqlNode.getKind())) {         //存在OrderBy  要先处理
            SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
            sqlNode = ((SqlOrderBy) sqlNode).query;               //sqlNode赋为SqlSelect
        }

        if (SqlKind.SELECT.equals(sqlNode.getKind())) {
            SqlSelect sqlSelect = (SqlSelect) sqlNode;
            SqlNode from = sqlSelect.getFrom();
            SqlNode where = sqlSelect.getWhere();                 //where条件
            SqlNodeList selectList = sqlSelect.getSelectList();
            //获取group by 字段
            SqlNodeList groupByList = sqlSelect.getGroup();
            SqlNode having = sqlSelect.getHaving();               //having条件
            System.out.println(from);
            System.out.println(where);
            }

        }
}
