package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.CodeBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SqlGenerator {

    public String buildCreateTableStatement(SchemaDefinition schema) {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ");
        appendIdentifier(sb, schema.getTableName());
        sb.append(" (");

        int nColumns = schema.getColumns().size();
        for (int i = 0; i < nColumns; i++) {
            ColumnDefinition column = schema.getColumns().get(i);
            appendColumnDef(sb, column);

            if ((i + 1) != nColumns) {
                sb.append(", ");
            }
        }

        sb.append(')');

        return sb.toString();

    }

    public void appendColumnDef(StringBuilder sb, ColumnDefinition column) {
        appendIdentifier(sb, column.columnName);
        sb.append(' ');

        sb.append(SqlTypes.getSqliteType(column.getRawType()));
        sb.append(' ');

        List<String> constraints = new ArrayList<>();

        if (column.primaryKey) {
            constraints.add("PRIMARY KEY");
        } else {
            if (column.unique) {
                constraints.add("UNIQUE");
            }
            if (column.nullable) {
                constraints.add("NULL");
            } else {
                constraints.add("NOT NULL");
            }
        }

        if (!Strings.isEmpty(column.defaultExpr)) {
            constraints.add("DEFAULT (" + column.defaultExpr + ")");
        }

        if (!Strings.isEmpty(column.collate)) {
            constraints.add("COLLATE " + column.collate);
        }

        sb.append(constraints.stream().collect(Collectors.joining(" ")));
    }

    public CodeBlock buildCreateIndexStatements(SchemaDefinition schema) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> indexedColumns = new ArrayList<>();
        schema.getColumns().forEach(column -> {
            if (column.indexed && !column.primaryKey) {
                indexedColumns.add(column);
            }
        });

        if (indexedColumns.isEmpty()) {
            return builder.addStatement("return $T.emptyList()", Types.Collections).build();
        }

        builder.add("return $T.asList(\n", Types.Arrays).indent();

        int nColumns = indexedColumns.size();
        for (int i = 0; i < nColumns; i++) {
            ColumnDefinition column = indexedColumns.get(i);
            StringBuilder sb = new StringBuilder();

            sb.append("CREATE INDEX ");
            appendIdentifier(sb, "index_" + column.columnName + "_on_" + schema.getTableName());
            sb.append(" ON ");
            appendIdentifier(sb, schema.getTableName());
            sb.append(" (");
            appendIdentifier(sb, column.columnName);
            sb.append(")");

            builder.add("$S", sb);

            if ((i + 1) != nColumns) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }
        builder.unindent().add(");\n");

        return builder.build();
    }

    public String buildDropTableStatement(SchemaDefinition schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS ");
        appendIdentifier(sb, schema.getTableName());
        return sb.toString();
    }


    public String buildInsertStatement(SchemaDefinition schema) {
        StringBuilder sb = new StringBuilder();

        sb.append("INSERT OR ROLLBACK INTO ");
        appendIdentifier(sb, schema.getTableName());
        sb.append(" (");

        List<ColumnDefinition> columns = schema.getColumns();
        int nColumns = columns.size();
        for (int i = 0; i < nColumns; i++) {
            ColumnDefinition c = columns.get(i);
            if (c.autoId) {
                continue;
            }
            appendIdentifier(sb, c.columnName);
            if ((i + 1) != nColumns && !columns.get(i + 1).autoId) {
                sb.append(',');
            }
        }
        sb.append(')');
        sb.append(" VALUES (");
        for (int i = 0; i < nColumns; i++) {
            ColumnDefinition c = columns.get(i);
            if (c.autoId) {
                continue;
            }
            sb.append('?');
            if ((i + 1) != nColumns && !columns.get(i + 1).autoId) {
                sb.append(',');
            }
        }
        sb.append(')');

        return sb.toString();
    }


    public void appendIdentifier(StringBuilder sb, String identifier) {
        sb.append('"');
        sb.append(identifier);
        sb.append('"');
    }

}
