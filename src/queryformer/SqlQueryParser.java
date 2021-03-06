package queryformer;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TBaseType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.*;
import gudusoft.gsqlparser.stmt.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static gudusoft.gsqlparser.ESqlStatementType.sstinformixOutput;
import static gudusoft.gsqlparser.ESqlStatementType.sstselect;

/*
* original name of this demo was Parsing DDL,
* renamed to analyzescript on 2011-11-09
*/

public class SqlQueryParser {
    public static void main(String args[]) {

//        if (args.length != 1) {
//            System.out.println("Usage: java AnalyzeScript sqlfile.sql");
//            return;
//        }
//        File file = new File(args[0]);
//        if (!file.exists()) {
//            System.out.println("File not exists:" + args[0]);
//            return;
//        }

        /*EDbVendor dbVendor = EDbVendor.dbvoracle;
        String msg = "Please select SQL dialect: 1: SQL Server, 2: Oralce, 3: MySQL, 4: DB2, 5: PostGRESQL, 6: Teradta, default is 2: Oracle";
        System.out.println(msg);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            int db = Integer.parseInt(br.readLine());
            if (db == 1) {
                dbVendor = EDbVendor.dbvmssql;
            } else if (db == 2) {
                dbVendor = EDbVendor.dbvoracle;
            } else if (db == 3) {
                dbVendor = EDbVendor.dbvmysql;
            } else if (db == 4) {
                dbVendor = EDbVendor.dbvdb2;
            } else if (db == 5) {
                dbVendor = EDbVendor.dbvpostgresql;
            } else if (db == 6) {
                dbVendor = EDbVendor.dbvteradata;
            }
        } catch (IOException i) {
        } catch (NumberFormatException numberFormatException) {
        }

        System.out.println("Selected SQL dialect: " + dbVendor.toString());*/

        // Init BD type: MySQL, Oracle etc...
        EDbVendor dbVendor = EDbVendor.dbvmysql;
        // Init The General SQL Parser(TGSqlParser) class and send him selected DB type(dbVendor)
        TGSqlParser sqlparser = new TGSqlParser(dbVendor);

        /**
         * At first write sqlQuery for analise
         */

//        String sqlQuery = "  SELECT    userName   " +
//                "     FROM         user  WHERE     user.id > 2;";

//        String sqlQuery = "  SELECT    user.name,user.email     FROM         user  WHERE     user.id > 2 ORDER BY name ASC ;";

        String sqlQuery = "  SELECT    user.name,user.email     FROM         user  WHERE     user.id > 2 AND user.email = 'vasa@gmail.com' OR user.email = 'peta@gmail.com'" +
                "ORDER BY name DESC LIMIT 10, 50;";

//        String sqlQuery = "SELECT * FROM user;";
//        String sqlQuery = "INSERT INTO user(userName) VAlUES ('Kola');";


        // clean at the start and end of sqlQuery
        sqlQuery = sqlQuery.trim();
        // clean more then 2 white space
        sqlQuery = sqlQuery.replaceAll("(\\s){2,}", " ");//good

        // add our sqlQuery to the General SQL Parser class
        sqlparser.sqltext = sqlQuery;


        // Parse sqlQuery and get int result for check syntax and other parameters
        int ret = sqlparser.parse();

        // ret == 0 means that sqlQuery has correct syntax and was parsed
        if (ret == 0) {
            // Move through the statements. sqlparser.sqlstatements.get(i) - its statement created from our entered sqlQuery
            for (int i = 0; i < sqlparser.sqlstatements.size(); i++) {

                /**
                 * Check that is a SELECT statement
                 * sqlparser.sqlstatements.get(i).sqlstatementtype - type of the statement created from our entered sqlQuery
                 */
                if (!sqlparser.sqlstatements.get(i).sqlstatementtype.equals(sstselect)) {
                    System.out.println("ERROR: Not SELECT statement!");
                }

                System.out.println("Check syntax ok!");
                System.out.println("My Tests.");
                System.out.println("======================");
                System.out.println("======================");
                System.out.println("Statement type: " + sqlparser.sqlstatements.get(i).sqlstatementtype);

                // Берем конкретно первый статмент - это наша sqlQuery
                TCustomSqlStatement stmt = sqlparser.sqlstatements.get(i);

//                TResultColumn resultColumn = stmt.getResultColumnList().getResultColumn(i);

                List<Object> projections = new ArrayList<>();

                // Для получения всех колонок нужно пройтись цикром по статменту.
                // Move with loop for get all columns from statement
                for (int j = 0; j < stmt.getResultColumnList().size(); j++) {
                    // Движемся по стейтменту с итерацией по колонкам j.  stmt.getResultColumnList().getResultColumn(j);
                    // Moving through the statement with iterate by column j.
                    TResultColumn resultColumn = stmt.getResultColumnList().getResultColumn(j);
                    // Gets only column name from the current column list. Example name
                    System.out.println("resultColumn.getColumnNameOnly(): " + resultColumn.getColumnNameOnly());
                    // Gets full expression from the current column list. Example user.name
                    System.out.println("resultColumn.getExpr(): " + resultColumn.getExpr());
                    // Получает конечный результат с выражения типо user.name - результатом будет name
                    // Gets end token from the current expression like a user.name - result will be name
                    System.out.println("resultColumn.getEndToken(): " + resultColumn.getEndToken());
                    // Получает начальный результат с выражения типо user.name - результатом будет user
                    // Gets start token from the current expression like a user.name - result will be user
                    System.out.println("resultColumn.getStartToken(): " + resultColumn.getStartToken());

                    projections.add(resultColumn.getColumnNameOnly());
                }

                System.out.println("Projections:");
                Projection projection = new Projection(projections);
                projection.parseSqlBlock(projection.getProjections());

                // Parse FROM case
                parseFromCase((TSelectSqlStatement) stmt, sqlparser);

                // Parse Where case
                parseWhereCase((TSelectSqlStatement) stmt);

                // Parse Order By case
                parseOrderByCase((TSelectSqlStatement) stmt);

                // Parse Limit case
                parseLimitCase((TSelectSqlStatement) stmt);

                System.out.println("======================");
                System.out.println("======================");

                analyzeStmt(sqlparser.sqlstatements.get(i));
//                System.out.println("Check syntax ok!");
            }
        } else {
            System.out.println("Incorrect SqlQuery:");
            System.out.println(sqlparser.getErrormessage());
        }
    }

