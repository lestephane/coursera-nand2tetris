class AssignmentOperationBuilder {
    private String from;
    private String to;

    public AssignmentOperationBuilder fromM() {
        from = "M";
        return this;
    }

    public AssignmentOperationBuilder toA() {
        to = "A";
        return this;
    }

    public AssignmentOperationBuilder fromA() {
        from = "A";
        return this;
    }

    public AssignmentOperationBuilder toD() {
        to = "D";
        return this;
    }

    public String toString() {
        return to + "=" + from;
    }

    public AssignmentOperationBuilder fromD() {
        from = "D";
        return this;
    }

    public AssignmentOperationBuilder toM() {
        to = "M";
        return this;
    }

    public AssignmentOperationBuilder mPlusOne() {
        from = "M+1";
        return this;
    }

    public AssignmentOperationBuilder andToM() {
        to += "M";
        return this;
    }

    public AssignmentOperationBuilder aMinusOne() {
        from = "A-1";
        return this;
    }

    public AssignmentOperationBuilder mMinusOne() {
        from = "M-1";
        return this;
    }

    public AssignmentOperationBuilder aPlusD() {
        from = "D+A";
        return this;
    }

    public AssignmentOperationBuilder mPlusD() {
        from = "D+M";
        return this;
    }

    public AssignmentOperationBuilder mMinusD() {
        from = "M-D";
        return this;
    }

    public AssignmentOperationBuilder negatedM() {
        from = "-M";
        return this;
    }

    public AssignmentOperationBuilder notM() {
        from = "!M";
        return this;
    }

    public AssignmentOperationBuilder aPlusOne() {
        from = "A+1";
        return this;
    }
}
