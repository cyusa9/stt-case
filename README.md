# Employee Management & Payroll System — Software Testing & QA Suite

A Java-based Employee Management and Payroll System developed with a comprehensive Software Testing and Quality Assurance (QA) suite. The project demonstrates both **Black-Box Testing** (Equivalence Partitioning and Boundary Value Analysis) and **White-Box Testing** (Cyclomatic Complexity and 100% Branch/Decision Coverage) using a lightweight, zero-dependency test harness.

---

## 📌 Table of Contents
- [Overview](#-overview)
- [System Architecture & Business Rules](#-system-architecture--business-rules)
- [Functional Requirements (FR)](#-functional-requirements-fr)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Compilation](#compilation)
  - [Running the Interactive CLI](#running-the-interactive-cli)
  - [Executing Automated Tests](#executing-automated-tests)
- [Testing Strategy & Test Cases](#-testing-strategy--test-cases)
  - [Black-Box Testing (BB-01 to BB-12)](#1-black-box-testing-bb-01-to-bb-12)
  - [White-Box Testing (WB-01 to WB-15)](#2-white-box-testing-wb-01-to-wb-15)
  - [Cyclomatic Complexity & Branch Coverage](#3-cyclomatic-complexity--branch-coverage)
  - [Fault Injection (Mutation) Verification](#4-fault-injection-mutation-verification)
- [Known Limitations & Design Insights](#-known-limitations--design-insights)
- [Author & Acknowledgments](#-author--acknowledgments)

---

## 🚀 Overview

The system addresses organizational employee administration, compensation processing, and payroll reporting. It is engineered to satisfy strict business logic regarding overtime premiums, departmental bonuses, and progressive tax cliffs. Alongside the production class, a custom test harness provides automated verification with zero external framework dependencies (pure Java SE).

---

## ⚙️ System Architecture & Business Rules

The system is implemented in `EmployeeManagementSystem.java` with the following business rules:

1. **Standard Working Hours**: Base salary is tied to a standard month of **160.0 hours**.
   $$\text{Hourly Rate} = \frac{\text{baseSalary}}{160.0}$$
2. **Overtime Pay**: Overtime hours are compensated at **1.5×** the derived hourly rate.
   $$\text{Gross Pay} = \text{round}_2(\text{hourly} \times \text{hours} + \text{hourly} \times 1.5 \times \text{overtimeHours})$$
3. **Department Bonus**: Employees in the **Management** department (case-insensitive) receive an additional **10% bonus** on their gross earnings.
   $$\text{Gross}_{\text{mgmt}} = \text{round}_2(\text{Gross} \times 1.10)$$
4. **Two-Tier Tax Brackets**:
   - **Gross $\ge$ 5,000.00**: High tax rate of **25%**
   - **Gross < 5,000.00**: Low tax rate of **15%**
5. **Net Pay Calculation**:
   $$\text{Net Pay} = \text{round}_2(\text{Gross} \times (1 - \text{Tax Rate}))$$
6. **Data Storage**: Managed via an in-memory `LinkedHashMap<String, Employee>` guaranteeing $O(1)$ lookups and predictable iteration order.

---

## 📋 Functional Requirements (FR)

| ID | Description |
| :--- | :--- |
| **FR1** | The system shall allow adding a new employee record with an ID, name, department, and base salary. |
| **FR2** | The system shall reject registration if the ID or name is empty/missing, base salary is negative or NaN, or ID is already in use. |
| **FR3** | The system shall allow removing an employee record by ID and report whether the record existed. |
| **FR4** | The system shall compute net pay using derived hourly rate, overtime (1.5×), management bonus (10%), and tax bracket rules (15% vs 25%). |
| **FR5** | The system shall reject payment processing when the employee ID is not found or when worked/overtime hours are negative or NaN. |
| **FR6** | The system shall generate a payroll report showing individual net pay, total payroll, average net pay, and top earner, handling empty or unpaid states. |
| **FR7** | The system shall report the total count of registered employees. |

---

## 📂 Project Structure

```
stt/
├── EmployeeManagementSystem.java       # Production system under test (SUT)
├── EmployeeManagementSystemTest.java   # Zero-dependency test harness (27 test cases)
├── Test_Case_Report (2).docx           # Formal QA test report
├── README.md                           # Comprehensive documentation
└── .gitignore                          # Git ignore configuration
```

---

## 💻 Getting Started

### Prerequisites
* **Java Development Kit (JDK)**: Version 8 or higher (tested with OpenJDK 17 and OpenJDK 21).

### Compilation
Compile both the production and test sources from the terminal:
```bash
javac EmployeeManagementSystem.java EmployeeManagementSystemTest.java
```

### Running the Interactive CLI
Launch the command-line interface to interactively add employees and run payroll reports:
```bash
java EmployeeManagementSystem
```

**CLI Menu Options:**
```text
1) Add employee  2) Remove employee  3) Process payment
4) Payroll report  5) Employee count  0) Exit
```

### Executing Automated Tests
Run the standalone test suite:
```bash
java EmployeeManagementSystemTest
```

To export machine-readable results (Tab-Separated Values):
```bash
java EmployeeManagementSystemTest --tsv
```

---

## 🧪 Testing Strategy & Test Cases

The test suite consists of **27 automated tests** (100% pass rate) split into Black-Box and White-Box suites.

### 1. Black-Box Testing (`BB-01` to `BB-12`)
Derived directly from requirements using **Equivalence Partitioning (EP)** and **Boundary Value Analysis (BVA)** without referencing internal control flow.

| Test ID | Requirement | Description / Procedure | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **BB-01** | FR1 | Add employee with valid ID, name, dept, and salary | Employee added; count = 1 | Employee added; count = 1. | **PASS** |
| **BB-02** | FR2 | Add duplicate employee ID | Second add rejected; count = 1 | Rejected; count = 1. | **PASS** |
| **BB-03** | FR2 | Add employee with empty ID string `""` | `IllegalArgumentException` thrown | `IllegalArgumentException` thrown. | **PASS** |
| **BB-04** | FR2 | Add employee with negative base salary (`-100`) | `IllegalArgumentException` thrown | `IllegalArgumentException` thrown. | **PASS** |
| **BB-05** | FR3 | Remove existing employee | Removed; count = 0 | Removed; count = 0. | **PASS** |
| **BB-06** | FR3 | Remove non-existent employee ID (`"E999"`) | Returns `false` | Returned false. | **PASS** |
| **BB-07** | FR4 | Payment below 5,000 threshold (Salary 3000, 160h, 0 OT) | Net pay = 2550.00 (15% tax) | Net pay = 2550.00. | **PASS** |
| **BB-08** | FR4 | Management employee with high tax bracket (Salary 6000) | Net pay = 4950.00 (10% bonus, 25% tax) | Net pay = 4950.00. | **PASS** |
| **BB-09** | FR5 | Process payment with negative hours (`-10`) | `IllegalArgumentException` thrown | `IllegalArgumentException` thrown. | **PASS** |
| **BB-10** | FR5 | Process payment for unregistered ID (`"E999"`) | `NoSuchElementException` thrown | `NoSuchElementException` thrown. | **PASS** |
| **BB-11** | FR6 | Generate payroll report with paid employees | Summary with total, average, top earner | Report contained all expected fields. | **PASS** |
| **BB-12** | FR6 | Generate payroll report on empty system | Exact message: "No employees to report." | Message returned exactly. | **PASS** |

---

### 2. White-Box Testing (`WB-01` to `WB-15`)
Engineered to drive every branch of internal decision logic both `true` and `false`.

| Test ID | Condition / Branch | Input / Procedure | Expected Result | Status |
| :--- | :--- | :--- | :--- | :---: |
| **WB-01** | `id == null` | `addEmployee(null, "Alice", "Eng", 3000)` | `IllegalArgumentException` thrown | **PASS** |
| **WB-02** | `name` is blank spaces `"   "` | `addEmployee("E001", "   ", "Eng", 3000)` | `IllegalArgumentException` thrown | **PASS** |
| **WB-03** | `baseSalary` is `Double.NaN` | `addEmployee("E001", "Alice", "Eng", NaN)` | `IllegalArgumentException` thrown | **PASS** |
| **WB-04** | Lower boundary `salary = 0`, `dept = null` | `addEmployee("E001", "Alice", null, 0)` | Added; dept defaults to `"Unassigned"` | **PASS** |
| **WB-05** | Duplicate ID with different details | Add duplicate ID `"E001"` with new name | Returns `false`; original record preserved | **PASS** |
| **WB-06** | Double removal | Call `removeEmployee("E001")` twice | First call `true`, second call `false` | **PASS** |
| **WB-07** | Hours is `Double.NaN` | `processPayment("E001", Double.NaN, 0)` | `IllegalArgumentException` thrown | **PASS** |
| **WB-08** | Overtime hours is negative (`-5`) | `processPayment("E001", 160, -5)` | `IllegalArgumentException` thrown | **PASS** |
| **WB-09** | `id` parameter is `null` | `processPayment(null, 160, 0)` | `NoSuchElementException` thrown | **PASS** |
| **WB-10** | Overtime compensation term | Salary 3200, 160h, 10h OT | Gross 3500; 15% tax; Net = 2975.00 | **PASS** |
| **WB-11** | Case-insensitive dept match | Salary 4000, dept `"management"` | Gross 4400; 15% tax; Net = 3740.00 | **PASS** |
| **WB-12** | Boundary test: Gross exactly `5000.00` | Salary 5000, 160h, Engineering | 25% tax; Net = 3750.00 | **PASS** |
| **WB-13** | Boundary test: Gross just below `4999.99` | Salary 4999.99, 160h, Engineering | 15% tax; Net = 4249.99 | **PASS** |
| **WB-14** | Report with unpaid employees | Add employee without calling payment | Listed as "not processed", no totals | **PASS** |
| **WB-15** | Report with mixed paid and unpaid records | Grace (paid), Bob (unpaid), Alice (paid) | Total: 7500.00; Avg: 3750.00; Top: Grace | **PASS** |

---

### 3. Cyclomatic Complexity & Branch Coverage

Cyclomatic complexity $V(G)$ is calculated as $V(G) = P + 1$, where $P$ is the number of predicate nodes.

* **`addEmployee`**: $V(G) = 9$ (8 predicate conditions + 1)
* **`removeEmployee`**: $V(G) = 2$ (1 predicate condition + 1)
* **`processPayment`**: $V(G) = 6$ (5 predicate conditions + 1)
* **`generatePayrollReport`**: $V(G) = 7$ (6 predicate conditions + 1)

**Summary**: Across the 4 core business methods, there are **16 decision points** producing **32 distinct branches**. All 32 branches are exercised (**100% Branch Coverage**).

---

### 4. Fault Injection (Mutation) Verification

To verify that the test suite actively catches regressions, 4 faults were injected into temporary copies of `EmployeeManagementSystem.java`:

| Injected Mutation | Location | Detecting Test Case | Suite Result | Detection |
| :--- | :--- | :---: | :---: | :---: |
| Tax threshold changed from `>=` to `>` | `processPayment` | `WB-12` (gross exactly 5000) | 26/27 Passed | **CAUGHT** |
| Dept match changed from `equalsIgnoreCase` to `equals` | `processPayment` | `WB-11` (dept `"management"`) | 26/27 Passed | **CAUGHT** |
| Overtime multiplier changed from `1.5` to `1.0` | `processPayment` | `WB-10` | 26/27 Passed | **CAUGHT** |
| Top-earner comparison changed from `>` to `<` | `generatePayrollReport` | `WB-15` | 26/27 Passed | **CAUGHT** |

---

## 🔍 Known Limitations & Design Insights

1. **Tax Cliff Effect**:
   The tax structure operates as a flat cliff rather than marginal tax brackets:
   - Gross of **$4,999.99** at 15% tax yields **$4,249.99 Net**.
   - Gross of **$5,000.00** at 25% tax yields **$3,750.00 Net**.
   *(An employee earning $0.01 more gross loses $499.99 in take-home pay; tested by `WB-12` and `WB-13`).*
2. **Financial Precision**:
   Calculations use primitive `double` with two-decimal rounding (`round2()`). While sufficient for unit-testing coursework, enterprise payroll systems should utilize `java.math.BigDecimal` to avoid binary floating-point representation drift.
3. **Prorated Hourly vs Fixed Salary**:
   Hourly rate is derived as `baseSalary / 160.0`. Working fewer than 160 hours prorates total pay downward.
4. **State Persistence**:
   The database is in-memory and non-persistent; records are reset when the JVM process terminates.

---

## 👤 Author & Acknowledgments

* **Developer & QA Analyst**: Kwizera Pacifique
* **Course**: Software Testing & Quality Assurance / Techniques
* **Date**: October 2026
