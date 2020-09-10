package com.sql.parse;


public class Main {
    public static void main(String[] args) {
        String sql = "SELECT distinct e.first_name AS FirstName, s.salary AS Salary,test AS t from employee AS e join salary AS s on e.emp_id=s.emp_id where e.organization = 'Tesla' and s.organization = 'Tesla'";
        SQLComposition sqlComposition=SQLParserUtil.parse(sql);
        PrintSqlConposition.print(sqlComposition);

    }
}
