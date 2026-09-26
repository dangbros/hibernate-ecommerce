package com.code.HibernateProject.crud;

import java.math.BigDecimal;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Category;
import com.code.HibernateProject.entity.Product;

public class AddProduct {

	private final SessionFactory factory;

	public AddProduct(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		Category cat = dbSession
				.createQuery("select c from Category c where c.name = :catName", Category.class)
				.setParameter("catName", "Electronics")
				.uniqueResult();

		persistIfAbsent(dbSession, new Product("Samsung Galaxy S25", new BigDecimal("80000.00"), 10, cat));
		persistIfAbsent(dbSession, new Product("Dell XPS 15", new BigDecimal("90000.00"), 6, cat));

		dbSession.getTransaction().commit();
		dbSession.close();
	}

	private void persistIfAbsent(Session dbSession, Product candidate) {
		Product existing = dbSession
				.createQuery("select p from Product p where p.name = :prodName", Product.class)
				.setParameter("prodName", candidate.getName())
				.uniqueResult();

		if (existing == null) {
			dbSession.persist(candidate);
			System.out.println("Product created successfully: " + candidate.getName());
		} else {
			System.out.println("Product \"" + candidate.getName() + "\" already exists, skipping");
		}
	}
}
