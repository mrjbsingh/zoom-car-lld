package com.zoomcar.service;

import com.zoomcar.dto.BookingRequest;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Service interface for pricing calculations using Strategy pattern.
 * Allows different pricing strategies for different vehicle types and booking types.
 */
public interface PricingService {
    
    /**
     * Calculate booking price based on request details
     * 
     * @param request Booking request with vehicle and time details
     * @return PricingResult with breakdown of costs
     */
    PricingResult calculateBookingPrice(BookingRequest request);
    
    /**
     * Calculate cancellation charges
     * 
     * @param bookingId Booking identifier
     * @param cancellationTime Time of cancellation
     * @return Cancellation charges
     */
    BigDecimal calculateCancellationCharges(String bookingId, java.time.LocalDateTime cancellationTime);
    
    /**
     * Apply promo code discount
     * 
     * @param baseAmount Base booking amount
     * @param promoCode Promo code to apply
     * @return Discount amount
     */
    BigDecimal applyPromoDiscount(BigDecimal baseAmount, String promoCode);
    
    /**
     * Result object for pricing calculations
     */
    @Data
    @Builder
    public static class PricingResult {
        private BigDecimal baseAmount;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private String pricingBreakdown;
    }
}
