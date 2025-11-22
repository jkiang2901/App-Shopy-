const express = require('express');
const router = express.Router();
const Order = require('../models/order');
const Cart = require('../models/cart');
const Product = require('../models/product');
const Promotion = require('../models/promotion');
const { authenticate, isCustomer, isAdminOrSeller } = require('../middleware/auth');

// Create order (Customer)
router.post('/', authenticate, isCustomer, async (req, res) => {
  try {
    const { paymentMethod, deliveryAddress, promotionCode } = req.body;
    const customerId = req.user._id;

    // Get cart
    const cart = await Cart.findOne({ customerId }).populate('items.productId');
    if (!cart || cart.items.length === 0) {
      return res.status(400).json({ error: 'Cart is empty' });
    }

    // Calculate totals
    let totalAmount = 0;
    const items = [];
    let sellerId = null;
    
    for (const item of cart.items) {
      const product = await Product.findById(item.productId);
      if (!product || product.quantity < item.quantity) {
        return res.status(400).json({ error: `Insufficient stock for ${product?.name || 'product'}` });
      }
      
      // Get sellerId from first product (assuming all items are from same seller for simplicity)
      // In a real scenario, you might want to create separate orders for different sellers
      if (!sellerId && product.sellerId) {
        sellerId = product.sellerId;
      }
      
      const itemTotal = product.price * item.quantity;
      totalAmount += itemTotal;
      items.push({
        productId: product._id,
        quantity: item.quantity,
        price: product.price
      });
    }

    // If no sellerId found, set to null (for products without seller)
    // Order model now allows sellerId to be null/optional
    // Note: In a real scenario, you might want to create separate orders for different sellers
    // For now, we allow null sellerId if all products are unassigned

    // Apply promotion if provided
    let discountAmount = 0;
    let promotionId = null;
    if (promotionCode) {
      const promotion = await Promotion.findOne({ code: promotionCode, isActive: true });
      if (promotion && new Date() >= promotion.startDate && new Date() <= promotion.endDate) {
        if (totalAmount >= promotion.minPurchaseAmount) {
          if (promotion.usageLimit && promotion.usedCount >= promotion.usageLimit) {
            return res.status(400).json({ error: 'Promotion code has reached usage limit' });
          }
          
          if (promotion.discountType === 'percentage') {
            discountAmount = (totalAmount * promotion.discountValue) / 100;
            if (promotion.maxDiscountAmount) {
              discountAmount = Math.min(discountAmount, promotion.maxDiscountAmount);
            }
          } else {
            discountAmount = promotion.discountValue;
          }
          
          promotionId = promotion._id;
        }
      }
    }

    const finalAmount = totalAmount - discountAmount;

    // Create order
    const order = new Order({
      customerId,
      sellerId,
      items,
      totalAmount,
      discountAmount,
      finalAmount,
      paymentMethod,
      deliveryAddress,
      promotionId
    });

    await order.save();

    // Update product quantities
    for (const item of items) {
      await Product.findByIdAndUpdate(item.productId, {
        $inc: { quantity: -item.quantity }
      });
    }

    // Update promotion used count
    if (promotionId) {
      await Promotion.findByIdAndUpdate(promotionId, {
        $inc: { usedCount: 1 }
      });
    }

    // Clear cart
    await Cart.findOneAndUpdate({ customerId }, { items: [] });

    res.status(201).json(order);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// Get orders (Customer - own orders, Seller - own sales, Admin - all)
router.get('/', authenticate, async (req, res) => {
  try {
    let orders;
    
    if (req.user.role === 'customer') {
      orders = await Order.find({ customerId: req.user._id })
        .populate('items.productId')
        .populate('sellerId', 'name email')
        .sort({ createdAt: -1 });
    } else if (req.user.role === 'seller') {
      orders = await Order.find({ sellerId: req.user._id })
        .populate('items.productId')
        .populate('customerId', 'name email phone address')
        .sort({ createdAt: -1 });
    } else if (req.user.role === 'admin') {
      orders = await Order.find()
        .populate('items.productId')
        .populate('customerId', 'name email')
        .populate('sellerId', 'name email')
        .sort({ createdAt: -1 });
    }

    res.json(orders);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get order by ID
router.get('/:id', authenticate, async (req, res) => {
  try {
    const order = await Order.findById(req.params.id)
      .populate('items.productId')
      .populate('customerId', 'name email phone address')
      .populate('sellerId', 'name email');

    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }

    // Check access
    if (req.user.role === 'customer' && order.customerId._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }
    if (req.user.role === 'seller' && order.sellerId._id.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }

    res.json(order);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Update order status (Seller or Admin)
router.put('/:id/status', authenticate, isAdminOrSeller, async (req, res) => {
  try {
    const { status } = req.body;
    const order = await Order.findById(req.params.id);

    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }

    // Seller can only update their own orders
    if (req.user.role === 'seller' && order.sellerId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Access denied' });
    }

    order.status = status;
    order.updatedAt = Date.now();
    await order.save();

    res.json(order);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

module.exports = router;

