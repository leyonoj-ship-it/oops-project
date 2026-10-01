package com.smartdine.service;

import com.smartdine.model.PaymentMethod;
import com.smartdine.model.payment.PaymentResult;

public interface PaymentService {
    PaymentResult processOrderPayment(double amount, PaymentMethod method, String referenceDetails, Integer tableNumber);
}
