package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

public class Column<T> {

    public final String name;

    public final Class<T> type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean indexed;

    public final boolean unique;

    public Column(String name, Class<T> type, boolean nullable, boolean primaryKey, boolean indexed, boolean unique) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.indexed = indexed;
        this.unique = unique;
    }

    /**
     *
     * @return A string representation of its SQLite data type
     */
    @NonNull
    public String getSqlType() {
        return DataTypes.getSqliteType(type);
    }

    @NonNull
    @Override
    public String toString() {
        return name + " " + getSqlType();
    }
}
