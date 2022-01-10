package model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Joseph
 */
public class Customer {
    private int id;
    private SimpleStringProperty name, phone, address1, address2, postal, city, country;
    
    public Customer(int id, String name, String phone, String address1, String address2,
                    String postal, String city, String country) {
        this(name, phone, address1, address2, postal, city, country);
        this.id = id;
    }
    public Customer(String name, String phone, String address1, String address2,
                    String postal, String city, String country) {
        this.name = new SimpleStringProperty(name);
        this.phone = new SimpleStringProperty(phone);
        this.address1 = new SimpleStringProperty(address1);
        this.address2 = new SimpleStringProperty(address2);
        this.postal = new SimpleStringProperty(postal);
        this.city = new SimpleStringProperty(city);
        this.country = new SimpleStringProperty(country);
    }
    
    @Override
    public String toString() {
        return name.get();
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }    
    
    public final String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public SimpleStringProperty getNameProperty() {
        return name;
    }    
    
    public final String getPhone() {
        return phone.get();
    }
    public void setPhone(String phone) {
        this.phone.set(phone);
    }
    public SimpleStringProperty getPhoneProperty() {
        return phone;
    }    
    
    public final String getAddress1() {
        return address1.get();
    }
    public void setAddress1(String address1) {
        this.address1.set(address1);
    }
    public SimpleStringProperty getAddress1Property() {
        return address1;
    }    
    
    public final String getAddress2() {
        return address2.get();
    }
    public void setAddress2(String address2) {
        this.address2.set(address2);
    }
    public SimpleStringProperty getAddress2Property() {
        return address2;
    }    
    
    public final String getPostal() {
        return postal.get();
    }
    public void setPostal(String postal) {
        this.postal.set(postal);
    }
    public SimpleStringProperty getPostalProperty() {
        return postal;
    }    
    
    public final String getCity() {
        return city.get();
    }
    public void setCity(String city) {
        this.city.set(city);
    }
    public SimpleStringProperty getCityProperty() {
        return city;
    }
    
    public final String getCountry() {
        return country.get();
    }
    public void setCountry(String country) {
        this.country.set(country);
    }
    public SimpleStringProperty getCountryProperty() {
        return country;
    }
}
