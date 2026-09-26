package com.code.HibernateProject.crud;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Product;

public class SoftDeleteProduct {

	private final SessionFactory factory;

	public SoftDeleteProduct(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		List<Product> targets = dbSession
				.createQuery("select p from Product p where p.name = :prodName and p.deleted = false", Product.class)
				.setParameter("prodName", "Dell XPS 15")
				.getResultList();

		if (targets.isEmpty()) {
			System.out.println("\"Dell XPS 15\" is missing or already soft-deleted, nothing to do");
		} else {
			for (Product product : targets) {
				product.setDeleted(true);
				dbSession.merge(product);
				System.out.println("Soft deleted: " + product.getName() + " (row kept, deleted=true)");
			}
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
