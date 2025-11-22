const express = require('express');
const router = express.Router();
const Order = require('../models/order');
const Product = require('../models/product');
const { authenticate, isSeller } = require('../middleware/auth');

// Get revenue statistics (Seller)
router.get('/revenue', authenticate, isSeller, async (req, res) => {
  try {
    const { period } = req.query; // 'day', 'month', 'year'
    const sellerId = req.user._id;

    let startDate = new Date();
    let groupFormat = '%Y-%m-%d';

    if (period === 'day') {
      startDate.setHours(0, 0, 0, 0);
      groupFormat = '%Y-%m-%d %H:00:00';
    } else if (period === 'month') {
      startDate = new Date(startDate.getFullYear(), startDate.getMonth(), 1);
      groupFormat = '%Y-%m';
    } else if (period === 'year') {
      startDate = new Date(startDate.getFullYear(), 0, 1);
      groupFormat = '%Y';
    }

    const orders = await Order.find({
      sellerId,
      status: { $ne: 'cancelled' },
      createdAt: { $gte: startDate }
    });

    // Calculate revenue by period
    const revenueByPeriod = {};
    let totalRevenue = 0;

    orders.forEach(order => {
      const date = new Date(order.createdAt);
      let key;

      if (period === 'day') {
        key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
      } else if (period === 'month') {
        key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      } else {
        key = String(date.getFullYear());
      }

      if (!revenueByPeriod[key]) {
        revenueByPeriod[key] = 0;
      }
      revenueByPeriod[key] += order.finalAmount;
      totalRevenue += order.finalAmount;
    });

    res.json({
      period,
      totalRevenue,
      revenueByPeriod,
      orderCount: orders.length
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get best selling products (Seller)
router.get('/bestsellers', authenticate, isSeller, async (req, res) => {
  try {
    const sellerId = req.user._id;
    const { limit = 10 } = req.query;

    const orders = await Order.find({
      sellerId,
      status: { $ne: 'cancelled' }
    }).populate('items.productId');

    // Calculate product sales
    const productSales = {};

    orders.forEach(order => {
      order.items.forEach(item => {
        const productId = item.productId._id.toString();
        if (!productSales[productId]) {
          productSales[productId] = {
            product: item.productId,
            totalQuantity: 0,
            totalRevenue: 0
          };
        }
        productSales[productId].totalQuantity += item.quantity;
        productSales[productId].totalRevenue += item.price * item.quantity;
      });
    });

    // Sort by total quantity and get top products
    const bestSellers = Object.values(productSales)
      .sort((a, b) => b.totalQuantity - a.totalQuantity)
      .slice(0, parseInt(limit));

    res.json(bestSellers);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;

