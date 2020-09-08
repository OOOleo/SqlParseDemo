package com.sql.parse;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseQueryValidator {

    private static List<String> extractTableAliases(SqlNode node) {
        final List<String> tables = new ArrayList<>();

        // If order by comes in the query.
        if (node.getKind().equals(SqlKind.ORDER_BY)) {
            // Retrieve exact select.
            node = ((SqlSelect) ((SqlOrderBy) node).query).getFrom();
        } else {
            node = ((SqlSelect) node).getFrom();
        }

        if (node == null) {
            return tables;
        }

        // Case when only 1 data set in the query.
        if (node.getKind().equals(SqlKind.AS)) {
            tables.add(((SqlBasicCall) node).operand(1).toString()+"  "+(((SqlBasicCall) node).operand(0).toString()));
            return tables;
        }

        // Case when there are more than 1 data sets in the query.
        if (node.getKind().equals(SqlKind.JOIN)) {
            final SqlJoin from = (SqlJoin) node;

            // Case when only 2 data sets are in the query.
            if (from.getLeft().getKind().equals(SqlKind.AS)) {
                tables.add(((SqlBasicCall) from.getLeft()).operand(1).toString()+"  "+((SqlBasicCall) from.getLeft()).operand(0).toString());
            } else if (from.getLeft().getKind().equals(SqlKind.IDENTIFIER)) {
                tables.add(from.getLeft().toString());
            } else {
                // Case when more than 2 data sets are in the query.
                SqlJoin left = (SqlJoin) from.getLeft();

                // 树的遍历
                while (!left.getLeft().getKind().equals(SqlKind.AS)) {
                    tables.add(((SqlBasicCall) left.getRight()).operand(1).toString() + "  " + ((SqlBasicCall) left.getRight()).operand(0).toString());
                    left = (SqlJoin) left.getLeft();
                }
                tables.add(((SqlBasicCall) left.getLeft()).operand(1).toString() + "  " + ((SqlBasicCall) left.getLeft()).operand(0).toString());
                tables.add(((SqlBasicCall) left.getRight()).operand(1).toString() + "  " + ((SqlBasicCall) left.getRight()).operand(0).toString());
            }
            if (from.getRight().getKind().equals(SqlKind.IDENTIFIER)) {
                tables.add(from.getRight().toString());
            }else
                tables.add(((SqlBasicCall) from.getRight()).operand(1).toString()+"  "+((SqlBasicCall) from.getRight()).operand(0).toString());
            return tables;
        }

        return tables;
    }

    private static Map<String, String> extractWhereClauses(SqlNode node) {
        final Map<String, String> tableToPlaceHolder = new HashMap<>();

        // If order by comes in the query.
        if (node.getKind().equals(SqlKind.ORDER_BY)) {
            // Retrieve exact select.
            node = ((SqlOrderBy) node).query;
        }

        if (node == null) {
            return tableToPlaceHolder;
        }

        final SqlBasicCall where = (SqlBasicCall) ((SqlSelect) node).getWhere();

        if (where != null) {
            // Case when there is only 1 where clause
            if (where.operand(0).getKind().equals(SqlKind.IDENTIFIER)
                    && where.operand(1).getKind().equals(SqlKind.LITERAL)) {
                tableToPlaceHolder.put(where.operand(0).toString(), 
                        where.operand(1).toString()); 
                return tableToPlaceHolder;
            }

            final SqlBasicCall sqlBasicCallRight = where.operand(1);
            SqlBasicCall sqlBasicCallLeft = where.operand(0);

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

    public static void main(String[] args) throws SqlParseException {
        final String query = "SELECT e.first_name , s.salary from employee  join salary  on e.emp_id=s.emp_id where e.organization = 'Tesla' and s.organization = 'Tesla'";
        //final String query = "select count(ddd)  from log_model group by aaa having sum(r)>3 and bbb>10";
	    final SqlParser parser = SqlParser.create(query);
        final SqlNode sqlNode = parser.parseQuery();
        final SqlSelect sqlSelect = (SqlSelect) sqlNode;
       // final SqlJoin from = (SqlJoin) sqlSelect.getFrom();

        // Extract table names/data sets, For above SQL query : [e, s]
        final List<String> tables = extractTableAliases(sqlNode);
        printList(tables);
        // Extract where clauses, For above SQL query : [e.organization -> 'Tesla', s.organization -> 'Tesla']
        final Map<String, String> whereClauses = extractWhereClauses(sqlSelect);
        printMap(whereClauses);
    }

    public static void printList(List<String> list) {
        for (String ss : list) {
            System.out.println(ss);
        }
    }

    public static void printMap(Map<String, String> map) {
        for (String ss : map.keySet()) {
            System.out.println(ss+"  "+map.get(ss));
        }
    }
}