    /**
     * TCustomSqlStatement - это статмент без привязки к онкретной операции, тоесть для: SELECT, INSERT, UPDATE etc.
     *
     * TSelectSqlStatement - это статмент конкретно для SELECT операции. Без проблем кастуется из TCustomSqlStatement
     */

    /**
     * Method for parse FROM case
     *
     * @param stmt
     */
    private static FromCase parseFromCase(TSelectSqlStatement stmt, TGSqlParser sqlparser) {

        List<Object> joins = new ArrayList<>();

        for (int j = 0; j < sqlparser.sqlstatements.size(); j++) {
//            TJoinList joinList = stmt.getJoins();
            joins.add(stmt.getJoins());
        }

        FromCase fromCase = new FromCase(joins);
        fromCase.parseSqlBlock(fromCase.getJoins());

        return fromCase;
    }

    /**
     * Method for parse WHERE case.
     *
     * @param stmt
     */
    private static WhereCase parseWhereCase(TSelectSqlStatement stmt) {

        List<Object> conditions = new ArrayList<>();

        //where clause
        if (stmt.getWhereClause() != null) {
            TWhereClause tWhereClaus = stmt.getWhereClause();
            // Gets a conditions from Where case. Example: user.id > 2 AND user.email = 'vasa@gmail.com'
            System.out.println("tWhereClaus.getCondition(): " + tWhereClaus.getCondition());
            conditions.add(tWhereClaus.getCondition());

            System.out.println("tWhereClaus.getCondition().getLeftOperand(): " + tWhereClaus.getCondition().getLeftOperand());
            System.out.println("tWhereClaus.getCondition().getLeftOperand().getStartToken: " + tWhereClaus.getCondition().getLeftOperand().getStartToken());
            System.out.println("tWhereClaus.getCondition().getLeftOperand().getEndToken: " + tWhereClaus.getCondition().getLeftOperand().getEndToken());
            System.out.println("tWhereClaus.getCondition().getLeftOperand().getFlattedAndOrExprs: " + tWhereClaus.getCondition().getLeftOperand().getFlattedAndOrExprs());

            parseWhereCaseExpression(tWhereClaus.getCondition());

            System.out.println("tWhereClaus.getCondition().getBetweenOperand(): " + tWhereClaus.getCondition().getBetweenOperand());
            System.out.println("tWhereClaus.getCondition().getRightOperand(): " + tWhereClaus.getCondition().getRightOperand());
            System.out.println("tWhereClaus.getCondition().getAndOrTokenBeforeExpr(): " + tWhereClaus.getCondition().getAndOrTokenBeforeExpr());
        }

        WhereCase whereCase = new WhereCase(conditions);
        whereCase.parseSqlBlock(whereCase.getConditions());

        return whereCase;
    }

