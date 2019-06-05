package com.hs.batch.common.typehandler;

import com.google.common.collect.Lists;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {

    private List<String> trueConventionValue = Lists.newArrayList("1", "y", "t", "yes", "true");

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Boolean aBoolean, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, aBoolean ? 1 : 0);
    }

    @Override
    public Boolean getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String value = resultSet.getString(s);
        return trueConventionValue.contains(value == null ? "" : value.toLowerCase().trim());
    }

    @Override
    public Boolean getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String value = resultSet.getString(i);
        return trueConventionValue.contains(value == null ? "" : value.toLowerCase().trim());
    }

    @Override
    public Boolean getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String value = callableStatement.getString(i);
        return trueConventionValue.contains(value == null ? "" : value.toLowerCase().trim());
    }
}
