package com.code.HibernateProject;

import org.hibernate.SessionFactory;

import com.code.HibernateProject.crud.AddCategory;
import com.code.HibernateProject.crud.AddOrder;
import com.code.HibernateProject.crud.AddProduct;
import com.code.HibernateProject.crud.AddUsers;
import com.code.HibernateProject.crud.FetchCategory;
import com.code.HibernateProject.crud.FetchOrder;
import com.code.HibernateProject.crud.FetchProductsByPage;
import com.code.HibernateProject.crud.FindProductsByCriteria;
import com.code.HibernateProject.crud.SoftDeleteProduct;

public class StoreApp {

	public static void main(String[] args) {
		String explicitConfig = (args != null && args.length > 0 && args[0] != null && !args[0].isBlank())
				? args[0]
				: System.getProperty("hibernate.config");

		SessionFactory factory;
		if (explicitConfig != null) {
			factory = HibernateUtil.getFactory(explicitConfig);
		} else {
			try {
				factory = HibernateUtil.getFactory("hibernate.cfg.xml");
			} catch (Exception ex) {
				System.out.println("Could not connect to MySQL database (" + ex.getMessage() + ").");
				System.out.println("Falling back to in-memory H2 database (hibernate-h2.cfg.xml)...");
				HibernateUtil.closeFactory();
				factory = HibernateUtil.getFactory("hibernate-h2.cfg.xml");
			}
		}
		try {
			new AddCategory(factory);
			new AddProduct(factory);
			new AddUsers(factory);
			new AddOrder(factory);
			new FetchCategory(factory);
			new FetchOrder(factory);
			new FindProductsByCriteria(factory);
			new FetchProductsByPage(factory);
			new SoftDeleteProduct(factory);
		} finally {
			HibernateUtil.closeFactory();
		}
	}
}
