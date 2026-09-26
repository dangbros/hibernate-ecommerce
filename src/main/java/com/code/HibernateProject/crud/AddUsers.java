package com.code.HibernateProject.crud;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Users;
import com.code.HibernateProject.entity.Users.Role;
import com.code.HibernateProject.util.PasswordHasher;

public class AddUsers {

	private final SessionFactory factory;

	public AddUsers(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		String rawPassword = "Rahul@123";
		Users account = new Users("Rahul", PasswordHasher.computeSha256Hex(rawPassword),
				"rahul@gmail.com", Role.CUSTOMER);

		Users existing = dbSession
				.createQuery("select u from Users u where u.username = :uname", Users.class)
				.setParameter("uname", account.getUsername())
				.uniqueResult();

		if (existing == null) {
			dbSession.persist(account);
			System.out.println("User created successfully");
		} else {
			System.out.println("User \"" + account.getUsername() + "\" already exists, skipping");
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