     /*       '=': '$eq',
             '<>': '$ne',
             '>': '$gt',
             '<': '$lt',
             '>=': '$ge',
             '<=': '$le',

             'and': '$and',
             'or': '$or',*/

    private static void parseWhereCaseExpression(TExpression tExpression){

        String ress = tExpression.toString().replaceAll("=","\\$eq").replaceAll("<>","\\$ne").replaceAll(">","\\$gt")
                .replaceAll("<","\\$lt").replaceAll(">=","\\$ge").replaceAll("<=","\\$le").replaceAll("and", "\\$and")
                .replaceAll("or","\\$or").replaceAll("AND", "\\$and").replaceAll("OR","\\$or");

        System.out.println("parseWhereCaseExpression: " + ress);
        System.out.println(tExpression.getFlattedAndOrExprs());//выпиливает end и or щперации в массив
        System.out.println(tExpression.getLeftOperand());
        System.out.println(tExpression.getRightOperand());
        System.out.println(tExpression.getRightOperand().getAndOrTokenBeforeExpr());

    }

    /**
     * Perse ORDER BY case
     *
     * @param stmt
     */
    private static OrderByCase parseOrderByCase(TSelectSqlStatement stmt) {

        List<Object> fields = new ArrayList<>();

        // Order by case
        if (stmt.getOrderbyClause() != null) {
            TOrderBy tOrderBy = stmt.getOrderbyClause();
            // Gets fields and post condition. Example: name ASC
            System.out.println("tOrderBy.getItems(): " + tOrderBy.getItems());
            fields.add(tOrderBy.getItems());
        }

        OrderByCase orderByCase = new OrderByCase(fields);
        orderByCase.parseSqlBlock(orderByCase.getFields());

        return orderByCase;
    }

    /**
     * Parse LIMIT case
     * <p>
     * Includes the possibility to use SKIP operation as a LIMIT fromIndex toIndex.
     * Example: SELECT * FROM user LIMIT 7,70;  -  means users between 7 and 70 indexes(start on 7, end on 70).
     *
     * @param stmt
     */

    private static LimitCase parseLimitCase(TSelectSqlStatement stmt) {

        List<Object> limits = new ArrayList<>();

        // limit clause
        if (stmt.getLimitClause() != null) {
            TLimitClause tLimitClause = stmt.getLimitClause();
            System.out.println("tLimitClause.getOffset(): " + tLimitClause.getOffset());
            System.out.println("tLimitClause.getRow_count(): " + tLimitClause.getRow_count());
            System.out.println("tLimitClause.getStartToken(): " + tLimitClause.getStartToken());
            System.out.println("tLimitClause.getEndToken(): " + tLimitClause.getEndToken());
            limits.add(tLimitClause);
        }

        LimitCase limitCase = new LimitCase(limits);
        limitCase.parseSqlBlock(limitCase.getLimits());

        return limitCase;
    }


