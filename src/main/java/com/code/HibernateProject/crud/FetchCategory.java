package com.code.HibernateProject.crud;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Category;

public class FetchCategory {

	private final SessionFactory factory;

	public FetchCategory(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		List<Category> allCategories = dbSession
				.createQuery("select c from Category c order by c.id", Category.class)
				.getResultList();

		if (allCategories.isEmpty()) {
			System.out.println("No categories found");
		} else {
			System.out.println("Categories found:");
			for (Category cat : allCategories) {
				System.out.println(cat);
			}
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
