package com.chinnammal.dto;

public class WardenCreateRequest {
    private String name;
    private String employeeId;
    private String department;
    private String phone;
    private String email;
    private Integer assignedBlock;
    private String address;

    public WardenCreateRequest() {}

    public WardenCreateRequest(String name, String employeeId, String department, String phone, String email, Integer assignedBlock, String address) {
        this.name = name;
        this.employeeId = employeeId;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.assignedBlock = assignedBlock;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAssignedBlock() {
        return assignedBlock;
    }

    public void setAssignedBlock(Integer assignedBlock) {
        this.assignedBlock = assignedBlock;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