    protected static void analyzeStmt(TCustomSqlStatement stmt) {

        switch (stmt.sqlstatementtype) {
            case sstselect:
                analyzeSelectStmt((TSelectSqlStatement) stmt);
                break;
            case sstupdate:
                analyzeUpdateStmt((TUpdateSqlStatement) stmt);
                break;
            case sstinsert:
                analyzeInsertStmt((TInsertSqlStatement) stmt);
                break;
            case sstcreatetable:
                analyzeCreateTableStmt((TCreateTableSqlStatement) stmt);
                break;
            case sstaltertable:
                analyzeAlterTableStmt((TAlterTableStatement) stmt);
                break;
            case sstcreateview:
                analyzeCreateViewStmt((TCreateViewSqlStatement) stmt);
                break;
            default:
                System.out.println(stmt.sqlstatementtype.toString());
        }
    }

    protected static void printConstraint(TConstraint constraint, Boolean outline) {

        if (constraint.getConstraintName() != null) {
            System.out.println("\t\tconstraint name:" + constraint.getConstraintName().toString());
        }

        switch (constraint.getConstraint_type()) {
            case notnull:
                System.out.println("\t\tnot null");
                break;
            case primary_key:
                System.out.println("\t\tprimary key");
                if (outline) {
                    String lcstr = "";
                    if (constraint.getColumnList() != null) {
                        for (int k = 0; k < constraint.getColumnList().size(); k++) {
                            if (k != 0) {
                                lcstr = lcstr + ",";
                            }
                            lcstr = lcstr + constraint.getColumnList().getObjectName(k).toString();
                        }
                        System.out.println("\t\tprimary key columns:" + lcstr);
                    }
                }
                break;
            case unique:
                System.out.println("\t\tunique key");
                if (outline) {
                    String lcstr = "";
                    if (constraint.getColumnList() != null) {
                        for (int k = 0; k < constraint.getColumnList().size(); k++) {
                            if (k != 0) {
                                lcstr = lcstr + ",";
                            }
                            lcstr = lcstr + constraint.getColumnList().getObjectName(k).toString();
                        }
                    }
                    System.out.println("\t\tcolumns:" + lcstr);
                }
                break;
            case check:
                System.out.println("\t\tcheck:" + constraint.getCheckCondition().toString());
                break;
            case foreign_key:
            case reference:
                System.out.println("\t\tforeign key");
                if (outline) {
                    String lcstr = "";
                    if (constraint.getColumnList() != null) {
                        for (int k = 0; k < constraint.getColumnList().size(); k++) {
                            if (k != 0) {
                                lcstr = lcstr + ",";
                            }
                            lcstr = lcstr + constraint.getColumnList().getObjectName(k).toString();
                        }
                    }
                    System.out.println("\t\tcolumns:" + lcstr);
                }
                System.out.println("\t\treferenced table:" + constraint.getReferencedObject().toString());
                if (constraint.getReferencedColumnList() != null) {
                    String lcstr = "";
                    for (int k = 0; k < constraint.getReferencedColumnList().size(); k++) {
                        if (k != 0) {
                            lcstr = lcstr + ",";
                        }
                        lcstr = lcstr + constraint.getReferencedColumnList().getObjectName(k).toString();
                    }
                    System.out.println("\t\treferenced columns:" + lcstr);
                }
                break;
            default:
                break;
        }
    }

    protected static void printObjectNameList(TObjectNameList objList) {
        for (int i = 0; i < objList.size(); i++) {
            System.out.println(objList.getObjectName(i).toString());
        }

    }

    protected static void printColumnDefinitionList(TColumnDefinitionList cdl) {
        for (int i = 0; i < cdl.size(); i++) {
            System.out.println(cdl.getColumn(i).getColumnName());
        }
    }

    protected static void printConstraintList(TConstraintList cnl) {
        for (int i = 0; i < cnl.size(); i++) {
            printConstraint(cnl.getConstraint(i), true);
        }
    }

