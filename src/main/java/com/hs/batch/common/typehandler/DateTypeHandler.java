package com.hs.batch.common.typehandler;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class DateTypeHandler extends BaseTypeHandler<Date> {

    private Map<Integer, String> datePattern = ImmutableMap.of(8, "yyyyMMdd",
            10, "yyyyMMddHH", 12, "yyyyMMddHHmm", 14, "yyyyMMddHHmmss");

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Date date, JdbcType jdbcType) throws SQLException {
        throw new UnsupportedOperationException("date type is not supported.");
    }

    @Override
    public Date getNullableResult(ResultSet resultSet, String s) throws SQLException {
        try {
            return resultSet.getDate(s);
        }catch (Exception e) {
            return getDate(resultSet.getString(s));
        }
    }

    @Override
    public Date getNullableResult(ResultSet resultSet, int i) throws SQLException {
        try {
            return resultSet.getDate(i);
        }catch (Exception e) {
            return getDate(resultSet.getString(i));
        }
    }

    @Override
    public Date getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        try {
            return callableStatement.getDate(i);
        }catch (Exception e) {
            return getDate(callableStatement.getString(i));
        }
    }

    private Date getDate(String dateString) {
        String pattern = datePattern.get(dateString.length());
        if (pattern != null) {
            try {
                return DateUtils.parseDate(pattern, dateString);
            } catch (ParseException e1) {
                return null;
            }
        } else {
            return null;
        }
    }
}
