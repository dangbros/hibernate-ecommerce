package com.code.HibernateProject;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.code.HibernateProject.entity.Category;
import com.code.HibernateProject.entity.OrderDetails;
import com.code.HibernateProject.entity.Orders;
import com.code.HibernateProject.entity.Product;
import com.code.HibernateProject.entity.Users;

public final class HibernateUtil {

	private static volatile SessionFactory sharedFactory;

	private HibernateUtil() {
	}

	public static synchronized SessionFactory getFactory() {
		return getFactory("hibernate.cfg.xml");
	}

	public static synchronized SessionFactory getFactory(String configFileName) {
		if (sharedFactory == null) {
			Configuration config = new Configuration()
					.configure(configFileName)
					.addAnnotatedClass(Category.class)
					.addAnnotatedClass(Product.class)
					.addAnnotatedClass(Users.class)
					.addAnnotatedClass(Orders.class)
					.addAnnotatedClass(OrderDetails.class);
			sharedFactory = config.buildSessionFactory();
		}
		return sharedFactory;
	}

	public static synchronized void closeFactory() {
		if (sharedFactory != null) {
			sharedFactory.close();
			sharedFactory = null;
		}
	}
}
