package m.google.eatshipper.model;

public class Order {

    private String ProductoId;
    private String NombreProducto;
    private String Cantidad;
    private String Precio;
    private String Descuento;

    public Order() {
    }

    public Order(String productoId, String nombreProducto, String cantidad, String precio, String descuento) {
        ProductoId = productoId;
        NombreProducto = nombreProducto;
        Cantidad = cantidad;
        Precio = precio;
        Descuento = descuento;
    }

    public String getProductoId() {
        return ProductoId;
    }

    public void setProductoId(String productoId) {
        ProductoId = productoId;
    }

    public String getNombreProducto() {
        return NombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        NombreProducto = nombreProducto;
    }

    public String getCantidad() {
        return Cantidad;
    }

    public void setCantidad(String cantidad) {
        Cantidad = cantidad;
    }

    public String getPrecio() {
        return Precio;
    }

    public void setPrecio(String precio) {
        Precio = precio;
    }

    public String getDescuento() {
        return Descuento;
    }

    public void setDescuento(String descuento) {
        Descuento = descuento;
    }
}
