package m.google.eatshipper.model;

import java.util.List;

public class Requests {

    //add new field state payment
    private String paymentState;
    private String phone;
    private String name;
    private String address;
    private String total;
    private String status;
    private String latLong;
    private String comment;
    private List<Order> foods;//list of food order
    private String PaymentMethod;

    public Requests() {
    }

    public Requests(String paymentState, String phone, String name, String address, String total, String status, String latLong, String comment, List<Order> foods, String paymentMethod) {
        this.paymentState = paymentState;
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.latLong = latLong;
        this.comment = comment;
        this.foods = foods;
        PaymentMethod = paymentMethod;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLatLong() {
        return latLong;
    }

    public void setLatLong(String latLong) {
        this.latLong = latLong;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        PaymentMethod = paymentMethod;
    }
}
