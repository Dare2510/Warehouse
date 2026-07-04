package com.boljevac.warehouse.user.entity;

import com.boljevac.warehouse.inventory.entity.InventoryEntity;
import com.boljevac.warehouse.location.entity.LocationEntity;
import com.boljevac.warehouse.order.entity.OrderEntity;
import com.boljevac.warehouse.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "surname", nullable = false)
	private String surname;

	@Column(name = "password", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role;

	@OneToMany(mappedBy = "createdByUser", fetch = FetchType.LAZY)
	private List<OrderEntity> ordersBy;

	@OneToMany(mappedBy = "lastChangedByUser", fetch = FetchType.LAZY)
	private List<OrderEntity> ordersChangedByUser;

	@OneToMany(mappedBy = "productCreatedByUser", fetch = FetchType.LAZY)
	private List<ProductEntity> productCreatedByUser;

	@OneToMany(mappedBy = "createdByUser", fetch = FetchType.LAZY)
	private List<InventoryEntity> inventoryCreatedByUser;

	@OneToMany(mappedBy = "locationCreatedByUser", fetch = FetchType.LAZY)
	private List<LocationEntity> locationCreatedByUser;

}
