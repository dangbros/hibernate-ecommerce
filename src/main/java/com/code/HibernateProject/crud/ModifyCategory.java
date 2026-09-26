package com.code.HibernateProject.crud;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Category;

public class ModifyCategory {

	private final SessionFactory factory;

	public ModifyCategory(SessionFactory factory) {
		this.factory = factory;

		Category updatedCategory = new Category();
		updatedCategory.setId(1);
		updatedCategory.setName("Mobile Electronics");
		updatedCategory.setDescription("Mobile and electronic products");

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();
		dbSession.merge(updatedCategory);
		dbSession.getTransaction().commit();
		dbSession.close();

		System.out.println("Category updated successfully");
	}
}
