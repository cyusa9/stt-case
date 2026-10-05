import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Dependency-free harness. Run with "--tsv" to print machine-readable results. */
public class EmployeeManagementSystemTest {

    interface Body { String run() throws Exception; }

    static class Case {
        final String id, expected; final Body body;
        String actual; boolean pass;
        Case(String id, String expected, Body body) { this.id = id; this.expected = expected; this.body = body; }
    }

    static final List<Case> CASES = new ArrayList<>();

    static void add(String id, String expected, Body body) { CASES.add(new Case(id, expected, body)); }

    static EmployeeManagementSystem fresh() { return new EmployeeManagementSystem(); }

    /** Runs the action and returns "<ExceptionName> thrown." or "No exception." */
    static String thrown(Runnable r) {
        try { r.run(); return "No exception."; }
        catch (Exception e) { return e.getClass().getSimpleName() + " thrown."; }
    }

    static String money(double v) { return String.format(Locale.US, "Net pay = %.2f.", v); }

    public static void main(String[] args) {
        // ---------------- Black-box (equivalence partitioning + boundary values) ----------------
        add("BB-01", "Employee added; count = 1.", () -> {
            EmployeeManagementSystem s = fresh();
            boolean ok = s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            return ok && s.getEmployeeCount() == 1 ? "Employee added; count = 1." : "FAILED";
        });
        add("BB-02", "Rejected; count = 1.", () -> {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            boolean second = s.addEmployee("E001", "Bob", "Finance", 2000);
            return !second && s.getEmployeeCount() == 1 ? "Rejected; count = 1." : "FAILED";
        });
        add("BB-03", "IllegalArgumentException thrown.", () ->
            thrown(() -> fresh().addEmployee("", "Alice", "Engineering", 3000)));
        add("BB-04", "IllegalArgumentException thrown.", () ->
            thrown(() -> fresh().addEmployee("E001", "Alice", "Engineering", -100)));
        add("BB-05", "Removed; count = 0.", () -> {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            boolean removed = s.removeEmployee("E001");
            return removed && s.getEmployeeCount() == 0 ? "Removed; count = 0." : "FAILED";
        });
        add("BB-06", "Returned false.", () ->
            fresh().removeEmployee("E999") ? "FAILED" : "Returned false.");
        add("BB-07", "Net pay = 2550.00.", () -> {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            return money(s.processPayment("E001", 160, 0));
        });
        add("BB-08", "Net pay = 4950.00.", () -> {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("M001", "Grace", "Management", 6000);
            return money(s.processPayment("M001", 160, 0));
        });
        add("BB-09", "IllegalArgumentException thrown.", () -> {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            return thrown(() -> s.processPayment("E001", -10, 0));
        });
        add("BB-10", "NoSuchElementException thrown.", () ->
            thrown(() -> fresh().processPayment("E999", 160, 0)));
        add("BB-11", "Report contained all expected fields.", () -> {
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice Uwase", "Engineering", 3000);
            s.processPayment("E001", 160, 0);
            String r = s.generatePayrollReport();
            boolean ok = r.contains("E001") && r.contains("Total payroll: 2550.00")
                    && r.contains("Average net pay: 2550.00") && r.contains("Top earner: Alice Uwase (E001)");
            return ok ? "Report contained all expected fields." : "FAILED: " + r;
        });
        add("BB-12", "Message returned exactly.", () ->
            "No employees to report.".equals(fresh().generatePayrollReport())
                    ? "Message returned exactly." : "FAILED");

        // ---------------- White-box (branch coverage of each method) ----------------
        add("WB-01", "IllegalArgumentException thrown.", () ->               // A1 true (id == null)
            thrown(() -> fresh().addEmployee(null, "Alice", "Engineering", 3000)));
        add("WB-02", "IllegalArgumentException thrown.", () ->               // A1 false, A2 true (blank name)
            thrown(() -> fresh().addEmployee("E001", "   ", "Engineering", 3000)));
        add("WB-03", "IllegalArgumentException thrown.", () ->               // A3 true (NaN salary)
            thrown(() -> fresh().addEmployee("E001", "Alice", "Engineering", Double.NaN)));
        add("WB-04", "Added; department = Unassigned.", () -> {              // A3 false at boundary 0; A5 true
            EmployeeManagementSystem s = fresh();
            boolean ok = s.addEmployee("E001", "Alice", null, 0);
            return ok && "Unassigned".equals(s.getDepartment("E001"))
                    ? "Added; department = Unassigned." : "FAILED";
        });
        add("WB-05", "Returned false; original record kept.", () -> {        // A4 true (duplicate)
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            boolean dup = s.addEmployee("E001", "Mallory", "Finance", 1);
            return !dup && "Engineering".equals(s.getDepartment("E001"))
                    ? "Returned false; original record kept." : "FAILED";
        });
        add("WB-06", "First call true, second call false.", () -> {          // D1 true and false
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            boolean first = s.removeEmployee("E001");
            boolean second = s.removeEmployee("E001");
            return first && !second ? "First call true, second call false." : "FAILED";
        });
        add("WB-07", "IllegalArgumentException thrown.", () ->               // P1 true (NaN hours)
            thrown(() -> {
                EmployeeManagementSystem s = fresh();
                s.addEmployee("E001", "Alice", "Engineering", 3000);
                s.processPayment("E001", Double.NaN, 0);
            }));
        add("WB-08", "IllegalArgumentException thrown.", () ->               // P1 false, P2 true
            thrown(() -> {
                EmployeeManagementSystem s = fresh();
                s.addEmployee("E001", "Alice", "Engineering", 3000);
                s.processPayment("E001", 160, -5);
            }));
        add("WB-09", "NoSuchElementException thrown.", () ->                 // P1, P2 false; P3 true (null id)
            thrown(() -> fresh().processPayment(null, 160, 0)));
        add("WB-10", "Net pay = 2975.00.", () -> {                           // overtime term; P4 false; P5 false
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3200);             // hourly 20; gross 3200 + 300
            return money(s.processPayment("E001", 160, 10));
        });
        add("WB-11", "Net pay = 3740.00.", () -> {                           // P4 true (case-insensitive); P5 false
            EmployeeManagementSystem s = fresh();
            s.addEmployee("M001", "Grace", "management", 4000);             // gross 4000 * 1.1 = 4400
            return money(s.processPayment("M001", 160, 0));
        });
        add("WB-12", "Net pay = 3750.00.", () -> {                           // P5 true at boundary gross == 5000
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 5000);
            return money(s.processPayment("E001", 160, 0));
        });
        add("WB-13", "Net pay = 4249.99.", () -> {                           // P5 false just below boundary
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 4999.99);
            return money(s.processPayment("E001", 160, 0));
        });
        add("WB-14", "Lists E001 as not processed; says no payments processed.", () -> {   // G3 false, G5 true
            EmployeeManagementSystem s = fresh();
            s.addEmployee("E001", "Alice", "Engineering", 3000);
            String r = s.generatePayrollReport();
            boolean ok = r.contains("E001 | Alice | Engineering | Net pay: not processed")
                    && r.contains("No payments have been processed.") && !r.contains("Total payroll");
            return ok ? "Lists E001 as not processed; says no payments processed." : "FAILED: " + r;
        });
        add("WB-15", "Total 7500.00; average 3750.00; top earner Grace (M001).", () -> {   // G3 both, G4 both, G5 false
            EmployeeManagementSystem s = fresh();
            s.addEmployee("M001", "Grace", "Management", 6000);              // net 4950.00 (first paid -> top)
            s.addEmployee("E002", "Bob", "Finance", 2000);                   // left unprocessed
            s.addEmployee("E003", "Alice", "Engineering", 3000);             // net 2550.00 (not > top)
            s.processPayment("M001", 160, 0);
            s.processPayment("E003", 160, 0);
            String r = s.generatePayrollReport();
            boolean ok = r.contains("E002 | Bob | Finance | Net pay: not processed")
                    && r.contains("Total payroll: 7500.00") && r.contains("Average net pay: 3750.00")
                    && r.contains("Top earner: Grace (M001) - 4950.00");
            return ok ? "Total 7500.00; average 3750.00; top earner Grace (M001)." : "FAILED: " + r;
        });

        // ---------------- Execute ----------------
        boolean tsv = args.length > 0 && "--tsv".equals(args[0]);
        int passed = 0;
        for (Case c : CASES) {
            try { c.actual = c.body.run(); }
            catch (Exception e) { c.actual = "Unexpected " + e; }
            c.pass = c.expected.equals(c.actual);
            if (c.pass) passed++;
            if (tsv) System.out.println(c.id + "\t" + c.expected + "\t" + c.actual + "\t" + (c.pass ? "Pass" : "Fail"));
            else System.out.printf("%-6s %-4s expected=[%s] actual=[%s]%n", c.id, c.pass ? "PASS" : "FAIL", c.expected, c.actual);
        }
        if (!tsv) System.out.printf("%nSummary: %d/%d passed, %d failed.%n", passed, CASES.size(), CASES.size() - passed);
        if (passed != CASES.size()) System.exit(1);
    }
}