    protected static void printAlterTableOption(TAlterTableOption ato) {
        System.out.println(ato.getOptionType());
        switch (ato.getOptionType()) {
            case AddColumn:
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case ModifyColumn:
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case AlterColumn:
                System.out.println(ato.getColumnName().toString());
                break;
            case DropColumn:
                System.out.println(ato.getColumnName().toString());
                break;
            case SetUnUsedColumn:  //oracle
                printObjectNameList(ato.getColumnNameList());
                break;
            case DropUnUsedColumn:
                break;
            case DropColumnsContinue:
                break;
            case RenameColumn:
                System.out.println("rename " + ato.getColumnName().toString() + " to " + ato.getNewColumnName().toString());
                break;
            case ChangeColumn:   //MySQL
                System.out.println(ato.getColumnName().toString());
                printColumnDefinitionList(ato.getColumnDefinitionList());
                break;
            case RenameTable:   //MySQL
                System.out.println(ato.getColumnName().toString());
                break;
            case AddConstraint:
                printConstraintList(ato.getConstraintList());
                break;
            case AddConstraintIndex:    //MySQL
                if (ato.getColumnName() != null) {
                    System.out.println(ato.getColumnName().toString());
                }
                printObjectNameList(ato.getColumnNameList());
                break;
            case AddConstraintPK:
            case AddConstraintUnique:
            case AddConstraintFK:
                if (ato.getConstraintName() != null) {
                    System.out.println(ato.getConstraintName().toString());
                }
                printObjectNameList(ato.getColumnNameList());
                break;
            case ModifyConstraint:
                System.out.println(ato.getConstraintName().toString());
                break;
            case RenameConstraint:
                System.out.println("rename " + ato.getConstraintName().toString() + " to " + ato.getNewConstraintName().toString());
                break;
            case DropConstraint:
                System.out.println(ato.getConstraintName().toString());
                break;
            case DropConstraintPK:
                break;
            case DropConstraintFK:
                System.out.println(ato.getConstraintName().toString());
                break;
            case DropConstraintUnique:
                if (ato.getConstraintName() != null) { //db2
                    System.out.println(ato.getConstraintName());
                }

                if (ato.getColumnNameList() != null) {//oracle
                    printObjectNameList(ato.getColumnNameList());
                }
                break;
            case DropConstraintCheck: //db2
                System.out.println(ato.getConstraintName());
                break;
            case DropConstraintPartitioningKey:
                break;
            case DropConstraintRestrict:
                break;
            case DropConstraintIndex:
                System.out.println(ato.getConstraintName());
                break;
            case DropConstraintKey:
                System.out.println(ato.getConstraintName());
                break;
            case AlterConstraintFK:
                System.out.println(ato.getConstraintName());
                break;
            case AlterConstraintCheck:
                System.out.println(ato.getConstraintName());
                break;
            case CheckConstraint:
                break;
            case OraclePhysicalAttrs:
            case toOracleLogClause:
            case OracleTableP:
            case MssqlEnableTrigger:
            case MySQLTableOptons:
            case Db2PartitioningKeyDef:
            case Db2RestrictOnDrop:
            case Db2Misc:
            case Unknown:
                break;
        }

    }

    protected static void analyzeCreateViewStmt(TCreateViewSqlStatement pStmt) {
        TCreateViewSqlStatement createView = pStmt;
        System.out.println("View name:" + createView.getViewName().toString());
        TViewAliasClause aliasClause = createView.getViewAliasClause();
        for (int i = 0; i < aliasClause.getViewAliasItemList().size(); i++) {
            System.out.println("View alias:" + aliasClause.getViewAliasItemList().getViewAliasItem(i).toString());
        }

        System.out.println("View subquery: \n" + createView.getSubquery().toString());
    }

