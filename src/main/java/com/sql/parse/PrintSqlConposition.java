package com.sql.parse;

import org.apache.calcite.sql.SqlNode;

public class PrintSqlConposition {
    public static void print(SQLComposition sqlComposition) {
        System.out.print("目标列表：");
        for (String  name : sqlComposition.getSelect()) {
            System.out.print(name+"  ");
        }
        System.out.println();

        if (sqlComposition.getSelectMapAS().size()!=0) {
            System.out.println("目标列表别名：");
            for (String key : sqlComposition.getSelectMapAS().keySet()) {
                System.out.println(key + "\t" + sqlComposition.getSelectMapAS().get(key));
            }
        }

        System.out.println("是否distinct: " + sqlComposition.isDistinct());

        System.out.println("from: " + sqlComposition.getFromSource());



        if (sqlComposition.getJoinList().size()!=0) {
            System.out.print("join：");
            for (String name : sqlComposition.getJoinList()) {
                System.out.print(name+ "   ");
            }
            System.out.println();
        }

        if (sqlComposition.getFromMapAS().size()!=0) {
            System.out.println("from别名：");
            for (String key : sqlComposition.getFromMapAS().keySet()) {
                System.out.println(key + "\t" + sqlComposition.getFromMapAS().get(key));
            }
        }

        if (sqlComposition.getWhereExpression().size()!=0) {




            System.out.println("where条件:");
            for (String cond : sqlComposition.getWhereExpression()) {
                System.out.println(cond);
            }
        }

        if (sqlComposition.getGroupByColName() != null) {
            System.out.print("group by: ");
            for (SqlNode sqlNode : sqlComposition.getGroupByColName()) {
                System.out.println(sqlNode.toString());
            }
        }


        if (sqlComposition.getHavingExpression().size()!=0) {
            System.out.println("having条件：");
            for (String cond : sqlComposition.getHavingExpression()) {
                System.out.println(cond);
            }
        }

        if (sqlComposition.getOrderByColName() != null) {
            System.out.print("orderBy: ");
            for (SqlNode sqlNode : sqlComposition.getOrderByColName()) {
                System.out.println(sqlNode.toString());
            }
        }

    }
}
