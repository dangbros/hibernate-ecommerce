package com.code.HibernateProject.crud;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Product;

public class FetchProductsByPage {

	private static final int PAGE_SIZE = 2;

	private final SessionFactory factory;

	public FetchProductsByPage(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		long totalCount = dbSession
				.createQuery("select count(p) from Product p where p.deleted = false", Long.class)
				.getSingleResult();
		int pageCount = (int) Math.ceil(totalCount / (double) PAGE_SIZE);

		System.out.println("Product listing: " + totalCount + " active product(s), " + PAGE_SIZE + " per page");

		for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
			List<Product> page = dbSession
					.createQuery("select p from Product p where p.deleted = false order by p.name", Product.class)
					.setFirstResult((pageNum - 1) * PAGE_SIZE)
					.setMaxResults(PAGE_SIZE)
					.getResultList();

			System.out.println("Page " + pageNum + " of " + pageCount + ":");
			for (Product product : page) {
				System.out.println("  " + product.getName() + " -> " + product.getPrice());
			}
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
