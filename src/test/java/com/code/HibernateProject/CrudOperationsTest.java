package com.code.HibernateProject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.code.HibernateProject.entity.Category;
import com.code.HibernateProject.entity.OrderDetails;
import com.code.HibernateProject.entity.Orders;
import com.code.HibernateProject.entity.Product;
import com.code.HibernateProject.entity.Users;
import com.code.HibernateProject.util.PasswordHasher;

class CrudOperationsTest {

	private static SessionFactory factory;

	@BeforeAll
	static void buildFactory() {
		factory = HibernateUtil.getFactory("hibernate-test.cfg.xml");
	}

	@AfterAll
	static void shutDownFactory() {
		HibernateUtil.closeFactory();
	}

	@Test
	void insertCategoryPersistsRow() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Audio Gear", "Headphones, speakers and audio devices");
			dbSession.persist(cat);
			dbSession.getTransaction().commit();

			assertTrue(cat.getId() > 0);

			Category reloaded = dbSession.get(Category.class, cat.getId());
			assertEquals("Audio Gear", reloaded.getName());
			assertEquals("Headphones, speakers and audio devices", reloaded.getDescription());
		}
	}

	@Test
	void insertProductWithCategoryLink() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Gaming", "Gaming hardware");
			dbSession.persist(cat);

			Product item = new Product("PlayStation 5", new BigDecimal("45000.00"), 25, cat);
			dbSession.persist(item);
			dbSession.getTransaction().commit();

			assertTrue(item.getId() > 0);
			assertEquals(cat.getId(), item.getCategory().getId());
			assertEquals(0, new BigDecimal("45000.00").compareTo(item.getPrice()));
			assertEquals(25, item.getStockQuantity());
		}
	}

	@Test
	void insertUserStoresHashedPassword() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			String rawPassword = "Secret#99";
			Users account = new Users("newMember", PasswordHasher.computeSha256Hex(rawPassword),
					"newmember@example.com", Users.Role.CUSTOMER);
			dbSession.persist(account);
			dbSession.getTransaction().commit();

			assertTrue(account.getId() > 0);
			assertNotEquals(rawPassword, account.getPassword());
			assertEquals(PasswordHasher.computeSha256Hex(rawPassword), account.getPassword());
		}
	}

	@Test
	void createOrderWithMultipleDetails() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Users buyer = new Users("orderBuyer", PasswordHasher.computeSha256Hex("Pass@1"),
					"buyer@example.com", Users.Role.CUSTOMER);
			dbSession.persist(buyer);

			Category cat = new Category("Laptops", "Notebooks");
			dbSession.persist(cat);

			Product laptop = new Product("ThinkPad X1", new BigDecimal("120000.00"), 8, cat);
			dbSession.persist(laptop);
			Product mouse = new Product("Wireless Mouse", new BigDecimal("1500.00"), 100, cat);
			dbSession.persist(mouse);

			OrderDetails line1 = new OrderDetails(2, laptop.getPrice(), null, laptop);
			OrderDetails line2 = new OrderDetails(3, mouse.getPrice(), null, mouse);
			BigDecimal total = line1.getUnitPrice().multiply(BigDecimal.valueOf(line1.getQuantity()))
					.add(line2.getUnitPrice().multiply(BigDecimal.valueOf(line2.getQuantity())));

			Orders newOrder = new Orders(LocalDateTime.now(), total, buyer);
			line1.setOrder(newOrder);
			line2.setOrder(newOrder);
			newOrder.getOrderDetails().add(line1);
			newOrder.getOrderDetails().add(line2);

			dbSession.persist(newOrder);
			dbSession.getTransaction().commit();

			assertTrue(newOrder.getId() > 0);
			assertEquals(2, newOrder.getOrderDetails().size());
			assertEquals(buyer.getId(), newOrder.getUser().getId());
			assertEquals(0, total.compareTo(newOrder.getTotalAmount()));
		}
	}

	@Test
	void fetchOrderAlongWithUserAndProducts() {
		int orderId;

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Users buyer = new Users("fetchBuyer", PasswordHasher.computeSha256Hex("Pass@2"),
					"fetchbuyer@example.com", Users.Role.ADMIN);
			dbSession.persist(buyer);

			Category cat = new Category("Cameras", "Photo and video cameras");
			dbSession.persist(cat);

			Product camera = new Product("Mirrorless Camera", new BigDecimal("60000.00"), 4, cat);
			dbSession.persist(camera);

			OrderDetails line = new OrderDetails(1, camera.getPrice(), null, camera);
			Orders newOrder = new Orders(LocalDateTime.now(), camera.getPrice(), buyer);
			line.setOrder(newOrder);
			newOrder.getOrderDetails().add(line);

			dbSession.persist(newOrder);
			dbSession.getTransaction().commit();
			orderId = newOrder.getId();
		}

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Orders loaded = dbSession.get(Orders.class, orderId);

			assertEquals("fetchBuyer", loaded.getUser().getUsername());
			assertEquals(Users.Role.ADMIN, loaded.getUser().getRole());
			assertEquals(1, loaded.getOrderDetails().size());
			assertEquals("Mirrorless Camera", loaded.getOrderDetails().get(0).getProduct().getName());

			dbSession.getTransaction().commit();
		}
	}

	@Test
	void updateCategoryViaMerge() {
		int categoryId;

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Phones", "Mobile phones");
			dbSession.persist(cat);
			dbSession.getTransaction().commit();
			categoryId = cat.getId();
		}

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category updated = new Category();
			updated.setId(categoryId);
			updated.setName("Smartphones");
			updated.setDescription("Modern mobile phones");
			dbSession.merge(updated);
			dbSession.getTransaction().commit();

			Category reloaded = dbSession.get(Category.class, categoryId);
			assertEquals("Smartphones", reloaded.getName());
			assertEquals("Modern mobile phones", reloaded.getDescription());
		}
	}

	@Test
	void deleteCategoryCascadesToItsProducts() {
		int categoryId;
		int productId;

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Peripherals", "Keyboards and mice");
			dbSession.persist(cat);

			Product item = new Product("Mechanical Keyboard", new BigDecimal("8000.00"), 15, cat);
			dbSession.persist(item);

			dbSession.getTransaction().commit();
			categoryId = cat.getId();
			productId = item.getId();
		}

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category toRemove = dbSession.get(Category.class, categoryId);
			dbSession.remove(toRemove);
			dbSession.getTransaction().commit();

			assertNull(dbSession.get(Category.class, categoryId));
			assertNull(dbSession.get(Product.class, productId));
		}
	}

	@Test
	void namedQueryFindsProductsByCategory() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Monitors", "Display screens");
			dbSession.persist(cat);
			dbSession.persist(new Product("27in 4K Monitor", new BigDecimal("30000.00"), 4, cat));
			dbSession.persist(new Product("32in Curved Monitor", new BigDecimal("40000.00"), 2, cat));
			dbSession.getTransaction().commit();

			List<Product> monitors = dbSession
					.createNamedQuery("Product.byCategoryName", Product.class)
					.setParameter("catName", "Monitors")
					.getResultList();

			assertEquals(2, monitors.size());
		}
	}

	@Test
	void criteriaQueryFindsActiveProductsByPriceRange() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Criteria Demo", "Products for the criteria query test");
			dbSession.persist(cat);
			dbSession.persist(new Product("Budget Item", new BigDecimal("50.00"), 1, cat));
			dbSession.persist(new Product("Mid Item", new BigDecimal("1500.00"), 1, cat));
			dbSession.persist(new Product("Premium Item", new BigDecimal("50000.00"), 1, cat));

			Product retired = new Product("Retired Item", new BigDecimal("2500.00"), 1, cat);
			retired.setDeleted(true);
			dbSession.persist(retired);

			CriteriaBuilder cb = dbSession.getCriteriaBuilder();
			CriteriaQuery<Product> cq = cb.createQuery(Product.class);
			Root<Product> product = cq.from(Product.class);
			cq.where(
					cb.equal(product.get("category").get("name"), "Criteria Demo"),
					cb.equal(product.get("deleted"), false),
					cb.between(product.get("price"), new BigDecimal("100.00"), new BigDecimal("10000.00")));

			List<Product> found = dbSession.createQuery(cq).getResultList();

			assertEquals(1, found.size());
			assertEquals("Mid Item", found.get(0).getName());

			dbSession.getTransaction().commit();
		}
	}

	@Test
	void softDeleteKeepsRowAndSetsFlag() {
		int productId;

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Soft Delete", "Products for the soft delete test");
			dbSession.persist(cat);
			Product item = new Product("Legacy Watch", new BigDecimal("3000.00"), 3, cat);
			dbSession.persist(item);
			dbSession.getTransaction().commit();
			productId = item.getId();
		}

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Product toRemove = dbSession.get(Product.class, productId);
			toRemove.setDeleted(true);
			dbSession.merge(toRemove);
			dbSession.getTransaction().commit();
		}

		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Product reloaded = dbSession.get(Product.class, productId);
			assertNotNull(reloaded);
			assertTrue(reloaded.isDeleted());

			List<Product> active = dbSession
					.createQuery("select p from Product p where p.name = :prodName and p.deleted = false", Product.class)
					.setParameter("prodName", "Legacy Watch")
					.getResultList();
			assertTrue(active.isEmpty());

			dbSession.getTransaction().commit();
		}
	}

	@Test
	void paginationSplitsProductListing() {
		try (Session dbSession = factory.openSession()) {
			dbSession.beginTransaction();

			Category cat = new Category("Paginated", "Products for the pagination test");
			dbSession.persist(cat);
			for (int i = 1; i <= 5; i++) {
				dbSession.persist(new Product("Paged Item " + i, BigDecimal.valueOf(i * 100L), 1, cat));
			}

			long total = dbSession
					.createQuery("select count(p) from Product p where p.name like 'Paged Item %' and p.deleted = false",
							Long.class)
					.getSingleResult();
			assertEquals(5L, total);

			List<Product> firstPage = dbSession
					.createQuery("select p from Product p where p.name like 'Paged Item %' and p.deleted = false order by p.name",
							Product.class)
					.setFirstResult(0)
					.setMaxResults(2)
					.getResultList();
			assertEquals(2, firstPage.size());

			List<Product> secondPage = dbSession
					.createQuery("select p from Product p where p.name like 'Paged Item %' and p.deleted = false order by p.name",
							Product.class)
					.setFirstResult(2)
					.setMaxResults(2)
					.getResultList();
			assertEquals(2, secondPage.size());
			assertNotEquals(firstPage.get(0).getId(), secondPage.get(0).getId());

			dbSession.getTransaction().commit();
		}
	}

	@Test
	void crudPipelineExecution() {
		new com.code.HibernateProject.crud.AddCategory(factory);
		new com.code.HibernateProject.crud.AddProduct(factory);
		new com.code.HibernateProject.crud.AddUsers(factory);
		new com.code.HibernateProject.crud.AddOrder(factory);
		new com.code.HibernateProject.crud.FetchCategory(factory);
		new com.code.HibernateProject.crud.FetchOrder(factory);
		new com.code.HibernateProject.crud.FindProductsByCriteria(factory);
		new com.code.HibernateProject.crud.FetchProductsByPage(factory);
		new com.code.HibernateProject.crud.SoftDeleteProduct(factory);
		new com.code.HibernateProject.crud.ModifyCategory(factory);

		// Verify idempotency on second run
		new com.code.HibernateProject.crud.AddCategory(factory);
		new com.code.HibernateProject.crud.AddProduct(factory);
		new com.code.HibernateProject.crud.AddUsers(factory);
		new com.code.HibernateProject.crud.SoftDeleteProduct(factory);
	}
}
