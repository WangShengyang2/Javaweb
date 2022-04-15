import com.util.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestJdbc {

    @Test
    public void testQuery() throws Exception{
        String sql = "update  db_test.tb_user set user_name = ?where user_id = ?";
        Integer integer = JdbcUtils.executeUpdate(sql, "李四",2);
        System.out.println("integer = " + integer);
    }

    @Test
    public void testExtends(){
        System.out.println("你号 hot-fix");
    }
}