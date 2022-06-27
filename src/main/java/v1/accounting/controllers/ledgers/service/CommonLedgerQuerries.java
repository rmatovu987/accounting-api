package v1.accounting.controllers.ledgers.service;

import io.agroal.api.AgroalDataSource;
import v1.accounting.domains.Ledger;
import v1.authentication.domains.Business;
import v1.statics.TimeConverter;
import v1.statics._StatusTypes_Enum;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import v1.accounting.controllers.ledgers.service.payload.*;

@ApplicationScoped
public class CommonLedgerQuerries {

    @Inject
    AgroalDataSource dataSource;

    @Inject
//    changesinequitystatement service;

    public BigDecimal getBalance(Ledger ledger) {
        BigDecimal amount = new BigDecimal("0");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            Statement st = connection.createStatement();
            ResultSet set = st.executeQuery("select"
                    + " sum(a.debit + a.credit) as amount"
                    + " from JournalEntry a"
                    + " inner join Journal d on a.journal_id=d.id"
                    + " where a.ledger_id = " + ledger.id
                    + " and d.status='" + _StatusTypes_Enum.PUBLISHED + "'");

            while (set.next()) {
                if (set.getBigDecimal("amount") != null) {
                    amount = amount.add(set.getBigDecimal("amount"));
                }
            }
            st.close();
            set.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return amount;
    }

    public BigDecimal getBalanceByDate(Long date, Ledger ledger) {
        BigDecimal amount = new BigDecimal("0");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            Statement st = connection.createStatement();
            ResultSet set = st.executeQuery("select"
                    + " sum(a.debit + a.credit) as amount"
                    + " from JournalEntry a"
                    + " inner join Journal d on a.journal_id=d.id"
                    + " where a.ledger_id = " + ledger.id
                    + " and d.status='" + _StatusTypes_Enum.PUBLISHED + "'"
                    + " and a.date <= " + date);

            while (set.next()) {
                if (set.getBigDecimal("amount") != null) {
                    amount = amount.add(set.getBigDecimal("amount"));
                }
            }
            st.close();
            set.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return amount;
    }

    public BigDecimal getBalanceByDateBranch(Long date, Ledger ledger, Business business) {


        BigDecimal amount = BigDecimal.ZERO;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            String qry = "select "
                    + "sum(a.debit+a.credit) as amount "
                    + "from JournalEntry a "
                    + "inner join Journal b on a.journal_id=b.id "
                    + "where a.ledger_id=" + ledger.id
                    + " and b.status='" + _StatusTypes_Enum.PUBLISHED
                    + "' and a.date <= " + date
                    + " and a.business_id = "+business.id;

            Statement st = connection.createStatement();
            ResultSet set = st.executeQuery(qry);

            while (set.next()) {
                if (set.getBigDecimal("amount") != null) {
                    amount = amount.add(set.getBigDecimal("amount"));
                }

            }
            st.close();
            set.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return amount;
    }

    public COAbalance getBalanceChart(Ledger ledger, Business business) {

        Connection connection = null;

        BigDecimal realValue = BigDecimal.ZERO;
        BigDecimal feckValue = BigDecimal.ZERO;

        try {
            connection = dataSource.getConnection();

            /// fetch the real value of the ledger
            Statement stmt = connection.createStatement();
            String qry = "select "
                    + "sum(a.debit+a.credit) as amount "
                    + "from JournalEntry a "
                    + "inner join Journal b on a.journal_id=b.id "
                    + "where a.ledger_id=" + ledger.id
                    + " and b.status='" + _StatusTypes_Enum.PUBLISHED
                    + "'"
                    + "and a.business_id = "+business.id;

            ResultSet rs = stmt.executeQuery(qry);

            while (rs.next()) {
                if (rs.getBigDecimal("amount") != null) {
                    realValue = rs.getBigDecimal("amount");
                }
            }
            rs.close();
            stmt.close();

            /// fetch the feck value of the ledger
            if (ledger.hasSubAccounts) {
                Statement stmt1 = connection.createStatement();
                String qry1 = null;

                qry1 = "select " + "sum(a.debit+a.credit) as amount " + "from JournalEntry a "
                        + "inner join Journal b on a.journal_id=b.id " + "inner join Ledger c on a.ledger_id=c.id "
                        + "where " + "(c.id='" + ledger.id + "' or c.code like '" + ledger.code + "%')"
                        + " and  b.status='" + _StatusTypes_Enum.PUBLISHED + "'"
                        + "and a.business_id = "+business.id;

                ResultSet rs1 = stmt1.executeQuery(qry1);
                while (rs1.next()) {
                    if (rs1.getBigDecimal("amount") != null) {
                        feckValue = rs1.getBigDecimal("amount");
                    }

                    if (ledger.systemName.equals("EQUITY") || ledger.systemName.equals("RETAINED_EARNINGS")) {
//                        feckValue = feckValue
//                                .add(service.calculateNetIncome(null, null, TimeConverter.EpochMillis_Now(), user));
                    }
                }
                rs1.close();
                stmt1.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return new COAbalance(realValue, feckValue);

    }

}
