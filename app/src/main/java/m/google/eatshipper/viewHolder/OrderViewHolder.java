package m.google.eatshipper.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import info.hoang8f.widget.FButton;
import m.google.eatshipper.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView OrderName, OrderPhone, OrderStatus, OrderAddress, txtDate;
    public FButton btnShipper;


    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        OrderName = itemView.findViewById(R.id.Id_Order_name);
        OrderStatus = itemView.findViewById(R.id.Id_Order_status);
        OrderPhone = itemView.findViewById(R.id.Id_Order_phone);
        OrderAddress = itemView.findViewById(R.id.Id_Order_address);
        txtDate = itemView.findViewById(R.id.Id_Date_Order);
        btnShipper = itemView.findViewById(R.id.IdBtnShipper);

    }
}