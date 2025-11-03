package aydin.firebasedemo;

public class Person {
    private String name;
    private int age;
    private String phone;   // 👈 new

    public Person() {
        // no-arg for firestore (optional)
    }

    public Person(String name, int age) {
        this(name, age, null);
    }

    public Person(String name, int age, String phone) {
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