    protected static void analyzeSelectStmt(TSelectSqlStatement pStmt) {
        System.out.println("\nSelect:");
        if (pStmt.isCombinedQuery()) {
            String setstr = "";
            switch (pStmt.getSetOperator()) {
                case 1:
                    setstr = "union";
                    break;
                case 2:
                    setstr = "union all";
                    break;
                case 3:
                    setstr = "intersect";
                    break;
                case 4:
                    setstr = "intersect all";
                    break;
                case 5:
                    setstr = "minus";
                    break;
                case 6:
                    setstr = "minus all";
                    break;
                case 7:
                    setstr = "except";
                    break;
                case 8:
                    setstr = "except all";
                    break;
            }
            System.out.printf("set type: %s\n", setstr);
            System.out.println("left select:");
            analyzeSelectStmt(pStmt.getLeftStmt());
            System.out.println("right select:");
            analyzeSelectStmt(pStmt.getRightStmt());
            if (pStmt.getOrderbyClause() != null) {
                System.out.printf("order by clause %s\n", pStmt.getOrderbyClause().toString());
            }
        } else {
            //select list
            for (int i = 0; i < pStmt.getResultColumnList().size(); i++) {
                TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
                System.out.printf("Column: %s, Alias: %s\n", resultColumn.getExpr().toString(), (resultColumn.getAliasClause() == null) ? "" : resultColumn.getAliasClause().toString());
            }

            //from clause, check this document for detailed information
            //http://www.sqlparser.com/sql-parser-query-join-table.php
            for (int i = 0; i < pStmt.joins.size(); i++) {
                TJoin join = pStmt.joins.getJoin(i);
                switch (join.getKind()) {
                    case TBaseType.join_source_fake:
                        System.out.printf("table: %s, alias: %s\n", join.getTable().toString(), (join.getTable().getAliasClause() != null) ? join.getTable().getAliasClause().toString() : "");
                        break;
                    case TBaseType.join_source_table:
                        System.out.printf("table: %s, alias: %s\n", join.getTable().toString(), (join.getTable().getAliasClause() != null) ? join.getTable().getAliasClause().toString() : "");
                        for (int j = 0; j < join.getJoinItems().size(); j++) {
                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
                            System.out.printf("Join type: %s\n", joinItem.getJoinType().toString());
                            System.out.printf("table: %s, alias: %s\n", joinItem.getTable().toString(), (joinItem.getTable().getAliasClause() != null) ? joinItem.getTable().getAliasClause().toString() : "");
                            if (joinItem.getOnCondition() != null) {
                                System.out.printf("On: %s\n", joinItem.getOnCondition().toString());
                            } else if (joinItem.getUsingColumns() != null) {
                                System.out.printf("using: %s\n", joinItem.getUsingColumns().toString());
                            }
                        }
                        break;
                    case TBaseType.join_source_join:
                        TJoin source_join = join.getJoin();
                        System.out.printf("table: %s, alias: %s\n", source_join.getTable().toString(), (source_join.getTable().getAliasClause() != null) ? source_join.getTable().getAliasClause().toString() : "");

                        for (int j = 0; j < source_join.getJoinItems().size(); j++) {
                            TJoinItem joinItem = source_join.getJoinItems().getJoinItem(j);
                            System.out.printf("source_join type: %s\n", joinItem.getJoinType().toString());
                            System.out.printf("table: %s, alias: %s\n", joinItem.getTable().toString(), (joinItem.getTable().getAliasClause() != null) ? joinItem.getTable().getAliasClause().toString() : "");
                            if (joinItem.getOnCondition() != null) {
                                System.out.printf("On: %s\n", joinItem.getOnCondition().toString());
                            } else if (joinItem.getUsingColumns() != null) {
                                System.out.printf("using: %s\n", joinItem.getUsingColumns().toString());
                            }
                        }

                        for (int j = 0; j < join.getJoinItems().size(); j++) {
                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
                            System.out.printf("Join type: %s\n", joinItem.getJoinType().toString());
                            System.out.printf("table: %s, alias: %s\n", joinItem.getTable().toString(), (joinItem.getTable().getAliasClause() != null) ? joinItem.getTable().getAliasClause().toString() : "");
                            if (joinItem.getOnCondition() != null) {
                                System.out.printf("On: %s\n", joinItem.getOnCondition().toString());
                            } else if (joinItem.getUsingColumns() != null) {
                                System.out.printf("using: %s\n", joinItem.getUsingColumns().toString());
                            }
                        }

                        break;
                    default:
                        System.out.println("unknown type in join!");
                        break;
                }
            }

            //where clause
            if (pStmt.getWhereClause() != null) {
                System.out.printf("where clause: \n%s\n", pStmt.getWhereClause().toString());
            }

            // group by
            if (pStmt.getGroupByClause() != null) {
                System.out.printf("group by: \n%s\n", pStmt.getGroupByClause().toString());
            }

            // order by
            if (pStmt.getOrderbyClause() != null) {
                System.out.printf("order by: \n%s\n", pStmt.getOrderbyClause().toString());
            }

            // for update
            if (pStmt.getForUpdateClause() != null) {
                System.out.printf("for update: \n%s\n", pStmt.getForUpdateClause().toString());
            }

            // top clause
            if (pStmt.getTopClause() != null) {
                System.out.printf("top clause: \n%s\n", pStmt.getTopClause().toString());
            }

            // limit clause
            if (pStmt.getLimitClause() != null) {
                System.out.printf("top clause: \n%s\n", pStmt.getLimitClause().toString());
            }
        }
    }

