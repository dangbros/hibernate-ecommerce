package com.code.HibernateProject.crud;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.code.HibernateProject.entity.Product;

public class FindProductsByCriteria {

	private final SessionFactory factory;

	public FindProductsByCriteria(SessionFactory factory) {
		this.factory = factory;

		Session dbSession = factory.getCurrentSession();
		dbSession.beginTransaction();

		CriteriaBuilder cb = dbSession.getCriteriaBuilder();
		CriteriaQuery<Product> criteriaQuery = cb.createQuery(Product.class);
		Root<Product> product = criteriaQuery.from(Product.class);

		criteriaQuery.where(
				cb.equal(product.get("deleted"), false),
				cb.between(product.get("price"), new BigDecimal("50000.00"), new BigDecimal("100000.00")));
		criteriaQuery.orderBy(cb.asc(product.get("name")));

		List<Product> results = dbSession.createQuery(criteriaQuery).getResultList();

		System.out.println("Criteria query: active products priced between 50000 and 100000");
		if (results.isEmpty()) {
			System.out.println("No matching products");
		} else {
			for (Product found : results) {
				System.out.println("  " + found.getName() + " -> " + found.getPrice());
			}
		}

		dbSession.getTransaction().commit();
		dbSession.close();
	}
}
