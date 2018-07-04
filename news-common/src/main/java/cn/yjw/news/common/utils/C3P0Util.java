/**
 * C3P0Util.java
 * util
 * Copyright (c) 2017, 海牛学院版权所有.
*/

package cn.yjw.news.common.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;


/**
 * C3P0连接池工具类
 * @author    杨锦文
 * @Date	 2017年10月12日 	 
 */
public class C3P0Util
{
	static Logger log = Logger.getLogger(C3P0Util.class);
	private static ComboPooledDataSource pool;
	static
	{
		pool = new ComboPooledDataSource();
		System.out.println("连接池开启");
	}

	public static Connection getConnection()
	{
		Connection con = null;
		try
		{
			synchronized (C3P0Util.class)
			{
				con = pool.getConnection();
			}
		} catch (SQLException e)
		{
			log.error(e.toString()+"获取连接失败");
			return null;
		}

		return con;
	}

	public static void close(Connection conn, PreparedStatement pst, ResultSet rs)
	{
		if (rs != null)
		{
			try
			{
				rs.close();
			} catch (SQLException e)
			{
				log.error(e.toString()+"ResultSet关闭失败");
			}
		}
		if (pst != null)
		{
			try
			{
				pst.close();
			} catch (SQLException e)
			{
				log.error(e.toString()+"PreparedStatement关闭失败");
			}
		}

		if (conn != null)
		{
			try
			{
				conn.close();
			} catch (SQLException e)
			{
				log.error(e.toString()+"Connection关闭失败");
			}
		}
	}

}
