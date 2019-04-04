package m.google.eatshipper.model;

public class ShippingInformation {

    private String orderId, orderPhone;
    private Double lat,lng;

    public ShippingInformation() {
    }

    public ShippingInformation(String orderId, String orderPhone, Double lat, Double lng) {
        this.orderId = orderId;
        this.orderPhone = orderPhone;
        this.lat = lat;
        this.lng = lng;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderPhone() {
        return orderPhone;
    }

    public void setOrderPhone(String orderPhone) {
        this.orderPhone = orderPhone;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
