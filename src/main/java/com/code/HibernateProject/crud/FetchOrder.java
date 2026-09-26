package com.code.HibernateProject.crud;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.OrderDetails;
import com.code.HibernateProject.entity.Orders;

public class FetchOrder {

	private final SessionFactory factory;

	public FetchOrder(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		List<Orders> latestOrders = dbSession
				.createQuery("select o from Orders o order by o.id desc", Orders.class)
				.setMaxResults(1)
				.getResultList();

		if (latestOrders.isEmpty()) {
			System.out.println("No orders found yet");
		} else {
			Orders fetchedOrder = latestOrders.get(0);

			System.out.println("Order ID: " + fetchedOrder.getId());
			System.out.println("Order Date: " + fetchedOrder.getOrderDate());
			System.out.println("Total Amount: " + fetchedOrder.getTotalAmount());

			System.out.println("User: " + fetchedOrder.getUser().getUsername());

			System.out.println("Order Details:");
			for (OrderDetails line : fetchedOrder.getOrderDetails()) {
				System.out.println("Product: " + line.getProduct().getName());
				System.out.println("Quantity: " + line.getQuantity());
				System.out.println("Unit Price: " + line.getUnitPrice());
			}
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
