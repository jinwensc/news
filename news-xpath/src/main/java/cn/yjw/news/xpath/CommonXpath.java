/**
 * CommonXpath.java
 * cn.yjw.news.xpath
 * Copyright (c) 2018, 海牛学院版权所有.
*/

package cn.yjw.news.xpath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.yjw.news.common.utils.C3P0Util;

/**
 * 读取mysql,统计每个host出现次数最多的xpath,上传到mysql
 * 
 * @author    杨锦文
 * @Date	 2018年1月21日 	 
 */
public class CommonXpath {
	private String sql = null;
	private Connection connection = C3P0Util.getConnection();

	/**
	 * 记录查询结果的类
	 */
	@SuppressWarnings("unused")
	private class Record {
		private String host = "";
		private String xpath = "";
		private int xpathCount = 0;
		private int total = 0;
		private float rat = 0f;

		public Record(String host, String xpath, int xpathCount, int total, float rat) {
			super();
			this.host = host;
			this.xpath = xpath;
			this.xpathCount = xpathCount;
			this.total = total;
			this.rat = rat;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getXpath() {
			return xpath;
		}

		public void setXpath(String xpath) {
			this.xpath = xpath;
		}

		public int getXpathCount() {
			return xpathCount;
		}

		public void setXpathCount(int xpathCount) {
			this.xpathCount = xpathCount;
		}

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public float getRat() {
			return rat;
		}

		public void setRat(float rat) {
			this.rat = rat;
		}

	}

	/**
	 * 统计xpath
	*/
	private void realXpath() {
		List<Record> list = getXpath();

		PreparedStatement prepareStatement = null;
		sql = "insert into news_host_xpath (host,xpath,total,rat) values (?,?,?,?) ON DUPLICATE KEY UPDATE xpath=values(xpath),total=values(total),rat=values(rat);";

		int temp = 0;
		int size = 100;//100条数据,插入更新一次

		try {
			prepareStatement = connection.prepareStatement(sql);

			/**
			 * 100条插入一次
			 */
			for (Iterator<Record> iterator = list.iterator(); iterator.hasNext();) {

				temp++;
				Record record = (Record) iterator.next();
				;
				record.getXpath();
				record.getTotal();
				record.getRat();
				prepareStatement.setString(1, record.getHost());
				prepareStatement.setString(2, record.getXpath());
				prepareStatement.setInt(3, record.getTotal());
				prepareStatement.setFloat(4, record.getRat());
				prepareStatement.addBatch();
				if (iterator.hasNext() && temp < size) {
				} else {
					temp = 0;
					prepareStatement.executeBatch();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			C3P0Util.close(connection, prepareStatement, null);

		}

	}

	/**
	 * 获取mysql中的xpath
	 */
	public List<Record> getXpath() {
		sql = "select a.host ,a.xpath ,MAX(a.sum_count),a.total_count ,a.rat from("
				+ "select host,xpath,SUM(count) sum_count,SUM(total) total_count,SUM(count)/SUM(total) rat from news_host_xpaths "
				+ "GROUP BY news_host_xpaths.host,news_host_xpaths.xpath HAVING sum_count>1 "
				+ ") a WHERE  a.total_count > 20 GROUP BY a.host;";
		Statement createStatement = null;
		ResultSet resultSet = null;
		List<Record> list = new ArrayList<Record>();
		try {
			createStatement = connection.createStatement();
			resultSet = createStatement.executeQuery(sql);
			//保存查询结果到list
			while (resultSet.next()) {
				list.add(new Record(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3),
						resultSet.getInt(4), resultSet.getFloat(5)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (createStatement != null) {
				try {
					createStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	public static void main(String[] args) {
		new CommonXpath().realXpath();

	}

}
