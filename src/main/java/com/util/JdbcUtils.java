package com.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class JdbcUtils {
    private static DataSource dataSource;
    private static Connection connection;

    private JdbcUtils() {
    }

    static {
        try {                   //记载配置文件
            InputStream resourceAsStream = JdbcUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");
            Properties properties = new Properties();
            properties.load(resourceAsStream);
            dataSource = DruidDataSourceFactory.createDataSource(properties);   //通过Druid工厂创建数据源
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() {
        if (connection == null) {
            synchronized (JdbcUtils.class) {
                if (connection == null) {
                    try {
                        connection = dataSource.getConnection();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return connection;
    }

    public static ResultSet executeQuery(String sql, Object... args) throws Exception {
        Connection connection = JdbcUtils.getConnection();      //获取连接对象
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);   //预编译执行sql语句
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < args.length; i++) {                 //设置参数
            preparedStatement.setObject(i + 1, args[i]);
        }
        ResultSet resultSet = preparedStatement.executeQuery();     //执行查询
        return resultSet;               //返回结果集
    }

    //pageNum当前页，pageSize当前页数据的数量
    public static ResultSet execQueryByPage(String sql, int pageNum, int pageSize) throws Exception {
        Connection connection = JdbcUtils.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);   //预编译sql
        } catch (SQLException e) {
            e.printStackTrace();
        }
        preparedStatement.setInt(1, pageNum - 1);   //设置当前页码，limit后两个参数中第一个从0开始，需要当前页码-1
        preparedStatement.setInt(2, pageSize);      //设置每页数量
        ResultSet resultSet = preparedStatement.executeQuery();     //执行查询
        return resultSet;
    }

    public static Integer executeUpdate(String sql, Object... args) throws Exception {
        PreparedStatement preparedStatement = JdbcUtils.getConnection().prepareStatement(sql);
        //设置参数
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        int row = preparedStatement.executeUpdate();
        return row;

    }

    public static List<HashMap<String, Object>> getMap(ResultSet resultSet) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        ResultSetMetaData metaData = null;
        HashMap<String, Object> map = new HashMap<>();
        try {
            metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 0; i < columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i + 1);
                    Object var1 = resultSet.getObject(columnLabel);
                    map.put(columnLabel, var1);
                }
                if (map.isEmpty()) {
                    return list;
                } else {
                    list.add(map);
                    map = new HashMap<>();
                }
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 功能描述: 传入结果集，与封装的类型，将结果封装为一个list集合
     *
     * @param resultSet
     * @param clazz
     * @return java.util.List<T>
     * @ClassName JdbcUtils
     **/
    public static <T> List<T> getList(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        if (resultSet == null) {
            throw new RuntimeException("查询结果集为空");
        }
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            T t = clazz.newInstance();
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (int i = 0; i < columnCount; i++) {                     //根据结果集的列数进行循环遍历
                for (int j = 0; j < declaredFields.length; j++) {
                    declaredFields[j].setAccessible(true);             //设置私有属性可以访问
                    if (metaData.getColumnLabel(i + 1).equals(declaredFields[j].getName())) {   //设置对象的属性值
                        declaredFields[j].set(t, resultSet.getObject(metaData.getColumnLabel(i + 1)));
                        break;
                    }
                }
            }
            list.add(t);
        }
        return list;
    }

    /**
     * 功能描述: 适用于一行数据，将结果集封装为对象
     *
     * @param resultSet
     * @param clazz
     * @return T
     * @ClassName JdbcUtils
     **/
    public static <T> T getBean(ResultSet resultSet, Class<T> clazz) throws Exception { //泛型方法，根据结果集封装JavaBean并返回
        if (resultSet == null) {                    //判断传入结果集是否为空
            throw new RuntimeException("结果集为空");
        }
        ResultSetMetaData metaData = resultSet.getMetaData();   //获取结果集的元数据
        T t = clazz.newInstance();                              //根据传入的运行类封装为对应的JavaBean
        Field[] declaredFields = t.getClass().getDeclaredFields();  //反射获取属性
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {                      //进行属性赋值
            for (int i = 0; i < columnCount; i++) {
                String columnLabel = null;
                for (int j = 0; j < declaredFields.length; j++) {
                    declaredFields[j].setAccessible(true);
                    if (metaData.getColumnLabel(i + 1).equals(declaredFields[j].getName())) {
                        declaredFields[j].set(t, resultSet.getObject(metaData.getColumnLabel(i + 1)));
                        break;
                    }
                }
            }
        }
        return t;
    }


    /**
     * 功能描述: 查询表中多少行数据
     *
     * @param resultSet
     * @return java.lang.Integer
     * @ClassName JdbcUtils
     **/
    public static Integer getCount(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            throw new RuntimeException("结果集为空");
        }
        resultSet.next();
        int count = resultSet.getInt(1);
        return count;

    }


}
