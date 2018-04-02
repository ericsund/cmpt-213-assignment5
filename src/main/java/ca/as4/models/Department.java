package ca.as4.models;

/*
A class to create Department objects
*/
public class Department {
    private long deptId;
    private String name;

    public Department() { }

    public Department(long deptId, String name) {
        this.deptId = deptId;
        this.name = name;
    }

    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDeptId() {
        return deptId;
    }

    public String getName() {
        return name;
    }
}
