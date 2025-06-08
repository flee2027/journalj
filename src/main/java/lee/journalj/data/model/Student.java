package lee.journalj.data.model;

public class Student {
    private Long id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String className;

    public Student() {
    }

    public Student(String firstName, String lastName, String className) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.className = className;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder(lastName);
        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(" ").append(firstName);
        }
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        return fullName.toString();
    }

    @Override
    public String toString() {
        return getFullName();
    }
} 