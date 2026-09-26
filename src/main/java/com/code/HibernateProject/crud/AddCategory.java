package com.code.HibernateProject.crud;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Category;

public class AddCategory {

	private final SessionFactory factory;

	public AddCategory(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		String categoryName = "Electronics";
		Category existing = dbSession
				.createQuery("select c from Category c where c.name = :catName", Category.class)
				.setParameter("catName", categoryName)
				.uniqueResult();

		if (existing != null) {
			System.out.println("Category \"" + categoryName + "\" already exists, skipping");
		} else {
			Category cat = new Category(categoryName, "Electronic products");
			dbSession.persist(cat);
			System.out.println("Category created successfully");
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
