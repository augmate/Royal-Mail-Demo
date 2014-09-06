package com.augmate.employeescanner;

/**
 * Created by premnirmal on 8/18/14.
 */
public class MockEmployeeBin implements IEmployeeBin {

    private static final MockEmployeeBin instance = new MockEmployeeBin();

    private MockEmployeeBin() {
    }

    public static IEmployeeBin getInstance() {
        return instance;
    }

    @Override
    public Employee getEmployee(String id) {
        return new Employee(id.replace("user_", ""), id);
    }
}
