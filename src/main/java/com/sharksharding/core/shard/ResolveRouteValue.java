/*
 * Copyright 2015-2101 gaoxianglong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sharksharding.core.shard;

import java.util.List;
import com.sharksharding.exception.SqlParserException;
import com.sharksharding.sql.ast.SQLStatement;
import com.sharksharding.sql.ast.statement.SQLDeleteStatement;
import com.sharksharding.sql.ast.statement.SQLInsertStatement;
import com.sharksharding.sql.ast.statement.SQLSelect;
import com.sharksharding.sql.ast.statement.SQLSelectQueryBlock;
import com.sharksharding.sql.ast.statement.SQLSelectStatement;
import com.sharksharding.sql.ast.statement.SQLUpdateStatement;
import com.sharksharding.sql.dialect.mysql.parser.MySqlStatementParser;
import com.sharksharding.sql.parser.SQLStatementParser;

/**
 * 解析sql语句中的路由参数
 * 
 * @author gaoxianglong
 * 
 * @version 1.3.5
 */
public class ResolveRouteValue {
	/**
	 * 解析路由参数
	 * 
	 * @author gaoxianglong
	 * 
	 * @param sql
	 * 
	 * @param rules
	 *            配置文件中的路由条件字段集合
	 * 
	 * @return long 路由条件
	 */
	public static long getRoute(String sql, List<String> rules) {
		long route = -1L;
		/* 生成AST抽象语法树 */
		SQLStatementParser parser = new MySqlStatementParser(sql);
		List<SQLStatement> statements = parser.parseStatementList();
		if (!statements.isEmpty()) {
			SQLStatement statement = statements.get(0);
			if (statement instanceof SQLSelectStatement) {
				SQLSelectStatement selectStatement = (SQLSelectStatement) statement;
				SQLSelect sqlselect = selectStatement.getSelect();
				SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) sqlselect.getQuery();
				List<String> values = queryBlock.getWhere_();
				if (!values.isEmpty()) {
					String firstParam = values.get(0);
					if (isRoute(firstParam, rules, sql)) {
						route = Long.parseLong(values.get(2));
					}
				}
			} else if (statement instanceof SQLInsertStatement) {
				SQLInsertStatement insertStatement = (SQLInsertStatement) statement;
				String firstParam = insertStatement.getColumns().get(0).toString();
				if (isRoute(firstParam, rules, sql)) {
					route = Long.parseLong(insertStatement.getValues().getValues().get(0).toString());
				}
			} else if (statement instanceof SQLDeleteStatement) {
				SQLDeleteStatement deleteStatement = (SQLDeleteStatement) statement;
				List<String> values = deleteStatement.getWhere_();
				if (!values.isEmpty()) {
					String firstParam = values.get(0);
					if (isRoute(firstParam, rules, sql)) {
						route = Long.parseLong(values.get(2));
					}
				}
			} else if (statement instanceof SQLUpdateStatement) {
				SQLUpdateStatement updateStatement = (SQLUpdateStatement) statement;
				List<String> values = updateStatement.getWhere_();
				if (!values.isEmpty()) {
					String firstParam = values.get(0);
					if (isRoute(firstParam, rules, sql)) {
						route = Long.parseLong(values.get(2));
					}
				}
			}
		}
		return route;
	}

	/**
	 * 检测sql语句中的第一个字段是否是shardkey
	 *
	 * @author gaoxianglong
	 * 
	 * @param firstParam
	 *            sql语句中执行条件的第一个参数
	 * 
	 * @param rules
	 *            配置文件中的路由条件字段集合
	 * 
	 * @param sql
	 * 
	 * @throws ShardException
	 * 
	 * @return boolean 检测结果
	 */
	private static boolean isRoute(String firstParam, List<String> rules, String sql) {
		boolean result = true;
		String[] values = firstParam.split("\\.");
		/* 验证是否使用有别名 */
		switch (values.length) {
		case 2:
			firstParam = values[1];
			break;
		default:
			firstParam = values[0];
		}
		if (!rules.contains(firstParam)) {
			result = false;
			throw new SqlParserException("can not find shardkey,sql-->" + sql);
		}
		return result;
	}
}