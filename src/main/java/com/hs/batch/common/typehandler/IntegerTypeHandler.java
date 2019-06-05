package com.hs.batch.common.typehandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Integer integer, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, NumberUtils.toInt(integer == null ? "" : integer.toString(), 0));
    }

    @Override
    public Integer getNullableResult(ResultSet resultSet, String s) throws SQLException {
        try {
            return resultSet.getInt(s);
        } catch (SQLException e) {
            return getInteger(resultSet.getString(s));
        }
    }

    private Integer getInteger(String value) throws SQLException {
        if (StringUtils.isNotBlank(value)) {
            return NumberUtils.toInt(StringUtils.trimToEmpty(value));
        } else {
            return null;
        }
    }

    @Override
    public Integer getNullableResult(ResultSet resultSet, int i) throws SQLException {
        try {
            return resultSet.getInt(i);
        } catch (SQLException e) {
            return getInteger(resultSet.getString(i));
        }
    }

    @Override
    public Integer getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        try {
            return callableStatement.getInt(i);
        } catch (SQLException e) {
            return getInteger(callableStatement.getString(i));
        }
    }
}
