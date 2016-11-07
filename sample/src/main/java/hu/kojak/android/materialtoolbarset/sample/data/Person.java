package hu.kojak.android.materialtoolbarset.sample.data;

public class Person {

    public final String firstName;
    public final String lastName;
    public final String company;
    public final String address;

    public Person(String[] fields) {
        int i = 0;
        this.firstName = fields[i++];
        this.lastName = fields[i++];
        this.company = fields[i++];
        this.address = fields[i];
    }
}