    protected static void analyzeInsertStmt(TInsertSqlStatement pStmt) {
        if (pStmt.getTargetTable() != null) {
            System.out.println("Table name:" + pStmt.getTargetTable().toString());
        }

        System.out.println("insert value type:" + pStmt.getValueType());

        if (pStmt.getColumnList() != null) {
            System.out.println("columns:");
            for (int i = 0; i < pStmt.getColumnList().size(); i++) {
                System.out.println("\t" + pStmt.getColumnList().getObjectName(i).toString());
            }
        }

        if (pStmt.getValues() != null) {
            System.out.println("values:");
            for (int i = 0; i < pStmt.getValues().size(); i++) {
                TMultiTarget mt = pStmt.getValues().getMultiTarget(i);
                for (int j = 0; j < mt.getColumnList().size(); j++) {
                    System.out.println("\t" + mt.getColumnList().getResultColumn(j).toString());
                }
            }
        }

        if (pStmt.getSubQuery() != null) {
            analyzeSelectStmt(pStmt.getSubQuery());
        }
    }

    protected static void analyzeUpdateStmt(TUpdateSqlStatement pStmt) {
        System.out.println("Table Name:" + pStmt.getTargetTable().toString());
        System.out.println("set clause:");
        for (int i = 0; i < pStmt.getResultColumnList().size(); i++) {
            TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
            TExpression expression = resultColumn.getExpr();
            System.out.println("\tcolumn:" + expression.getLeftOperand().toString() + "\tvalue:" + expression.getRightOperand().toString());
        }
        if (pStmt.getWhereClause() != null) {
            System.out.println("where clause:\n" + pStmt.getWhereClause().getCondition().toString());
        }
    }

    protected static void analyzeAlterTableStmt(TAlterTableStatement pStmt) {
        System.out.println("Table Name:" + pStmt.getTableName().toString());
        System.out.println("Alter table options:");
        for (int i = 0; i < pStmt.getAlterTableOptionList().size(); i++) {
            printAlterTableOption(pStmt.getAlterTableOptionList().getAlterTableOption(i));
        }
    }

    protected static void analyzeCreateTableStmt(TCreateTableSqlStatement pStmt) {
        System.out.println("Table Name:" + pStmt.getTargetTable().toString());
        System.out.println("Columns:");
        TColumnDefinition column;
        for (int i = 0; i < pStmt.getColumnList().size(); i++) {
            column = pStmt.getColumnList().getColumn(i);
            System.out.println("\tname:" + column.getColumnName().toString());
            System.out.println("\tdatetype:" + column.getDatatype().toString());
            if (column.getDefaultExpression() != null) {
                System.out.println("\tdefault:" + column.getDefaultExpression().toString());
            }
            if (column.isNull()) {
                System.out.println("\tnull: yes");
            }
            if (column.getConstraints() != null) {
                System.out.println("\tinline constraints:");
                for (int j = 0; j < column.getConstraints().size(); j++) {
                    printConstraint(column.getConstraints().getConstraint(j), false);
                }
            }
            System.out.println("");
        }

        if (pStmt.getTableConstraints().size() > 0) {
            System.out.println("\toutline constraints:");
            for (int i = 0; i < pStmt.getTableConstraints().size(); i++) {
                printConstraint(pStmt.getTableConstraints().getConstraint(i), true);
                System.out.println("");
            }
        }
    }

}