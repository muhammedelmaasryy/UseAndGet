package com.useandget.util;

@FunctionalInterface
public interface RowMapper<T> {

    T map(String[] columns) throws RowParseException;
}
