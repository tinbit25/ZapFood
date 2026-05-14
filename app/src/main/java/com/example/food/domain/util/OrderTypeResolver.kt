package com.example.food.domain.util

import com.example.food.data.model.Order
import com.example.food.data.model.OrderType

/**
 * OrderTypeResolver
 *
 * Validates and resolves type-specific constraints for each order type.
 * Ensures that the correct info block (deliveryInfo / pickupInfo / dineInInfo)
 * is present before an order is submitted.
 */
object OrderTypeResolver {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    /**
     * Validates that the required type-specific info block is populated
     * for the given order's [OrderType].
     */
    fun validate(order: Order): ValidationResult {
        return when (order.orderType) {
            OrderType.DELIVERY -> {
                val info = order.deliveryInfo
                when {
                    info == null ->
                        ValidationResult.Invalid("Delivery order requires deliveryInfo to be set.")
                    info.address.isBlank() ->
                        ValidationResult.Invalid("Delivery order requires a valid delivery address.")
                    else -> ValidationResult.Valid
                }
            }
            OrderType.TAKEAWAY -> {
                val info = order.pickupInfo
                when {
                    info == null ->
                        ValidationResult.Invalid("Takeaway order requires pickupInfo to be set.")
                    info.pickupBranch.isBlank() ->
                        ValidationResult.Invalid("Takeaway order requires a valid pickup branch.")
                    else -> ValidationResult.Valid
                }
            }
            OrderType.DINE_IN -> {
                val info = order.dineInInfo
                when {
                    info == null ->
                        ValidationResult.Invalid("Dine-in order requires dineInInfo to be set.")
                    info.guestCount < 1 ->
                        ValidationResult.Invalid("Dine-in order requires at least 1 guest.")
                    else -> ValidationResult.Valid
                }
            }
        }
    }

    /**
     * Returns a human-readable label for a given [OrderType].
     */
    fun labelFor(type: OrderType): String = when (type) {
        OrderType.DELIVERY -> "Delivery"
        OrderType.TAKEAWAY -> "Takeaway"
        OrderType.DINE_IN  -> "Dine-In"
    }

    /**
     * Returns true if the order requires a delivery fee to be applied.
     */
    fun requiresDeliveryFee(type: OrderType): Boolean = type == OrderType.DELIVERY

    /**
     * Returns true if a pickup time must be set on the order.
     */
    fun requiresPickupSchedule(type: OrderType): Boolean =
        type == OrderType.TAKEAWAY || type == OrderType.DINE_IN
}
