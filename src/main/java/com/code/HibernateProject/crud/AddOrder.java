package com.code.HibernateProject.crud;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.OrderDetails;
import com.code.HibernateProject.entity.Orders;
import com.code.HibernateProject.entity.Product;
import com.code.HibernateProject.entity.Users;

public class AddOrder {

	private final SessionFactory factory;

	public AddOrder(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		Users buyer = findUserByUsername(dbSession, "Rahul");
		Product item1 = findProductByName(dbSession, "Samsung Galaxy S25");
		Product item2 = findProductByName(dbSession, "Dell XPS 15");

		OrderDetails line1 = new OrderDetails(1, item1.getPrice(), null, item1);
		OrderDetails line2 = new OrderDetails(1, item2.getPrice(), null, item2);

		BigDecimal total = line1.getUnitPrice().multiply(BigDecimal.valueOf(line1.getQuantity()))
				.add(line2.getUnitPrice().multiply(BigDecimal.valueOf(line2.getQuantity())));

		Orders newOrder = new Orders(LocalDateTime.now(), total, buyer);
		line1.setOrder(newOrder);
		line2.setOrder(newOrder);
		newOrder.getOrderDetails().add(line1);
		newOrder.getOrderDetails().add(line2);

		dbSession.persist(newOrder);

		dbSession.getTransaction().commit();
		dbSession.close();

		System.out.println("Order created successfully, total amount: " + total);
	}

	private Users findUserByUsername(Session dbSession, String userName) {
		return dbSession
				.createQuery("select u from Users u where u.username = :uname", Users.class)
				.setParameter("uname", userName)
				.uniqueResult();
	}

	private Product findProductByName(Session dbSession, String productName) {
		return dbSession
				.createQuery("select p from Product p where p.name = :prodName", Product.class)
				.setParameter("prodName", productName)
				.uniqueResult();
	}
}
