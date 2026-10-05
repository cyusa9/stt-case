import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Employee Management System for organization X.
 * Responsibilities: employee record management, payment processing, payroll reporting.
 */
public class EmployeeManagementSystem {

    // ---- Business rules -------------------------------------------------
    static final double STANDARD_HOURS      = 160.0;  // hours used to derive hourly rate
    static final double OVERTIME_MULTIPLIER = 1.5;
    static final double MANAGEMENT_BONUS    = 0.10;   // 10% of gross, Management dept only
    static final double HIGH_INCOME_LIMIT   = 5000.0; // gross >= limit -> high tax rate
    static final double HIGH_TAX_RATE       = 0.25;
    static final double LOW_TAX_RATE        = 0.15;
    static final String MANAGEMENT_DEPT     = "Management";

    // ---- Employee record ------------------------------------------------
    static class Employee {
        final String id;
        final String name;
        final String department;
        final double baseSalary;
        boolean paid;     // true once processPayment has been run for this employee
        double netPay;    // net pay from the most recent payment run

        Employee(String id, String name, String department, double baseSalary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.baseSalary = baseSalary;
        }
    }

    private final Map<String, Employee> employees = new LinkedHashMap<>();

    // ---- FR1, FR2: add employee ----------------------------------------
    /**
     * @return true if added, false if the ID is already in use
     * @throws IllegalArgumentException if id/name is empty or salary is negative/NaN
     */
    public boolean addEmployee(String id, String name, String department, double baseSalary) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }
        if (!(baseSalary >= 0)) {                       // also rejects NaN
            throw new IllegalArgumentException("Base salary must be zero or more.");
        }
        if (employees.containsKey(id)) {
            return false;
        }
        if (department == null || department.trim().isEmpty()) {
            department = "Unassigned";
        }
        employees.put(id, new Employee(id, name.trim(), department.trim(), baseSalary));
        return true;
    }

    // ---- FR3: remove employee ------------------------------------------
    /** @return true if removed, false if the ID does not exist */
    public boolean removeEmployee(String id) {
        if (employees.remove(id) != null) {
            return true;
        }
        return false;
    }

    // ---- FR7: count / lookup -------------------------------------------
    public int getEmployeeCount() {
        return employees.size();
    }

    /** @return the employee's department, or null if the ID is unknown */
    public String getDepartment(String id) {
        Employee e = employees.get(id);
        return e == null ? null : e.department;
    }

    // ---- FR4, FR5: payment processing ----------------------------------
    /**
     * Net pay = (hourly*hours + hourly*1.5*overtime) [+10% if Management], less tax
     * (25% when gross >= 5000, otherwise 15%). Hourly rate = baseSalary / 160.
     *
     * @throws IllegalArgumentException if hours or overtimeHours is negative/NaN
     * @throws NoSuchElementException   if the employee ID is not registered
     */
    public double processPayment(String id, double hours, double overtimeHours) {
        if (!(hours >= 0)) {
            throw new IllegalArgumentException("Hours worked must be zero or more.");
        }
        if (!(overtimeHours >= 0)) {
            throw new IllegalArgumentException("Overtime hours must be zero or more.");
        }
        Employee e = employees.get(id);
        if (e == null) {
            throw new NoSuchElementException("Employee not found: " + id);
        }

        double hourly = e.baseSalary / STANDARD_HOURS;
        double gross = round2(hourly * hours + hourly * OVERTIME_MULTIPLIER * overtimeHours);

        if (MANAGEMENT_DEPT.equalsIgnoreCase(e.department)) {
            gross = round2(gross * (1 + MANAGEMENT_BONUS));
        }

        double taxRate;
        if (gross >= HIGH_INCOME_LIMIT) {
            taxRate = HIGH_TAX_RATE;
        } else {
            taxRate = LOW_TAX_RATE;
        }

        e.netPay = round2(gross * (1 - taxRate));
        e.paid = true;
        return e.netPay;
    }

    // ---- FR6: payroll report -------------------------------------------
    public String generatePayrollReport() {
        if (employees.isEmpty()) {
            return "No employees to report.";
        }
        StringBuilder sb = new StringBuilder("=== PAYROLL REPORT ===\n");
        double total = 0;
        int paidCount = 0;
        Employee top = null;

        for (Employee e : employees.values()) {
            if (e.paid) {
                sb.append(String.format(Locale.US, "%s | %s | %s | Net pay: %.2f%n",
                        e.id, e.name, e.department, e.netPay));
                total += e.netPay;
                paidCount++;
                if (top == null || e.netPay > top.netPay) {
                    top = e;
                }
            } else {
                sb.append(String.format(Locale.US, "%s | %s | %s | Net pay: not processed%n",
                        e.id, e.name, e.department));
            }
        }

        if (paidCount == 0) {
            sb.append("No payments have been processed.\n");
        } else {
            sb.append(String.format(Locale.US, "Total payroll: %.2f%n", total));
            sb.append(String.format(Locale.US, "Average net pay: %.2f%n", total / paidCount));
            sb.append(String.format(Locale.US, "Top earner: %s (%s) - %.2f%n",
                    top.name, top.id, top.netPay));
        }
        return sb.toString();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ---- Console menu --------------------------------------------------
    public static void main(String[] args) {
        EmployeeManagementSystem ems = new EmployeeManagementSystem();
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("\n1) Add employee  2) Remove employee  3) Process payment");
            System.out.println("4) Payroll report  5) Employee count  0) Exit");
            System.out.print("Choice: ");
            if (!in.hasNextLine()) {
                break;
            }
            String choice = in.nextLine().trim();
            try {
                switch (choice) {
                    case "1": {
                        System.out.print("ID: ");         String id = in.nextLine();
                        System.out.print("Name: ");       String name = in.nextLine();
                        System.out.print("Department: "); String dept = in.nextLine();
                        System.out.print("Base salary: "); double sal = Double.parseDouble(in.nextLine().trim());
                        System.out.println(ems.addEmployee(id, name, dept, sal)
                                ? "Employee added." : "ID already in use.");
                        break;
                    }
                    case "2": {
                        System.out.print("ID: ");
                        System.out.println(ems.removeEmployee(in.nextLine())
                                ? "Employee removed." : "No such employee.");
                        break;
                    }
                    case "3": {
                        System.out.print("ID: ");             String id = in.nextLine();
                        System.out.print("Hours worked: ");   double h = Double.parseDouble(in.nextLine().trim());
                        System.out.print("Overtime hours: "); double ot = Double.parseDouble(in.nextLine().trim());
                        System.out.printf(Locale.US, "Net pay: %.2f%n", ems.processPayment(id, h, ot));
                        break;
                    }
                    case "4":
                        System.out.print(ems.generatePayrollReport());
                        break;
                    case "5":
                        System.out.println("Employees: " + ems.getEmployeeCount());
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (IllegalArgumentException | NoSuchElementException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
