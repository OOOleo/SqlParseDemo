import com.sql.parse.SQLComposition;
import com.sql.parse.SQLParserUtil;
import org.junit.Test;
public class TestDemo {

    @Test
    public void test(){
        //String sql = "SELECT distinct e.first_name AS FirstName, s.salary AS Salary,test AS t from employee AS e join salary AS s on e.emp_id=s.emp_id where e.organization = 'Tesla' and s.organization = 'Tesla'";
        //String sql = "select avg(logout_time - login_time)  from log_model where eduction='本科' and level=1 group by dept_id";
        //String sql = "select bb from log_model order by aa";
        //String sql = "SELECT ename, sal*13+nvl(comm,0)  FROM emp";
        String sql = "select * from aaa join bbb on a.dd=b.dd join ccc on a.dd=c.dd";
        SQLComposition sqlComposition = SQLParserUtil.parse(sql);
        System.out.println(sqlComposition);

    }
}
