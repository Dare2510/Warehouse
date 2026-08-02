package com.boljevac.warehouse.inventory.dto;

public record InventoryResponse(Long inventoryId, String product, int quantity) {
}
