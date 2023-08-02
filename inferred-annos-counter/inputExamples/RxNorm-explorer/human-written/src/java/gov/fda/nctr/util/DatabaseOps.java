package gov.fda.nctr.util;

import java.sql.*;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseOps
{
  private static final Logger log = LogManager.getLogger(DatabaseOps.class);

  public static int[] deleteFromTables(Connection conn, String... tables)
  {
    String[] sqls = Arrays.stream(tables).map(t -> "delete from " + t).toArray(String[]::new);

    return executeUpdates(conn, sqls);
  }

  public static int countRecords(Connection conn, String table)
  {
    try (Statement stmt = conn.createStatement())
    {
      try (ResultSet rs = stmt.executeQuery("select count(*) count from " + table))
      {
        rs.next();
        return rs.getInt(1);
      }
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static String recordCountsReport(String title, String[] tables, Connection conn)
  {
    int[] counts = Arrays.stream(tables).mapToInt(t -> countRecords(conn, t)).toArray();
    return recordCountsReport(title, counts, tables);
  }

  public static String recordCountsReport(String title, int[] counts, String[] tables)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(title).append("\n");
    for (int i=0; i<counts.length; ++i)
      sb.append("  ").append(tables[i]).append(": ").append(counts[i]).append("\n");
    return sb.toString();
  }

  public static int[] executeUpdates(Connection conn, String... updateSqls)
  {
    int[] res = new int[updateSqls.length];

    try (Statement stmt = conn.createStatement())
    {
      for (int i = 0; i < res.length; ++i)
        res[i] = stmt.executeUpdate(updateSqls[i]);

      return res;
    }
    catch(SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static void executeTransaction(Supplier<Connection> connMaker, DatabaseOp op)
  {
    Connection conn = connMaker.get();

    try
    {
      conn.setAutoCommit(false);

      op.execute(conn);

      conn.commit();
    }
    catch(Throwable t)
    {
      log.info("Performing rollback due to error: " + Nullables.or(t.getMessage(), ""));
      try { conn.rollback(); }
      catch(SQLException e) { System.err.println("Failed to rollback transaction: " + e.getMessage());  }
      throw new RuntimeException(t);
    }
    finally
    {
      try { conn.close(); }
      catch(SQLException e) { System.err.println("Failed to close connection: " + e.getMessage()); }
    }
  }

  public interface DatabaseOp
  {
    void execute(Connection conn) throws SQLException;
  }
}
